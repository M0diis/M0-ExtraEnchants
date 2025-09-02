package me.m0dii.extraenchants.enchants;

import io.papermc.paper.enchantments.EnchantmentRarity;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@SuppressWarnings("all")
public abstract class CustomEnchantment extends Enchantment implements Listener {
    protected final NamespacedKey key;

    protected final String name;
    protected final int maxLevel;
    protected final EEnchant enchant;

    public CustomEnchantment(final String name, final int maxLevel, EEnchant enchant) {
        this.name = name;
        this.maxLevel = maxLevel;
        this.enchant = enchant;

        this.key = new NamespacedKey(ExtraEnchants.getInstance(), name.toLowerCase().replace(" ", "_"));
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return enchant.canEnchantItemCustom(item);
    }

    @Override
    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        return enchant.getCustomConflicts().contains(enchantment);
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
    public float getDamageIncrease(int i, @NotNull EntityType entityType) {
        return 0;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
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

    @SuppressWarnings("removal")
    @Override
    public @NotNull String getTranslationKey() {
        return name.toLowerCase().replace(" ", "_");
    }

    @Override
    public int getAnvilCost() {
        return 0;
    }

    @Override
    public @NotNull Set<EquipmentSlotGroup> getActiveSlotGroups() {
        return Set.of(EquipmentSlotGroup.ANY);
    }

    @Override
    public @NotNull Component description() {
        return null;
    }

    @Override
    public @NotNull RegistryKeySet<ItemType> getSupportedItems() {
        return RegistrySet.keySet(RegistryKey.ITEM);
    }

    @Override
    public @Nullable RegistryKeySet<ItemType> getPrimaryItems() {
        return null;
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public @NotNull RegistryKeySet<Enchantment> getExclusiveWith() {
        return RegistrySet.keySet(RegistryKey.ENCHANTMENT);
    }
}
