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
        switch (type) {
            case ALL -> {
                return true;
            }
            case ARMOR -> {
                if (EnchantableItemTypeUtil.isArmor(item)) {
                    return true;
                }
            }
            case SWORD -> {
                if (EnchantableItemTypeUtil.isSword(item)) {
                    return true;
                }
            }
            case AXE -> {
                if (EnchantableItemTypeUtil.isAxe(item)) {
                    return true;
                }
            }
            case PICKAXE -> {
                if (EnchantableItemTypeUtil.isPickaxe(item)) {
                    return true;
                }
            }
            case SHOVEL -> {
                if (EnchantableItemTypeUtil.isShovel(item)) {
                    return true;
                }
            }
            case HOE -> {
                if (EnchantableItemTypeUtil.isHoe(item)) {
                    return true;
                }
            }
            case BOW -> {
                if (EnchantableItemTypeUtil.isBow(item)) {
                    return true;
                }
            }
            case FISHING_ROD -> {
                if (EnchantableItemTypeUtil.isFishingRod(item)) {
                    return true;
                }
            }
            case TRIDENT -> {
                if (EnchantableItemTypeUtil.isTrident(item)) {
                    return true;
                }
            }
            case CROSSBOW -> {
                if (EnchantableItemTypeUtil.isCrossbow(item)) {
                    return true;
                }
            }
            case SHEARS -> {
                if (EnchantableItemTypeUtil.isShears(item)) {
                    return true;
                }
            }
            case SHIELD -> {
                if (EnchantableItemTypeUtil.isShield(item)) {
                    return true;
                }
            }
            case ELYTRA -> {
                if (EnchantableItemTypeUtil.isElytra(item)) {
                    return true;
                }
            }
            case TOOL -> {
                if (EnchantableItemTypeUtil.isTool(item)) {
                    return true;
                }
            }
            case WEAPON -> {
                if (EnchantableItemTypeUtil.isWeapon(item)) {
                    return true;
                }
            }
            case HELMET -> {
                if (EnchantableItemTypeUtil.isHelmet(item)) {
                    return true;
                }
            }
            case CHESTPLATE -> {
                if (EnchantableItemTypeUtil.isChestplate(item)) {
                    return true;
                }
            }
            case LEGGINGS -> {
                if (EnchantableItemTypeUtil.isLeggings(item)) {
                    return true;
                }
            }
            case BOOTS -> {
                if (EnchantableItemTypeUtil.isBoots(item)) {
                    return true;
                }
            }
            default -> {
            }
        }

        return false;
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
