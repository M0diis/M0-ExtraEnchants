package me.m0dii.extraenchants.mockbukkit;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.wrappers.DisposerWrapper;
import me.m0dii.extraenchants.enchants.wrappers.SmeltWrapper;
import me.m0dii.extraenchants.events.DisposerEvent;
import me.m0dii.extraenchants.utils.pipeline.BlockBreakContext;
import me.m0dii.extraenchants.utils.pipeline.BlockBreakPipeline;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterAll;
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

import static org.junit.jupiter.api.Assertions.*;

class EnchantWrappersMockBukkitTest {

    private static ServerMock server;
    private static ExtraEnchants plugin;
    private static YamlConfiguration config;
    private static Map<EEnchant, Enchantment> previousEnchants;
    private static Plugin registrationPlugin;

    private World world;
    private PlayerMock player;

    @BeforeAll
    static void beforeAll() throws Exception {
        server = MockBukkit.mock();

        config = new YamlConfiguration();
        config.set("enchants.smelt.enabled", true);
        config.set("enchants.smelt.trigger-chance", -1);
        config.set("enchants.tunnel.enabled", true);
        config.set("enchants.tunnel.trigger-chance", -1);
        config.set("enchants.disposer.enabled", true);
        config.set("enchants.disposer.trigger-chance", -1);

        plugin = Mockito.mock(ExtraEnchants.class);
        Mockito.when(plugin.getName()).thenReturn("M0-ExtraEnchants");
        Mockito.when(plugin.getConfig()).thenReturn(config);
        Mockito.when(plugin.getCfg()).thenReturn(config);
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("EnchantWrappersMockBukkitTest"));

        TestPluginBindingUtil.bindPlugin(plugin);
        previousEnchants = new EnumMap<>(EEnchant.class);

        previousEnchants.put(EEnchant.SMELT, EEnchant.SMELT.getEnchantment());
        previousEnchants.put(EEnchant.DISPOSER, EEnchant.DISPOSER.getEnchantment());
        EEnchant.SMELT.setEnchantment(Enchantment.EFFICIENCY);
        EEnchant.DISPOSER.setEnchantment(Enchantment.UNBREAKING);

        registrationPlugin = MockBukkit.createMockPlugin();
    }

    @AfterAll
    static void afterAll() throws Exception {
        if (previousEnchants != null) {
            previousEnchants.forEach(EEnchant::setEnchantment);
            previousEnchants.clear();
        }

        TestPluginBindingUtil.bindPlugin(null);
        MockBukkit.unmock();
    }

    @BeforeEach
    void setUp() {
        world = server.addSimpleWorld("wrappers-world");
        player = server.addPlayer();
        player.teleport(new Location(world, 0, 64, 0));

        server.getPluginManager().registerEvents(new SmeltWrapper("Smelt", 1, EEnchant.SMELT), registrationPlugin);
        server.getPluginManager().registerEvents(new DisposerWrapper("Disposer", 1, EEnchant.DISPOSER), registrationPlugin);

        FurnaceRecipe recipe = new FurnaceRecipe(
                new NamespacedKey("test", "iron_ore_to_ingot"),
                new ItemStack(Material.IRON_INGOT),
                Material.IRON_ORE,
                1.0f,
                100
        );
        Bukkit.addRecipe(recipe);
    }

    @Test
    void smeltInPlaceConvertsSmeltableDrops() {
        ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
        BlockBreakContext context = contextWithDrops(Material.STONE, tool, List.of(new ItemStack(Material.IRON_ORE, 2)));

        SmeltWrapper.smeltInPlace(context);

        assertEquals(1, context.getDrops().size());
        assertEquals(Material.IRON_INGOT, context.getDrops().getFirst().getType());
        assertEquals(2, context.getDrops().getFirst().getAmount());
    }

    @Test
    void smeltInPlaceKeepsOriginalDropsForUnsmeltableMaterials() {
        ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
        BlockBreakContext context = contextWithDrops(Material.STONE, tool, List.of(new ItemStack(Material.COAL_ORE)));

        SmeltWrapper.smeltInPlace(context);

        assertEquals(1, context.getDrops().size());
        assertEquals(Material.COAL_ORE, context.getDrops().getFirst().getType());
    }

    @Test
    void smeltInPlaceRespectsSilkTouchAndReturnsOriginalDrops() {
        ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
        tool.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);

        BlockBreakContext context = contextWithDrops(Material.STONE, tool, List.of(new ItemStack(Material.IRON_ORE)));

        SmeltWrapper.smeltInPlace(context);

        assertEquals(1, context.getDrops().size());
        assertEquals(Material.IRON_ORE, context.getDrops().getFirst().getType());
    }

    @Test
    void disposerClearsDropsAndForcesNoDropSpawn() {
        ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
        BlockBreakContext context = contextWithDrops(Material.STONE, tool, List.of(new ItemStack(Material.COBBLESTONE)));
        context.setSpawnDrops(true);
        context.getEvent().setDropItems(true);

        DisposerWrapper wrapper = new DisposerWrapper("Disposer", 1, EEnchant.DISPOSER);
        wrapper.onDisposer(new DisposerEvent(context));

        assertFalse(context.getEvent().isDropItems());
        assertFalse(context.isSpawnDrops());
        assertTrue(context.getDrops().isEmpty());
    }

    @Test
    void smeltAndDisposerTogetherEndWithNoDropsBecauseDisposerRunsLater() {
        ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
        tool.addUnsafeEnchantment(EEnchant.SMELT.getEnchantment(), 1);
        tool.addUnsafeEnchantment(EEnchant.DISPOSER.getEnchantment(), 1);

        Block source = world.getBlockAt(0, 64, 0);
        source.setType(Material.STONE);

        BlockBreakContext context = contextFor(source, tool, List.of(new ItemStack(Material.IRON_ORE)));
        BlockBreakPipeline pipeline = new BlockBreakPipeline(plugin);

        pipeline.run(context);

        assertFalse(context.getEvent().isDropItems());
        assertFalse(context.isSpawnDrops());
        assertTrue(context.getDrops().isEmpty());
    }

    private BlockBreakContext contextWithDrops(Material blockType, ItemStack tool, List<ItemStack> drops) {
        Block block = world.getBlockAt(0, 64, 0);
        block.setType(blockType);
        return contextFor(block, tool, drops);
    }

    private BlockBreakContext contextFor(Block block, ItemStack tool, List<ItemStack> drops) {
        player.getInventory().setItemInMainHand(tool);

        BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
        BlockBreakContext context = new BlockBreakContext(plugin, breakEvent);
        context.setDrops(new ArrayList<>(drops));

        return context;
    }

}

