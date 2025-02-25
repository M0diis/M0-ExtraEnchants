package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.*;
import me.m0dii.extraenchants.listeners.custom.OnLavaWalk;
import me.m0dii.extraenchants.utils.Enchantables;
import me.m0dii.extraenchants.utils.Enchanter;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
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
import java.util.Collection;
import java.util.List;

public class BlockBreak implements Listener {
    private static final NamespacedKey enchantKey = new NamespacedKey(ExtraEnchants.getInstance(), "extraenchants_enchant");
    private static final NamespacedKey enchantLevelKey = new NamespacedKey(ExtraEnchants.getInstance(), "extraenchants_enchant_level");

    private final List<String> heads = Arrays.asList("PLAYER_HEAD", "SKELETON_SKULL", "CREEPER_HEAD", "WITHER_SKELETON_SKULL",
            "ZOMBIE_HEAD", "CREEPER_WALL_HEAD", "PLAYER_WALL_HEAD", "DRAGON_HEAD", "DRAGON_WALL_HEAD", "ZOMBIE_WALL_HEAD",
            "SKELETON_WALL_SKULL", "WITHER_SKELETON_WALL_SKULL");

    private final ExtraEnchants plugin;

    public BlockBreak(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakTelepathy(final BlockBreakEvent e) {
        if (shouldSkip(e, EEnchant.TELEPATHY)) {
            return;
        }

        Player p = e.getPlayer();
        Block b = e.getBlock();

        ItemStack hand = p.getInventory().getItemInMainHand();

        Collection<ItemStack> drops = b.getDrops(hand);

        if (drops.isEmpty()) {
            return;
        }

        e.setDropItems(false);

        List<Enchantment> conflicts = List.of(
                EEnchant.SMELT.getEnchantment(),
                EEnchant.EXCAVATOR.getEnchantment()
        );

        boolean hasConflicts = conflicts.stream().anyMatch(enchant -> InventoryUtils.hasEnchant(hand, enchant));

        if (hasConflicts) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new TelepathyEvent(p, e, drops));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakStatTrack(final BlockBreakEvent e) {
        if (shouldSkip(e, EEnchant.STAT_TRACK)) {
            return;
        }

        Player p = e.getPlayer();
        Block b = e.getBlock();

        ItemStack hand = p.getInventory().getItemInMainHand();

        Collection<ItemStack> drops = b.getDrops(hand);

        if (drops.isEmpty()) {
            return;
        }

        e.setDropItems(false);

        Bukkit.getPluginManager().callEvent(new StatTrackEvent(p, e, drops));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakSmelt(final BlockBreakEvent e) {
        if (shouldSkip(e, EEnchant.SMELT)) {
            return;
        }

        Player p = e.getPlayer();
        Block b = e.getBlock();

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();

        List<Enchantment> conflicts = List.of(
                EEnchant.EXCAVATOR.getEnchantment()
        );

        boolean hasConflicts = conflicts.stream()
                .anyMatch(enchant -> InventoryUtils.hasEnchant(hand, enchant));

        if (hasConflicts) {
            return;
        }

        Collection<ItemStack> drops = b.getDrops(hand);

        e.setDropItems(false);

        Bukkit.getPluginManager().callEvent(new SmeltEvent(p, e, drops));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakHasteMiner(final BlockBreakEvent e) {
        if (shouldSkip(e, EEnchant.HASTE_MINER)) {
            return;
        }

        int level = InventoryUtils.getEnchantLevelHand(e.getPlayer(), EEnchant.HASTE_MINER);

        Bukkit.getPluginManager().callEvent(new HasteMinerEvent(e.getPlayer(), e, level));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakExperienceMiner(final BlockBreakEvent e) {
        if (shouldSkip(e, EEnchant.EXPERIENCE_MINER)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new ExperienceMinerEvent(e.getPlayer(), e));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakVeinMiner(final BlockBreakEvent e) {
        if (shouldSkip(e, EEnchant.VEIN_MINER)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new VeinMinerEvent(e.getPlayer(), e));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakTunnel(final BlockBreakEvent e) {
        if (shouldSkip(e, EEnchant.TUNNEL)) {
            return;
        }

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();

        Bukkit.getPluginManager().callEvent(new TunnelEvent(e.getPlayer(), e,
                hand.getEnchantmentLevel(EEnchant.TUNNEL.getEnchantment())));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakExcavator(final BlockBreakEvent e) {
        if (shouldSkip(e, EEnchant.EXCAVATOR)) {
            return;
        }

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();

        Bukkit.getPluginManager().callEvent(new ExcavatorEvent(e.getPlayer(), e,
                hand.getEnchantmentLevel(EEnchant.EXCAVATOR.getEnchantment())));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakDisposer(final BlockBreakEvent e) {
        if (shouldSkip(e, EEnchant.DISPOSER)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new DisposerEvent(e.getPlayer(), e));
    }

    private boolean shouldSkip(final BlockBreakEvent e, @NotNull EEnchant enchant) {
        if (e.isCancelled()) {
            return true;
        }

        Block block = e.getBlock();

        if (OnLavaWalk.lavaWalkerBlocks.contains(block)) {
            e.setDropItems(false);

            return true;
        }

        if (block.getWorld().getGameRuleValue(GameRule.DO_TILE_DROPS) == Boolean.FALSE) {
            return true;
        }

        Material type = block.getType();

        if (type.equals(Material.SPAWNER)
                || type.name().toUpperCase().contains("BED")
                || type.name().toUpperCase().contains("SPAWNER")) {
            return true;
        }

        if ((block.getState() instanceof Container)) {
            return true;
        }

        if (this.heads.contains(type.toString())) {
            return true;
        }

        Player p = e.getPlayer();

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();

        if (hand.getType().isAir()) {
            return true;
        }

        if (!InventoryUtils.hasEnchant(hand, enchant)) {
            return true;
        }

        if (!Utils.allowedAt(p, e.getBlock().getLocation())) {
            return true;
        }

        return (p.getGameMode() == GameMode.CREATIVE)
                || (p.getGameMode() == GameMode.SPECTATOR);
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

        if (enchant == null) {
            return;
        }

        if (InventoryUtils.hasEnchant(item, enchant)) {
            return;
        }

        int level = pdc.getOrDefault(enchantLevelKey, PersistentDataType.INTEGER, 1);

        Enchanter.applyEnchantWithoutLore(item, enchant, level);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreakReplanter(final BlockBreakEvent e) {
        if (shouldSkip(e, EEnchant.REPLANTER)) {
            return;
        }

        Player p = e.getPlayer();

        ItemStack hand = p.getInventory().getItemInMainHand();

        if (!Enchantables.isHoe(hand)) {
            return;
        }

        int level = InventoryUtils.getEnchantLevel(hand, EEnchant.REPLANTER);

        Bukkit.getPluginManager().callEvent(new ReplanterBreakEvent(p, e, level));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreakTimber(final BlockBreakEvent e) {
        if (shouldSkip(e, EEnchant.TIMBER)) {
            return;
        }

        Player p = e.getPlayer();

        ItemStack hand = p.getInventory().getItemInMainHand();

        if (!Enchantables.isAxe(hand)) {
            return;
        }

        if (!isLog(e.getBlock())) {
            return;
        }

        Block b = e.getBlock();

        Collection<ItemStack> drops = b.getDrops(hand);

        e.setDropItems(false);

        Bukkit.getPluginManager().callEvent(new TimberEvent(p, e, drops));
    }

    private static boolean isLog(Block block) {
        String name = block.getType().name();
        return name.contains("LOG") && !name.contains("STRIPPED");
    }
}