package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.wrappers.LavaWalkerWrapper;
import me.m0dii.extraenchants.utils.Enchanter;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import me.m0dii.extraenchants.utils.pipeline.BlockBreakContext;
import me.m0dii.extraenchants.utils.pipeline.BlockBreakPipeline;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;


public class BlockBreak implements Listener {
    private static final NamespacedKey enchantKey = new NamespacedKey(ExtraEnchants.getInstance(), "extraenchants_enchant");
    private static final NamespacedKey enchantLevelKey = new NamespacedKey(ExtraEnchants.getInstance(), "extraenchants_enchant_level");

    private final List<String> heads = Arrays.asList(
            "PLAYER_HEAD", "SKELETON_SKULL", "CREEPER_HEAD", "WITHER_SKELETON_SKULL",
            "ZOMBIE_HEAD", "CREEPER_WALL_HEAD", "PLAYER_WALL_HEAD", "DRAGON_HEAD",
            "DRAGON_WALL_HEAD", "ZOMBIE_WALL_HEAD", "SKELETON_WALL_SKULL", "WITHER_SKELETON_WALL_SKULL"
    );

    private final ExtraEnchants plugin;
    private final BlockBreakPipeline pipeline;

    public BlockBreak(ExtraEnchants plugin) {
        this.plugin = plugin;
        this.pipeline = new BlockBreakPipeline(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent e) {
        if (shouldSkipGeneral(e)) {
            return;
        }

        BlockBreakContext ctx = new BlockBreakContext(plugin, e);
        pipeline.run(ctx);
    }

    @EventHandler
    public void onBlockBreakFixEnchant(final BlockBreakEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (!pdc.has(enchantKey, PersistentDataType.STRING)) {
            return;
        }

        String enchantName = pdc.get(enchantKey, PersistentDataType.STRING);
        EEnchant enchant = EEnchant.parse(enchantName);

        if (enchant == null || InventoryUtils.hasEnchant(item, enchant)) {
            return;
        }

        int level = pdc.getOrDefault(enchantLevelKey, PersistentDataType.INTEGER, 1);

        Enchanter.applyEnchantWithoutLore(item, enchant, level);
    }

    private boolean shouldSkipGeneral(@NotNull BlockBreakEvent e) {
        if (e.isCancelled()) {
            return true;
        }

        Block block = e.getBlock();

        if (LavaWalkerWrapper.lavaWalkerBlocks.contains(block)) {
            e.setDropItems(false);
            return true;
        }

        Boolean doTileDrops = block.getWorld().getGameRuleValue(GameRule.DO_TILE_DROPS);

        if (Boolean.FALSE.equals(doTileDrops)) {
            return true;
        }

        Material type = block.getType();

        if (type == Material.SPAWNER || type.name().contains("BED") || type.name().contains("SPAWNER")) {
            return true;
        }

        if (block.getState() instanceof Container || this.heads.contains(type.toString())) {
            return true;
        }

        Player p = e.getPlayer();
        ItemStack hand = p.getInventory().getItemInMainHand();

        if (hand.getType().isAir() || !Utils.allowedAt(p, block.getLocation())) {
            return true;
        }

        return p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR;
    }

    public static boolean isLog(Block block) {
        String name = block.getType().name();
        return name.contains("LOG") && !name.contains("STRIPPED");
    }
}