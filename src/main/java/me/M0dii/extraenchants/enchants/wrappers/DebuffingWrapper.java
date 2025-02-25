package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.utils.EnchantWrapper;
import me.m0dii.extraenchants.utils.Enchantables;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@EnchantWrapper(name = "Debuffing", maxLevel = 1)
public class DebuffingWrapper extends CustomEnchantment {
    public DebuffingWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return Enchantables.isSword(item) || enchant.canEnchantItemCustom(item);
    }

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

    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }
}