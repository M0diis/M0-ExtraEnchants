package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.events.ExcavatorEvent;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import me.m0dii.extraenchants.utils.pipeline.BlockBreakContext;
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

@SuppressWarnings("removal")
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

        BlockBreakContext ctx = e.getContext();
        ctx.getDrops().clear();

        Player p = ctx.player();

        Block source = ctx.block();

        float pitch = p.getLocation().getPitch();

        BlockFace facing = p.getFacing();

        if (pitch < -40 || pitch > 40) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block b = source.getRelative(x, 0, z);
                    destroy(p, b, ctx);
                }
            }
        } else if (facing.equals(BlockFace.SOUTH) || facing.equals(BlockFace.NORTH)) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    Block b = source.getRelative(x, y, 0);
                    destroy(p, b, ctx);
                }
            }
        } else if (facing.equals(BlockFace.EAST) || facing.equals(BlockFace.WEST)) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    Block b = source.getRelative(0, y, z);
                    destroy(p, b, ctx);
                }
            }
        }
    }

    private static final Set<Material> conflicts = EnumSet.of(
            Material.BEDROCK,
            Material.BARRIER
    );

    private void destroy(Player p, Block b, @NotNull BlockBreakContext ctx) {
        if (!b.getType().isSolid()) {
            Messenger.debug("Block is not solid, skipping excavator.");
            return;
        }

        if (!Utils.allowedAt(p, b.getLocation())) {
            Messenger.debug("Player not allowed, skipping excavator.");
            return;
        }

        if (conflicts.contains(b.getType())) {
            Messenger.debug("Block is a conflict, skipping excavator.");
            return;
        }

        if (EEnchant.EXCAVATOR.ignoresBlock(b.getType())) {
            Messenger.debug("Block is ignored by excavator, skipping.");
            return;
        }

        ItemStack item = ctx.toolUsed();

        ctx.addDrops(b.getDrops(item));
        b.setType(Material.AIR);

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