package me.m0dii.extraenchants.enchants.wrappers;

import io.papermc.paper.enchantments.EnchantmentRarity;
import me.m0dii.extraenchants.utils.Enchantables;
import me.m0dii.extraenchants.utils.Utils;
import me.m0dii.extraenchants.utils.Wrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Wrapper(name = "Tunnel", maxLvl = 3)
public class TunnelWrapper extends Enchantment {
    private final String name;
    private final int maxLvl;

    public TunnelWrapper(final String name, final int lvl) {
        super(NamespacedKey.minecraft(name.toLowerCase().replace(" ", "_")));
        this.name = name;
        this.maxLvl = lvl;
    }

    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return Enchantables.isPickaxe(item) || Enchantables.isShovel(item);
    }

    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        return Enchantment.SILK_TOUCH.equals(enchantment);
    }

    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    public int getMaxLevel() {
        return this.maxLvl;
    }

    public @NotNull String getName() {
        return this.name;
    }

    public int getStartLevel() {
        return 0;
    }

    public boolean isCursed() {
        return false;
    }

    public boolean isTreasure() {
        return false;
    }

    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    public float getDamageIncrease(int value, @NotNull EntityCategory category) {
        return 0;
    }

    public boolean isDiscoverable() {
        return false;
    }

    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.VERY_RARE;
    }

    public boolean isTradeable() {
        return false;
    }

    public @NotNull Component displayName(int level) {
        return Component.text(name + " " + Utils.arabicToRoman(level));
    }

    @Override
    public @NotNull String translationKey() {
        return name.toLowerCase().replace(" ", "_");
    }
}
