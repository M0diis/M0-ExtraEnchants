package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.*;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.listeners.custom.OnLavaWalk;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BlockBreak implements Listener {
    private final List<String> heads = Arrays.asList("PLAYER_HEAD", "SKELETON_SKULL", "CREEPER_HEAD", "WITHER_SKELETON_SKULL",
            "ZOMBIE_HEAD", "CREEPER_WALL_HEAD", "PLAYER_WALL_HEAD", "DRAGON_HEAD", "DRAGON_WALL_HEAD", "ZOMBIE_WALL_HEAD",
            "SKELETON_WALL_SKULL", "WITHER_SKELETON_WALL_SKULL");

    private final List<String> hoes = Arrays.asList(
            "NETHERITE_HOE", "DIAMOND_HOE", "IRON_HOE",
            "GOLDEN_HOE", "STONE_HOE", "WOODEN_HOE");

    private final ExtraEnchants plugin;

    public BlockBreak(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakTelepathy(BlockBreakEvent e) {
        if (!plugin.getCfg().getBoolean("enchants.telepathy.enabled")) {
            return;
        }

        if (e.isCancelled()) {
            return;
        }

        if (e.getBlock().getWorld().getGameRuleValue(GameRule.DO_TILE_DROPS) == Boolean.FALSE)
            return;

        Player p = e.getPlayer();
        Block b = e.getBlock();

        if (OnLavaWalk.lavaWalkerBlocks.contains(b)) {
            e.setDropItems(false);

            return;
        }

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();

        if (b.getType().equals(Material.SPAWNER)
         || b.getType().name().toUpperCase().contains("SPAWNER"))
            return;

        ItemStack offhand = e.getPlayer().getInventory().getItemInOffHand();

        if (offhand != null) {
            if (offhand.hasItemMeta() &&
                offhand.getItemMeta().hasEnchant(EEnchant.TELEPATHY.getEnchant())) {
                return;
        }

        if (hand == null)
            return;

        if (hand.getItemMeta() == null)
            return;

        if (!hand.getItemMeta().hasEnchant(EEnchant.SMELT.getEnchant())
         && !hand.getItemMeta().hasEnchant(EEnchant.TELEPATHY.getEnchant())
         && !hand.getItemMeta().hasEnchant(EEnchant.PLOW.getEnchant()))
            return;

        if ((p.getGameMode() == GameMode.CREATIVE)
         || (p.getGameMode() == GameMode.SPECTATOR))
            return;

        if ((e.getBlock().getState() instanceof Container))
            return;

        if (this.heads.contains(b.getType().toString()))
            return;

        Collection<ItemStack> drops = b.getDrops(hand);

        e.setDropItems(false);

        if (hand.getItemMeta().hasEnchant(EEnchant.PLOW.getEnchant())) {
            Bukkit.getPluginManager().callEvent(new SmeltEvent(p, e, drops));
        } else if (!hand.getItemMeta().hasEnchant(EEnchant.SMELT.getEnchant()) &&
                hand.getItemMeta().hasEnchant(EEnchant.TELEPATHY.getEnchant())) {
                if (hoes.contains(hand.getType().toString())) {
                    for (ItemStack drop : drops)
                        p.getInventory().addItem(drop);

                    return;
                }

                if (b.getType().name().contains("BED")) {
                    p.getInventory().addItem(new ItemStack(b.getType()));

                    return;
                }

                if (drops.isEmpty()) {
                    return;
                }

                Bukkit.getPluginManager().callEvent(new TelepathyEvent(p, e, drops));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakHasteMiner(BlockBreakEvent e) {
        if (!plugin.getCfg().getBoolean("enchants.hasteminer.enabled")) {
            return;
        }

        if(shouldSkip(e, EEnchant.HASTE_MINER.getEnchant())) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new HasteMinerEvent(e.getPlayer(), e));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakExperienceMiner(BlockBreakEvent e) {
        if (!plugin.getCfg().getBoolean("enchants.experienceminer.enabled")) {
            return;
        }

        if(shouldSkip(e, EEnchant.EXPERIENCE_MINER.getEnchant())) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new ExperienceMinerEvent(e.getPlayer(), e));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakVeinMiner(BlockBreakEvent e) {
        if (!plugin.getCfg().getBoolean("enchants.veinminer.enabled")) {
            return;
        }

        if(shouldSkip(e, EEnchant.VEIN_MINER.getEnchant())) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new VeinMinerEvent(e.getPlayer(), e));
    }

    private boolean shouldSkip(BlockBreakEvent e, Enchantment enchant) {
        if (e.isCancelled()) {
            return true;
        }

        if (e.getBlock().getWorld().getGameRuleValue(GameRule.DO_TILE_DROPS) == Boolean.FALSE) {
            return true;
        }

        Player p = e.getPlayer();

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();

        if (hand == null || hand.getType().equals(Material.AIR)) {
            return true;
        }

        if (hand.getItemMeta() == null || !hand.hasItemMeta()) {
            return true;
        }

        if (!hand.getItemMeta().hasEnchant(enchant)) {
            return true;
        }

        return (p.getGameMode() == GameMode.CREATIVE)
            || (p.getGameMode() == GameMode.SPECTATOR);
    }
}
