package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.AssassinEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@EnchantWrapper(name = "Assassin", maxLevel = 1)
public class AssassinWrapper extends CustomEnchantment {

    public AssassinWrapper(final String name, final int lvl, EEnchant enchant) {
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
                || EEnchant.BERSERK.equals(enchantment);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAssassin(final AssassinEvent e) {
        if (!Utils.shouldTrigger(EEnchant.ASSASSIN)) {
            return;
        }

        EntityDamageEvent damageEvent = e.getEntityDamageEvent();

        Entity target = damageEvent.getEntity();

        if (EEnchant.ASSASSIN.isPlayerOnly() && !(target instanceof Player)) {
            return;
        }

        Player damager = e.getPlayer();

        double damage = damageEvent.getDamage();

        Location loc = target.getLocation();
        Location loc2 = damager.getLocation();

        double distance = loc.distance(loc2);

        if (distance <= 0.15) {
            damageEvent.setDamage(damage * 1.20);
        }

        if (distance > 0.15 && distance <= 0.5) {
            damageEvent.setDamage(damage * 1.15);
        }

        if (distance > 0.5 && distance <= 1) {
            damageEvent.setDamage(damage * 1.10);
        }

        if (distance > 1 && distance <= 1.5) {
            damageEvent.setDamage(damage * 1.05);
        }

        if (distance > 1.5 && distance <= 2) {
            damageEvent.setDamage(damage * 0.90);
        }

        if (distance > 2 && distance <= 2.5) {
            damageEvent.setDamage(damage * 0.85);
        }

        if (distance > 2.5 && distance <= 3) {
            damageEvent.setDamage(damage * 0.80);
        }

        if (distance > 3) {
            damageEvent.setDamage(damage * 0.7);
        }
    }
}
