package me.m0dii.extraenchants.enchants;

import io.papermc.paper.enchantments.EnchantmentRarity;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityCategory;
import org.jetbrains.annotations.NotNull;

public abstract class CustomEnchantment extends Enchantment {
    protected final NamespacedKey key;

    protected final String name;
    protected final int maxLvl;
    protected final EEnchant enchant;

    public CustomEnchantment(final String name, final int lvl, EEnchant enchant) {
        this.name = name;
        this.maxLvl = lvl;
        this.enchant = enchant;

        this.key = new NamespacedKey(ExtraEnchants.getInstance(), name.toLowerCase().replace(" ", "_"));
    }

    @Override
    public @NotNull String translationKey() {
        return name.toLowerCase().replace(" ", "_");
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return enchant.getRarity();
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public @NotNull Component displayName(int level) {
        return Component.text(name + " " + Utils.arabicToRoman(level));
    }

    @Override
    public float getDamageIncrease(int value, @NotNull EntityCategory category) {
        return 0;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return this.maxLvl;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    @Override
    public boolean isCursed() {
        return enchant.isCursed();
    }

    @Override
    public boolean isTreasure() {
        return enchant.isTreasure();
    }

    @Override
    public int getMinModifiedCost(int level) {
        return 0;
    }

    @Override
    public int getMaxModifiedCost(int level) {
        return 1;
    }
}
