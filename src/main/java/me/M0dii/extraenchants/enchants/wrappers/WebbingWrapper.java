package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.WebbingEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@EnchantWrapper(name = "Webbing", maxLevel = 1)
public class WebbingWrapper extends CustomEnchantment {

    public WebbingWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isSword(item) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        if (enchant.getCustomConflicts().contains(enchantment)) {
            return true;
        }

        if (!enchant.defaultConflictsEnabled()) {
            return false;
        }

        return EEnchant.LIFESTEAL.equals(enchantment)
                || EEnchant.BERSERK.equals(enchantment)
                || EEnchant.BARBARIAN.equals(enchantment)
                || Enchantment.FIRE_ASPECT.equals(enchantment);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    private final List<Block> webs = new ArrayList<>();

    @EventHandler
    public void onWithering(final WebbingEvent e) {
        Messenger.debug("WebbingEvent called");

        if (!Utils.shouldTrigger(EEnchant.WEBBING)) {
            return;
        }

        Entity target = e.getEntityDamageEvent().getEntity();

        if (EEnchant.WEBBING.isPlayerOnly() && !(target instanceof Player)) {
            return;
        }

        Location feet = target.getLocation();

        if (!feet.getBlock().getType().isAir()) {
            return;
        }

        if (webs.contains(feet.getBlock())) {
            return;
        }

        webs.add(feet.getBlock());

        feet.getBlock().setType(Material.COBWEB);

        Bukkit.getScheduler().runTaskLater(ExtraEnchants.getInstance(), () -> {
            if (feet.getBlock().getType() == Material.COBWEB) {
                feet.getBlock().setType(Material.AIR);
                webs.remove(feet.getBlock());
            }
        }, 20L * EEnchant.WEBBING.getDuration());
    }
}
