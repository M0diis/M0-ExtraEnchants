package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.utils.EnchantWrapper;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@EnchantWrapper(name = "Bonded", maxLvl = 1)
public class BondedWrapper extends CustomEnchantment {
    public BondedWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return enchant.canEnchantItemCustom(item);
    }

    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        return enchant.getCustomConflicts().contains(enchantment);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ALL;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.values());
    }
}
