package me.m0dii.extraenchants.enchants.wrappers;

import io.papermc.paper.enchantments.EnchantmentRarity;
import me.m0dii.extraenchants.enchants.EEnchant;
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

@Wrapper(name = "Webbing", maxLvl = 1)
public class WebbingWrapper extends Enchantment {
    private final String name;
    private final int maxLvl;
    private final EEnchant enchant;

    public WebbingWrapper(final String name, final int lvl, EEnchant enchant) {
        super(NamespacedKey.minecraft(name.toLowerCase().replace(" ", "_")));
        this.name = name;
        this.maxLvl = lvl;

        this.enchant = enchant;
    }

    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return Enchantables.isSword(item) || enchant.canEnchantItemCustom(item);
    }

    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        if(enchant.getCustomConflicts().contains(enchantment)) {
            return true;
        }

        if(!enchant.defaultConflictsEnabled()) {
            return false;
        }

        return EEnchant.LIFESTEAL.equals(enchantment)
            || EEnchant.BERSERK.equals(enchantment)
            || EEnchant.BARBARIAN.equals(enchantment)
            || Enchantment.FIRE_ASPECT.equals(enchantment);
    }

    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
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
        return enchant.isCursed();
    }

    public boolean isTreasure() {
        return enchant.isTreasure();
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
        return enchant.getRarity();
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
