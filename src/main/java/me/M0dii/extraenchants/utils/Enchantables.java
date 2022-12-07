package me.m0dii.extraenchants.utils;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class Enchantables {
    public enum ItemType {
        PICKAXE("PICKAXE"),
        AXE("AXE"),
        SHOVEL("SHOVEL"),
        HOE("HOE"),
        SWORD("SWORD"),
        HELMET("HELMET"),
        CHESTPLATE("CHESTPLATE"),
        LEGGINGS("LEGGINGS"),
        BOOTS("BOOTS"),
        BOW("BOW"),
        TRIDENT("TRIDENT"),
        CROSSBOW("CROSSBOW"),
        FISHING_ROD("FISHING_ROD"),
        SHEARS("SHEARS"),
        ALL("ALL"),
        ARMOR("ARMOR"),
        ELYTRA("ELYTRA"),
        WEAPON("WEAPON"),
        SHIELD("SHIELD"),
        TOOL("TOOL");

        private final String name;

        ItemType(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static ItemType parse(String name) {
            return Arrays.stream(ItemType.values())
                    .filter(type -> type.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
        }
    }

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
        return isPickaxe(item) || isAxe(item) || isShovel(item)
            || isShears(item) || (includeHoe && isHoe(item));
    }

    public static boolean isWeapon(ItemStack item) {
        return isSword(item) || isBow(item);
    }

    public static boolean isFishingRod(ItemStack item) {
        return item.getType().name().toUpperCase().contains("FISHING_ROD");
    }

    public static boolean isTrident(ItemStack item) {
        return item.getType().name().toUpperCase().contains("TRIDENT");
    }

    public static boolean isCrossbow(ItemStack item) {
        return item.getType().name().toUpperCase().contains("CROSSBOW");
    }

    public static boolean isShears(ItemStack item) {
        return item.getType().name().toUpperCase().contains("SHEARS");
    }

    public static boolean isShield(ItemStack item) {
        return item.getType().name().toUpperCase().contains("SHIELD");
    }

    public static boolean isElytra(ItemStack item) {
        return item.getType().name().toUpperCase().contains("ELYTRA");
    }
}
