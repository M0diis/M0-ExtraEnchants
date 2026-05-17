package me.m0dii.extraenchants.mockbukkit;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.ExcavatorEvent;
import me.m0dii.extraenchants.events.ReplanterBreakEvent;
import me.m0dii.extraenchants.events.TelepathyEvent;
import me.m0dii.extraenchants.utils.pipeline.BlockBreakContext;
import me.m0dii.extraenchants.utils.pipeline.BlockBreakPipeline;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockBreakPipelineMockBukkitTest {

    private static Map<EEnchant, Enchantment> previousEnchants;

    private static ServerMock server;
    private static ExtraEnchants plugin;
    private static Plugin registrationPlugin;
    private static YamlConfiguration config;

    private PlayerMock player;
    private World world;
    private EventCollector listener;
    private BlockBreakPipeline pipeline;

    @BeforeAll
    static void beforeAll() throws Exception {
        server = MockBukkit.mock();
        config = new YamlConfiguration();
        config.set("debug-enchants.pipeline", false);
        config.set("enchants.excavator.ignored-blocks", List.of());

        plugin = Mockito.mock(ExtraEnchants.class);
        Mockito.when(plugin.getName()).thenReturn("M0-ExtraEnchants");
        Mockito.when(plugin.getConfig()).thenReturn(config);
        Mockito.when(plugin.getCfg()).thenReturn(config);
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("BlockBreakPipelineMockBukkitTest"));

        TestPluginBindingUtil.bindPlugin(plugin);
        previousEnchants = new EnumMap<>(EEnchant.class);
        configureTestEnchantments();

        registrationPlugin = MockBukkit.createMockPlugin();
    }

    @AfterAll
    static void afterAll() throws Exception {
        restoreEnchantments();
        TestPluginBindingUtil.bindPlugin(null);

        if (server != null) {
            MockBukkit.unmock();
        }
    }

    @BeforeEach
    void setUp() {
        world = server.addSimpleWorld("pipeline-world");
        player = server.addPlayer();
        player.teleport(new Location(world, 0, 64, 0));
        listener = new EventCollector();
        pipeline = new BlockBreakPipeline(plugin);

        server.getPluginManager().registerEvents(listener, registrationPlugin);
        config.set("enchants.excavator.ignored-blocks", List.of());
    }

    @AfterEach
    void tearDown() {
        player.getInventory().clear();
    }

    @Test
    void telepathyEnchantFiresEventAndDisablesVanillaDrops() {
        ItemStack tool = enchantedTool(Material.DIAMOND_PICKAXE, EEnchant.TELEPATHY);
        BlockBreakContext ctx = contextFor(Material.STONE, tool, List.of(new ItemStack(Material.COBBLESTONE)));

        pipeline.run(ctx);

        assertEquals(1, listener.telepathyCalls);
        assertFalse(ctx.getEvent().isDropItems());
        assertFalse(ctx.isSpawnDrops());
    }

    @Test
    void replanterEnchantFiresBreakEventForHoeTools() {
        ItemStack tool = enchantedTool(Material.DIAMOND_HOE, EEnchant.REPLANTER);
        BlockBreakContext ctx = contextFor(Material.WHEAT, tool, List.of(new ItemStack(Material.WHEAT_SEEDS)));

        pipeline.run(ctx);

        assertEquals(1, listener.replanterBreakCalls);
    }

    @Test
    void excavatorEnchantFiresEventAndEnablesPipelineDrops() {
        ItemStack tool = enchantedTool(Material.DIAMOND_PICKAXE, EEnchant.EXCAVATOR);
        BlockBreakContext ctx = contextFor(Material.STONE, tool, List.of(new ItemStack(Material.COBBLESTONE)));

        pipeline.run(ctx);

        assertEquals(1, listener.excavatorCalls);
        assertTrue(ctx.isSpawnDrops());
    }

    @Test
    void excavatorIsSkippedForIgnoredBlockTypes() {
        config.set("enchants.excavator.ignored-blocks", List.of("STONE"));

        ItemStack tool = enchantedTool(Material.DIAMOND_PICKAXE, EEnchant.EXCAVATOR);
        BlockBreakContext ctx = contextFor(Material.STONE, tool, List.of(new ItemStack(Material.COBBLESTONE)));

        pipeline.run(ctx);

        assertEquals(0, listener.excavatorCalls);
        assertFalse(ctx.isSpawnDrops());
    }

    @Test
    void telepathyDoesNotFireWhenThereAreNoDrops() {
        ItemStack tool = enchantedTool(Material.DIAMOND_PICKAXE, EEnchant.TELEPATHY);
        BlockBreakContext ctx = contextFor(Material.STONE, tool, List.of());

        pipeline.run(ctx);

        assertEquals(0, listener.telepathyCalls);
        assertTrue(ctx.getEvent().isDropItems());
    }

    @Test
    void pipelineDoesNothingWhenToolHasNoSupportedEnchantments() {
        ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
        BlockBreakContext ctx = contextFor(Material.STONE, tool, List.of(new ItemStack(Material.COBBLESTONE)));

        pipeline.run(ctx);

        assertEquals(0, listener.telepathyCalls + listener.replanterBreakCalls + listener.excavatorCalls);
        assertTrue(ctx.getEvent().isDropItems());
        assertFalse(ctx.isSpawnDrops());
    }

    private BlockBreakContext contextFor(Material blockType, ItemStack tool, List<ItemStack> drops) {
        player.getInventory().setItemInMainHand(tool);

        Block block = world.getBlockAt(0, 64, 0);
        block.setType(blockType);

        BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
        BlockBreakContext context = new BlockBreakContext(plugin, breakEvent);
        context.setDrops(new ArrayList<>(drops));

        return context;
    }

    private static ItemStack enchantedTool(Material material, EEnchant enchant) {
        ItemStack item = new ItemStack(material);
        item.addUnsafeEnchantment(enchant.getEnchantment(), 1);
        return item;
    }

    private static void configureTestEnchantments() {
        Enchantment[] shared = new Enchantment[]{
                Enchantment.SILK_TOUCH,
                Enchantment.UNBREAKING,
                Enchantment.FORTUNE,
                Enchantment.EFFICIENCY,
                Enchantment.MENDING,
                Enchantment.SHARPNESS,
                Enchantment.LOOTING,
                Enchantment.FIRE_ASPECT,
                Enchantment.POWER,
                Enchantment.PROTECTION,
                Enchantment.RESPIRATION
        };

        EEnchant[] usedInPipeline = new EEnchant[]{
                EEnchant.EXCAVATOR,
                EEnchant.TUNNEL,
                EEnchant.VEIN_MINER,
                EEnchant.SMELT,
                EEnchant.REPLANTER,
                EEnchant.TIMBER,
                EEnchant.TELEPATHY,
                EEnchant.STAT_TRACK,
                EEnchant.HASTE_MINER,
                EEnchant.EXPERIENCE_MINER,
                EEnchant.DISPOSER
        };

        for (int i = 0; i < usedInPipeline.length; i++) {
            EEnchant enchant = usedInPipeline[i];
            previousEnchants.put(enchant, enchant.getEnchantment());
            enchant.setEnchantment(shared[i]);
        }
    }

    private static void restoreEnchantments() {
        if (previousEnchants == null) {
            return;
        }

        previousEnchants.forEach(EEnchant::setEnchantment);
        previousEnchants.clear();
    }


    private static class EventCollector implements Listener {
        int telepathyCalls;
        int replanterBreakCalls;
        int excavatorCalls;

        @EventHandler
        public void onTelepathy(TelepathyEvent ignored) {
            telepathyCalls++;
        }

        @EventHandler
        public void onReplanterBreak(ReplanterBreakEvent ignored) {
            replanterBreakCalls++;
        }

        @EventHandler
        public void onExcavator(ExcavatorEvent ignored) {
            excavatorCalls++;
        }
    }
}

