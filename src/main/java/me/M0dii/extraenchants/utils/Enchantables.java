package me.m0dii.extraenchants.utils;

import org.bukkit.inventory.ItemStack;

public class Enchantables {
    public static boolean isPickaxe(ItemStack item) {
        return item.getType().name().toUpperCase().contains("PICKAXE");
    }

    public static boolean isAxe(ItemStack item) {
        return item.getType().name().toUpperCase().contains("AXE")
           && !item.getType().name().toUpperCase().contains("PICKAXE");
    }

    public static boolean isShovel(ItemStack item) {
        return item.getType().name().toUpperCase().contains("SHOVEL");
    }

    public static boolean isHoe(ItemStack item) {
        return item.getType().name().toUpperCase().contains("HOE");
    }

    public static boolean isSword(ItemStack item) {
        return item.getType().name().toUpperCase().contains("SWORD");
    }

    public static boolean isHelmet(ItemStack item) {
        return item.getType().name().toUpperCase().contains("HELMET");
    }

    public static boolean isChestplate(ItemStack item) {
        return item.getType().name().toUpperCase().contains("CHESTPLATE");
    }

    public static boolean isLeggings(ItemStack item) {
        return item.getType().name().toUpperCase().contains("LEGGINGS");
    }

    public static boolean isBoots(ItemStack item) {
        return item.getType().name().toUpperCase().contains("BOOTS");
    }

    public static boolean isBow(ItemStack item) {
        return item.getType().name().toUpperCase().contains("BOW");
    }

    public static boolean isArmor(ItemStack item) {
        return isHelmet(item) || isChestplate(item) || isLeggings(item) || isBoots(item);
    }

    public static boolean isTool(ItemStack item) {
        return isTool(item, false);
    }

    public static boolean isTool(ItemStack item, boolean includeHoe) {
        return isPickaxe(item) || isAxe(item) || isShovel(item) || (includeHoe && isHoe(item));
    }
}
