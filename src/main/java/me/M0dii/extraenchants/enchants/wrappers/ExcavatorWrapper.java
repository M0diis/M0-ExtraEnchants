package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.events.ExcavatorEvent;
import me.m0dii.extraenchants.utils.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

@EnchantWrapper(name = "Excavator", maxLevel = 1)
public class ExcavatorWrapper extends CustomEnchantment {

    public ExcavatorWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isPickaxe(item) || EnchantableItemTypeUtil.isShovel(item) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        if (enchant.getCustomConflicts().contains(enchantment)) {
            return true;
        }

        if (!enchant.defaultConflictsEnabled()) {
            return false;
        }

        return Enchantment.SILK_TOUCH.equals(enchantment)
                || EEnchant.SMELT.equals(enchantment)
                || EEnchant.TELEPATHY.equals(enchantment);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
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

        InventoryUtils.applyDurabilityChanced(p, item, 70);
    }
}