package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OnLavaWalk implements Listener {
    public static List<Block> lavaWalkerBlocks;
    private final ExtraEnchants plugin;

    public OnLavaWalk(ExtraEnchants plugin) {
        this.plugin = plugin;

        lavaWalkerBlocks = new ArrayList<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();

        if (lavaWalkerBlocks.contains(b)) {
            e.setDropItems(false);
        }
    }

    @EventHandler
    public void onLavaWalker(final PlayerMoveEvent e) {
        if (!Utils.shouldTrigger(EEnchant.LAVA_WALKER)) {
            return;
        }

        ItemStack boots = e.getPlayer().getInventory().getBoots();

        if (!InventoryUtils.hasEnchant(boots, EEnchant.LAVA_WALKER)) {
            return;
        }

        Block b = e.getTo().clone().subtract(0.0D, 1.0D, 0.0D).getBlock();
        checkAndSet(b);

        Block blkN = b.getRelative(BlockFace.NORTH);
        checkAndSet(blkN);
        getNorthSouth(blkN);

        Block blkE = b.getRelative(BlockFace.EAST);
        checkAndSet(blkE);
        getNorthSouth(blkE);

        Block blkS = b.getRelative(BlockFace.SOUTH);
        checkAndSet(blkS);
        getNorthSouth(blkS);

        Block blkW = b.getRelative(BlockFace.WEST);
        checkAndSet(blkW);
        getNorthSouth(blkW);
    }

    private void getNorthSouth(final Block b) {
        checkAndSet(b.getRelative(BlockFace.NORTH));
        checkAndSet(b.getRelative(BlockFace.SOUTH));
    }

    private void checkAndSet(final Block b) {
        if (b.isLiquid() && b.getType().equals(Material.LAVA)) {
            BlockData bd = b.getBlockData();

            if (bd instanceof Levelled lv) {
                if (lv.getLevel() != 0) {
                    return;
                }

                lavaWalkerBlocks.add(b);

                removeBlockLater(b, b.getType());
                b.setType(Material.OBSIDIAN);

                b.getState().update();
            }
        }
    }

    private void removeBlockLater(final Block current, final Material previous) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            lavaWalkerBlocks.remove(current);

            current.setType(previous);
        }, 20L * EEnchant.LAVA_WALKER.getDuration());
    }
}
