package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.ExcavatorEvent;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public class OnExcavator implements Listener {
    private final ExtraEnchants plugin;

    public OnExcavator(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExcavator(final ExcavatorEvent e) {
        Messenger.debug("Excavator event called.");

        if (!Utils.shouldTrigger(EEnchant.EXCAVATOR)) {
            return;
        }

        Player p = e.getPlayer();

        Block source = e.getBlock();

        float pitch = p.getLocation().getPitch();

        BlockFace facing = p.getFacing();

        if (pitch < -40 || pitch > 40) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block b = source.getRelative(x, 0, z);
                    destroy(p, b, e);
                }
            }
        } else if (facing.equals(BlockFace.SOUTH) || facing.equals(BlockFace.NORTH)) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    Block b = source.getRelative(x, y, 0);
                    destroy(p, b, e);
                }
            }
        } else if (facing.equals(BlockFace.EAST) || facing.equals(BlockFace.WEST)) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    Block b = source.getRelative(0, y, z);
                    destroy(p, b, e);
                }
            }
        }
    }

    private static final Set<Material> conflicts = EnumSet.of(
            Material.BEDROCK,
            Material.BARRIER,
            Material.END_PORTAL_FRAME,
            Material.CHEST,
            Material.SPAWNER,
            Material.END_CRYSTAL,
            Material.END_GATEWAY,
            Material.END_PORTAL,
            Material.BEACON,
            Material.COMMAND_BLOCK,
            Material.HOPPER,
            Material.DROPPER,
            Material.DISPENSER,
            Material.FURNACE,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.BREWING_STAND,
            Material.ENCHANTING_TABLE,
            Material.ANVIL,
            Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL,
            Material.GRINDSTONE,
            Material.SMITHING_TABLE,
            Material.CARTOGRAPHY_TABLE,
            Material.FLETCHING_TABLE
    );

    private void destroy(Player p, Block b, ExcavatorEvent e) {
        if (!b.getType().isSolid()) {
            Messenger.debug("Block is not solid, skipping excavator.");
            return;
        }

        if (!Utils.allowedAt(p, b.getLocation())) {
            Messenger.debug("Player not allowed, skipping excavator.");
            return;
        }

        boolean conflict = conflicts.contains(b.getType());

        if (conflict) {
            Messenger.debug("Block is a conflict, skipping excavator.");
            return;
        }

        ItemStack item = p.getInventory().getItemInMainHand();

        b.breakNaturally(item);

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = b.getRelative(x, y, z);
                    block.getState().update();
                    p.sendBlockChange(block.getLocation(), block.getBlockData());
                }
            }
        }

        InventoryUtils.applyDurabilityChanced(p, p.getInventory().getItemInMainHand(), 70);
    }
}