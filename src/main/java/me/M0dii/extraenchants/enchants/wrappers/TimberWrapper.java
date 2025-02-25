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

@EnchantWrapper(name = "Timber", maxLevel = 1)
public class TimberWrapper extends CustomEnchantment {
    public TimberWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return Enchantables.isAxe(item) || enchant.canEnchantItemCustom(item);
    }

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

    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    @Override
    public @NotNull String translationKey() {
        return name.toLowerCase();
    }
}