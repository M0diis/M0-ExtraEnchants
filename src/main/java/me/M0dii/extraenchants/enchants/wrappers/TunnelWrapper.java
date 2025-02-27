package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.TunnelEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
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

import java.util.Set;
import java.util.stream.Stream;

@EnchantWrapper(name = "Tunnel", maxLevel = 3)
public class TunnelWrapper extends CustomEnchantment {

    public TunnelWrapper(final String name, final int lvl, EEnchant enchant) {
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

        return Enchantment.SILK_TOUCH.equals(enchantment);
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
    public void onTunnel(final TunnelEvent e) {
        Messenger.debug("Tunnel event called.");

        if (!Utils.shouldTrigger(EEnchant.TUNNEL)) {
            return;
        }

        Player p = e.getPlayer();

        Block source = e.getBlock();

        int level = e.getEnchantLevel();

        BlockFace facing = p.getFacing();

        Messenger.debug("Player facing: " + facing);

        if (level == 1) {
            Block opposite = source.getRelative(facing);

            destroy(p, opposite);
        }

        if (level == 2) {
            Block opposite = source.getRelative(facing);

            destroy(p, opposite);

            destroy(p, source.getRelative(BlockFace.DOWN));
            destroy(p, opposite.getRelative(BlockFace.DOWN));
        }
    }

    private void destroy(Player p, Block b) {
        if (!b.getType().isSolid()) {
            Messenger.debug("Block is not solid, skipping tunnel.");
            return;
        }

        if (!Utils.allowedAt(p, b.getLocation())) {
            Messenger.debug("Player not allowed, skipping tunnel.");
            return;
        }

        Material conflict = Stream.of(Material.BEDROCK,
                        Material.BARRIER,
                        Material.END_PORTAL_FRAME,
                        Material.CHEST,
                        Material.SPAWNER,
                        Material.END_CRYSTAL,
                        Material.END_GATEWAY,
                        Material.END_PORTAL
                )
                .filter(b.getType()::equals)
                .findFirst()
                .orElse(null);

        if (conflict != null) {
            Messenger.debug("Block is a conflict, skipping tunnel.");
            return;
        }

        b.breakNaturally(p.getInventory().getItemInMainHand());
    }
}
