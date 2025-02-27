package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.BerserkEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@EnchantWrapper(name = "Berserk", maxLevel = 1)
public class BerserkWrapper extends CustomEnchantment {

    public BerserkWrapper(final String name, final int lvl, EEnchant enchant) {
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

        return EEnchant.LIFESTEAL.equals(enchantment) || EEnchant.ASSASSIN.equals(enchantment);
    }

    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    @EventHandler
    public void onBerserk(final BerserkEvent e) {
        Messenger.debug("BerserkEvent called");
        if (!Utils.shouldTrigger(EEnchant.BERSERK)) {
            return;
        }

        Entity target = e.getEntityDamageEvent().getEntity();

        if (EEnchant.BERSERK.isPlayerOnly() && !(target instanceof Player)) {
            return;
        }

        Player damager = e.getPlayer();

        double healthHas = damager.getHealth();
        AttributeInstance attribute = damager.getAttribute(Attribute.MAX_HEALTH);

        if (attribute == null) {
            return;
        }

        double healthMax = attribute.getValue();
        double healthDiff = healthMax - healthHas;

        double healthPoint = 0.5;

        if (healthHas >= healthMax || healthDiff < healthPoint) {
            return;
        }

        int pointAmount = (int) (healthDiff / healthPoint);

        if (pointAmount == 0) {
            return;
        }

        double damageAmount = 0.1;
        double damageCap = 1.30;
        double damageFinal = Math.min(damageCap, 1D + damageAmount * pointAmount);

        e.getEntityDamageEvent().setDamage(e.getEntityDamageEvent().getDamage() * damageFinal);
    }
}
