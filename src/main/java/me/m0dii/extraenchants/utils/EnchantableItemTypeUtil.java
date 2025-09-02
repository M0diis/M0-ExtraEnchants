package me.m0dii.extraenchants.utils;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class EnchantableItemTypeUtil {
    public static boolean isPickaxe(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("PICKAXE");
    }

    public static boolean isAxe(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("AXE")
                && !item.getType().name().toUpperCase().contains("PICKAXE");
    }

    public static boolean isShovel(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("SHOVEL");
    }

    public static boolean isHoe(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("HOE");
    }

    public static boolean isSword(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("SWORD");
    }

    public static boolean isHelmet(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("HELMET");
    }

    public static boolean isChestplate(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("CHESTPLATE");
    }

    public static boolean isLeggings(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("LEGGINGS");
    }

    public static boolean isBoots(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("BOOTS");
    }

    public static boolean isBow(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("BOW");
    }

    public static boolean isArmor(@NotNull ItemStack item) {
        return isHelmet(item) || isChestplate(item) || isLeggings(item) || isBoots(item);
    }

    public static boolean isTool(@NotNull ItemStack item) {
        return isTool(item, false);
    }

    public static boolean isTool(@NotNull ItemStack item, boolean includeHoe) {
        return isPickaxe(item) || isAxe(item) || isShovel(item)
                || isShears(item) || (includeHoe && isHoe(item));
    }

    public static boolean isWeapon(@NotNull ItemStack item) {
        return isSword(item) || isBow(item);
    }

    public static boolean isFishingRod(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("FISHING_ROD");
    }

    public static boolean isTrident(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("TRIDENT");
    }

    public static boolean isCrossbow(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("CROSSBOW");
    }

    public static boolean isShears(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("SHEARS");
    }

    public static boolean isShield(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("SHIELD");
    }

    public static boolean isElytra(@NotNull ItemStack item) {
        return item.getType().name().toUpperCase().contains("ELYTRA");
    }

    public static boolean canEnchantItemCustom(ItemStack item, ItemType type) {
        return switch (type) {
            case ALL -> true;
            case ARMOR -> isArmor(item);
            case SWORD -> isSword(item);
            case AXE -> isAxe(item);
            case PICKAXE -> isPickaxe(item);
            case SHOVEL -> isShovel(item);
            case HOE -> isHoe(item);
            case BOW -> isBow(item);
            case FISHING_ROD -> isFishingRod(item);
            case TRIDENT -> isTrident(item);
            case CROSSBOW -> isCrossbow(item);
            case SHEARS -> isShears(item);
            case SHIELD -> isShield(item);
            case ELYTRA -> isElytra(item);
            case TOOL -> isTool(item);
            case WEAPON -> isWeapon(item);
            case HELMET -> isHelmet(item);
            case CHESTPLATE -> isChestplate(item);
            case LEGGINGS -> isLeggings(item);
            case BOOTS -> isBoots(item);
        };
    }

    @Getter
    public enum ItemType {
        PICKAXE("PICKAXE", "PICKAXES"),
        AXE("AXE", "AXES"),
        SHOVEL("SHOVEL", "SHOVELS"),
        HOE("HOE", "HOES"),
        SWORD("SWORD", "SWORDS"),
        HELMET("HELMET", "HELMETS"),
        CHESTPLATE("CHESTPLATE", "CHESTPLATES"),
        LEGGINGS("LEGGINGS", "PANTS"),
        BOOTS("BOOTS", "SHOES"),
        BOW("BOW", "BOWS"),
        TRIDENT("TRIDENT", "TRIDENTS"),
        CROSSBOW("CROSSBOW", "CROSSBOWS"),
        FISHING_ROD("FISHING_ROD", "FISHING_RODS"),
        SHEARS("SHEARS"),
        ALL("ALL", "EVERYTHING", "ANY"),
        ARMOR("ARMOR", "ARMORS"),
        ELYTRA("ELYTRA"),
        WEAPON("WEAPON", "WEAPONS"),
        SHIELD("SHIELD", "SHIELDS"),
        TOOL("TOOL", "TOOLS");

        private final String[] names;

        ItemType(String... names) {
            this.names = names;
        }

        public static ItemType parse(String name) {
            return Arrays.stream(ItemType.values())
                    .filter(type -> Arrays.stream(type.getNames())
                            .anyMatch(n -> n.equalsIgnoreCase(name)
                                    || n.toUpperCase().contains(name.toUpperCase())))
                    .findFirst()
                    .orElse(null);
        }
    }
}
