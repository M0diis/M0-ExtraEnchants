package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.utils.EnchantWrapper;
import me.m0dii.extraenchants.utils.Enchantables;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@EnchantWrapper(name = "Gills", maxLevel = 1)
public class GillsWrapper extends CustomEnchantment {

    public GillsWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return Enchantables.isHelmet(item) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HEAD);
    }
}
