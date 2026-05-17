package me.m0dii.extraenchants.mockbukkit;

import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.wrappers.ExcavatorWrapper;
import me.m0dii.extraenchants.enchants.wrappers.ReplanterWrapper;
import me.m0dii.extraenchants.enchants.wrappers.TelepathyWrapper;
import me.m0dii.extraenchants.enchants.wrappers.TimberWrapper;
import me.m0dii.extraenchants.events.ExcavatorEvent;
import me.m0dii.extraenchants.events.ReplanterBreakEvent;
import me.m0dii.extraenchants.events.TelepathyEvent;
import me.m0dii.extraenchants.events.TimberEvent;
import me.m0dii.extraenchants.utils.pipeline.BlockBreakContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriorityWrappersBehaviorMockBukkitTest {

    private static ServerMock server;
    private static ExtraEnchants plugin;
    private static Map<EEnchant, Enchantment> previousEnchants;

    private World world;
    private PlayerMock player;

    @BeforeAll
    static void beforeAll() throws Exception {
        server = MockBukkit.mock();

        YamlConfiguration config = new YamlConfiguration();
        config.set("debug-enchants.telepathy", false);
        config.set("debug-enchants.timber", false);

        PlatformScheduler scheduler = Mockito.mock(PlatformScheduler.class);
        WrappedTask wrappedTask = Mockito.mock(WrappedTask.class);
        Mockito.lenient().when(scheduler.runTimer(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.anyLong())).thenReturn(wrappedTask);
        Mockito.lenient().when(scheduler.runLater(Mockito.any(Runnable.class), Mockito.anyLong())).thenReturn(wrappedTask);

        plugin = Mockito.mock(ExtraEnchants.class);
        Mockito.when(plugin.getName()).thenReturn("M0-ExtraEnchants");
        Mockito.when(plugin.getConfig()).thenReturn(config);
        Mockito.when(plugin.getCfg()).thenReturn(config);
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("PriorityWrappersBehaviorMockBukkitTest"));
        Mockito.when(plugin.getScheduler()).thenReturn(scheduler);

        TestPluginBindingUtil.bindPlugin(plugin);

        for (EEnchant enchant : EEnchant.values()) {
            String base = "enchants." + enchant.getConfigName();
            config.set(base + ".enabled", true);
            config.set(base + ".trigger-chance", -1);
            config.set(base + ".default-conflicts", true);
            config.set(base + ".conflicts", List.of());
            config.set(base + ".ignored-blocks", List.of());
            config.set(base + ".enchantable-items", List.of());
        }

        previousEnchants = new EnumMap<>(EEnchant.class);
        previousEnchants.put(EEnchant.TELEPATHY, EEnchant.TELEPATHY.getEnchantment());
        previousEnchants.put(EEnchant.REPLANTER, EEnchant.REPLANTER.getEnchantment());
        previousEnchants.put(EEnchant.TIMBER, EEnchant.TIMBER.getEnchantment());
        previousEnchants.put(EEnchant.EXCAVATOR, EEnchant.EXCAVATOR.getEnchantment());

        EEnchant.TELEPATHY.setEnchantment(Enchantment.EFFICIENCY);
        EEnchant.REPLANTER.setEnchantment(Enchantment.UNBREAKING);
        EEnchant.TIMBER.setEnchantment(Enchantment.FORTUNE);
        EEnchant.EXCAVATOR.setEnchantment(Enchantment.MENDING);
    }

    @AfterAll
    static void afterAll() throws Exception {
        if (previousEnchants != null) {
            previousEnchants.forEach(EEnchant::setEnchantment);
            previousEnchants.clear();
        }

        TestPluginBindingUtil.bindPlugin(null);
        if (server != null) {
            MockBukkit.unmock();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        world = server.addSimpleWorld("priority-world");
        player = server.addPlayer();
        player.teleport(new Location(world, 0, 64, 0));

        clearStaticMap(ExcavatorWrapper.class, "excavatorBreaking");
        clearStaticMap(TimberWrapper.class, "playerQueues");
        clearStaticMap(TimberWrapper.class, "timberBreaking");
    }

    @Test
    void telepathyMovesDropsToInventoryWhenSpaceExists() {
        ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
        tool.addUnsafeEnchantment(EEnchant.TELEPATHY.getEnchantment(), 1);

        BlockBreakContext context = contextFor(Material.STONE, tool, List.of(new ItemStack(Material.COBBLESTONE, 2)));

        TelepathyWrapper wrapper = new TelepathyWrapper("Telepathy", 1, EEnchant.TELEPATHY);
        wrapper.onTelepathy(new TelepathyEvent(context));

        assertTrue(player.getInventory().contains(Material.COBBLESTONE));
        assertNotNull(player.getInventory().getItem(player.getInventory().first(Material.COBBLESTONE)));
    }

    @Test
    void replanterBreakIgnoresNonCropBlocksWithoutCancellingEvent() {
        ItemStack tool = new ItemStack(Material.DIAMOND_HOE);
        tool.addUnsafeEnchantment(EEnchant.REPLANTER.getEnchantment(), 1);

        Block crop = world.getBlockAt(0, 64, 0);
        crop.setType(Material.STONE);

        BlockBreakContext context = contextFor(crop, tool, List.of(new ItemStack(Material.WHEAT_SEEDS)));

        ReplanterWrapper wrapper = new ReplanterWrapper("Replanter", 1, EEnchant.REPLANTER);
        wrapper.onBlockBreak(new ReplanterBreakEvent(context));

        assertFalse(context.getEvent().isCancelled());
    }

    @Test
    void timberSchedulesAdditionalLogBreaksIntoPerPlayerQueue() throws Exception {
        ItemStack tool = new ItemStack(Material.DIAMOND_AXE);
        tool.addUnsafeEnchantment(EEnchant.TIMBER.getEnchantment(), 1);

        Block baseLog = world.getBlockAt(0, 64, 0);
        baseLog.setType(Material.OAK_LOG);
        world.getBlockAt(0, 65, 0).setType(Material.OAK_LOG);
        world.getBlockAt(0, 66, 0).setType(Material.OAK_LOG);

        BlockBreakContext context = contextFor(baseLog, tool, List.of(new ItemStack(Material.OAK_LOG)));

        TimberWrapper wrapper = new TimberWrapper("Timber", 1, EEnchant.TIMBER);
        wrapper.onTimber(new TimberEvent(context));

        Field field = TimberWrapper.class.getDeclaredField("playerQueues");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<Object, List<?>> queues = (Map<Object, List<?>>) field.get(null);

        assertTrue(queues.containsKey(player));
        assertFalse(queues.get(player).isEmpty());
    }

    @Test
    void excavatorSkipsSecondaryBreaksAlreadyMarkedInGuardMap() throws Exception {
        ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
        tool.addUnsafeEnchantment(EEnchant.EXCAVATOR.getEnchantment(), 1);

        Block source = world.getBlockAt(0, 64, 0);
        source.setType(Material.STONE);

        BlockBreakContext context = contextFor(source, tool, List.of(new ItemStack(Material.COBBLESTONE)));

        Field field = ExcavatorWrapper.class.getDeclaredField("excavatorBreaking");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<Block, Long> marked = (Map<Block, Long>) field.get(null);
        marked.put(source, System.currentTimeMillis());

        ExcavatorWrapper wrapper = new ExcavatorWrapper("Excavator", 1, EEnchant.EXCAVATOR);
        wrapper.onExcavator(new ExcavatorEvent(context));

        assertEquals(1, context.getDrops().size());
        assertEquals(Material.COBBLESTONE, context.getDrops().getFirst().getType());
    }

    private BlockBreakContext contextFor(Material blockType, ItemStack tool, List<ItemStack> drops) {
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

    private static void clearStaticMap(Class<?> owner, String fieldName) throws Exception {
        Field field = owner.getDeclaredField(fieldName);
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<Object, Object> map = (Map<Object, Object>) field.get(null);

        map.clear();
    }

}

