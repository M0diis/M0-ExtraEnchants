package me.m0dii.extraenchants.utils;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class Enchantables {
    public static boolean isPickaxe(ItemStack item) {
        Messenger.debug(item.toString());

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

    public static boolean canEnchantItemCustom(ItemStack item, ItemType type) {
        switch (type) {
            case ALL:
                return true;
            case ARMOR:
                if (Enchantables.isArmor(item)) {
                    return true;
                }
                break;
            case SWORD:
                if (Enchantables.isSword(item)) {
                    return true;
                }
                break;
            case AXE:
                if (Enchantables.isAxe(item)) {
                    return true;
                }
                break;
            case PICKAXE:
                if (Enchantables.isPickaxe(item)) {
                    return true;
                }
                break;
            case SHOVEL:
                if (Enchantables.isShovel(item)) {
                    return true;
                }
                break;
            case HOE:
                if (Enchantables.isHoe(item)) {
                    return true;
                }
                break;
            case BOW:
                if (Enchantables.isBow(item)) {
                    return true;
                }
                break;
            case FISHING_ROD:
                if (Enchantables.isFishingRod(item)) {
                    return true;
                }
                break;
            case TRIDENT:
                if (Enchantables.isTrident(item)) {
                    return true;
                }
                break;
            case CROSSBOW:
                if (Enchantables.isCrossbow(item)) {
                    return true;
                }
                break;
            case SHEARS:
                if (Enchantables.isShears(item)) {
                    return true;
                }
                break;
            case SHIELD:
                if (Enchantables.isShield(item)) {
                    return true;
                }
                break;
            case ELYTRA:
                if (Enchantables.isElytra(item)) {
                    return true;
                }
                break;
            case TOOL:
                if (Enchantables.isTool(item)) {
                    return true;
                }
                break;
            case WEAPON:
                if (Enchantables.isWeapon(item)) {
                    return true;
                }
                break;
            case HELMET:
                if (Enchantables.isHelmet(item)) {
                    return true;
                }
                break;
            case CHESTPLATE:
                if (Enchantables.isChestplate(item)) {
                    return true;
                }
                break;
            case LEGGINGS:
                if (Enchantables.isLeggings(item)) {
                    return true;
                }
                break;
            case BOOTS:
                if (Enchantables.isBoots(item)) {
                    return true;
                }
                break;

            default:
                break;
        }

        return false;
    }

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
                            .anyMatch(n ->
                                    n.equalsIgnoreCase(name)
                                            || n.toUpperCase().contains(name.toUpperCase())
                            ))
                    .findFirst().orElse(null);
        }

        public String[] getNames() {
            return this.names;
        }


    }
}
