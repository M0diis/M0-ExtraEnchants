package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.DebuffingEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@EnchantWrapper(name = "Debuffing", maxLevel = 1)
public class DebuffingWrapper extends CustomEnchantment {
    public DebuffingWrapper(final String name, final int lvl, EEnchant enchant) {
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
                || EEnchant.ASSASSIN.equals(enchantment);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }


    @EventHandler
    public void onDebuffing(final DebuffingEvent e) {
        Messenger.debug("Debuffing called");

        if (!Utils.shouldTrigger(EEnchant.DEBUFFING)) {
            return;
        }

        Entity target = e.getEntityDamageByEntityEvent().getEntity();

        if (!(target instanceof Player targetPlayer)) {
            return;
        }

        if (targetPlayer.getHealth() > 8) {
            return;
        }

        Messenger.debug("Removing potion effects");

        targetPlayer.getActivePotionEffects().forEach(effect -> targetPlayer.removePotionEffect(effect.getType()));
    }
}