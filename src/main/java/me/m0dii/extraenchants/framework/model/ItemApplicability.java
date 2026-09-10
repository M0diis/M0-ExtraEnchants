package me.m0dii.extraenchants.framework.model;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.lang.reflect.Field;

/**
 * Maps the human-readable values used by custom-enchant YAML files to Paper's
 * item type tags. The enum name is the canonical YAML value.
 */
public enum ItemApplicability {
    SWORD(tag("SWORDS"), "SWORDS"),
    AXE(tag("AXES"), "AXES"),
    PICKAXE(tag("PICKAXES"), "PICKAXES"),
    SHOVEL(tag("SHOVELS"), "SHOVELS"),
    HOE(tag("HOES"), "HOES"),
    HELMET(tag("HEAD_ARMOR"), "HEAD_ARMOR", "HEAD"),
    CHESTPLATE(tag("CHEST_ARMOR"), "CHEST_ARMOR", "CHEST"),
    LEGGINGS(tag("LEG_ARMOR"), "LEG_ARMOR", "LEGS"),
    BOOTS(tag("FOOT_ARMOR"), "FOOT_ARMOR", "FEET"),
    BOW(tag("ENCHANTABLE_BOW")),
    CROSSBOW(tag("ENCHANTABLE_CROSSBOW")),
    FISHING_ROD(tag("ENCHANTABLE_FISHING"), "FISHING", "FISHINGROD"),
    TRIDENT(tag("ENCHANTABLE_TRIDENT")),
    MACE(tag("ENCHANTABLE_MACE")),
    SPEAR(tag("SPEARS"), "SPEARS"),
    ARMOR(tag("ENCHANTABLE_ARMOR")),
    WEAPON(tag("ENCHANTABLE_WEAPON")),
    MELEE_WEAPON(tag("ENCHANTABLE_MELEE_WEAPON"), "MELEE_WEAPON", "MELEEWEAPON"),
    MINING(tag("ENCHANTABLE_MINING")),
    DURABILITY(tag("ENCHANTABLE_DURABILITY"));

    private static final Map<String, ItemApplicability> LOOKUP;

    static {
        Map<String, ItemApplicability> lookup = new LinkedHashMap<>();
        for (ItemApplicability applicability : values()) {
            lookup.put(normalize(applicability.name()), applicability);
            for (String alias : applicability.aliases) {
                lookup.put(normalize(alias), applicability);
            }
        }
        LOOKUP = Collections.unmodifiableMap(lookup);
    }

    private final TagKey<ItemType> tagKey;
    private final Set<String> aliases;

    ItemApplicability(TagKey<ItemType> tagKey, String... aliases) {
        this.tagKey = tagKey;
        this.aliases = Arrays.stream(aliases)
                .map(ItemApplicability::normalize)
                .collect(Collectors.toUnmodifiableSet());
    }

    public TagKey<ItemType> getTagKey() {
        return tagKey;
    }

    public static Optional<ItemApplicability> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(LOOKUP.get(normalize(raw)));
    }

    public static Set<String> acceptedNames() {
        return LOOKUP.keySet();
    }

    /** Matches the runtime item category used by commands and trigger dispatch. */
    public boolean matches(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        return matches(item.getType());
    }

    /**
     * Matches without constructing an ItemStack. Bootstrap registry events run
     * before item components are bound, so this overload is also safe for
     * compatibility fallbacks on an older Paper server.
     */
    public boolean matches(Material material) {
        if (material == null || material.isAir()) {
            return false;
        }

        if (this == DURABILITY) {
            return material.getMaxDurability() > 0;
        }

        return matchesName(material.name());
    }

    /** Matches a registry field name without touching Bukkit's bound server. */
    public boolean matchesName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return false;
        }

        String name = rawName.toUpperCase(Locale.ROOT);
        return switch (this) {
            case SWORD -> isSword(name);
            case AXE -> isAxe(name);
            case PICKAXE -> isPickaxe(name);
            case SHOVEL -> isShovel(name);
            case HOE -> isHoe(name);
            case HELMET -> isHelmet(name);
            case CHESTPLATE -> isChestplate(name);
            case LEGGINGS -> isLeggings(name);
            case BOOTS -> isBoots(name);
            case BOW -> isBow(name);
            case CROSSBOW -> isCrossbow(name);
            case FISHING_ROD -> isFishingRod(name);
            case TRIDENT -> isTrident(name);
            case MACE -> name.equals("MACE");
            case SPEAR -> name.contains("SPEAR");
            case ARMOR -> isArmor(name);
            case WEAPON -> isWeapon(name);
            case MELEE_WEAPON -> isMeleeWeapon(name);
            case MINING -> isMining(name);
            case DURABILITY -> true;
        };
    }

    private static boolean isSword(String name) {
        return name.endsWith("_SWORD");
    }

    private static boolean isAxe(String name) {
        return name.contains("AXE") && !isPickaxe(name);
    }

    private static boolean isPickaxe(String name) {
        return name.contains("PICKAXE");
    }

    private static boolean isShovel(String name) {
        return name.contains("SHOVEL");
    }

    private static boolean isHoe(String name) {
        return name.contains("HOE");
    }

    private static boolean isHelmet(String name) {
        return name.endsWith("_HELMET");
    }

    private static boolean isChestplate(String name) {
        return name.endsWith("_CHESTPLATE");
    }

    private static boolean isLeggings(String name) {
        return name.endsWith("_LEGGINGS");
    }

    private static boolean isBoots(String name) {
        return name.endsWith("_BOOTS");
    }

    private static boolean isBow(String name) {
        return name.equals("BOW") || name.endsWith("_BOW");
    }

    private static boolean isCrossbow(String name) {
        return name.contains("CROSSBOW");
    }

    private static boolean isFishingRod(String name) {
        return name.contains("FISHING_ROD");
    }

    private static boolean isTrident(String name) {
        return name.contains("TRIDENT");
    }

    private static boolean isArmor(String name) {
        return isHelmet(name) || isChestplate(name) || isLeggings(name) || isBoots(name);
    }

    private static boolean isWeapon(String name) {
        return isSword(name) || isAxe(name) || isBow(name) || isCrossbow(name)
                || isTrident(name) || name.equals("MACE") || name.contains("SPEAR");
    }

    private static boolean isMeleeWeapon(String name) {
        return isSword(name) || isAxe(name) || isTrident(name)
                || name.equals("MACE") || name.contains("SPEAR");
    }

    private static boolean isMining(String name) {
        return isPickaxe(name) || isAxe(name) || isShovel(name) || isHoe(name);
    }

    public static boolean matches(ItemStack item, Iterable<String> categories) {
        if (categories == null) {
            return false;
        }

        for (String category : categories) {
            if (parse(category).filter(value -> value.matches(item)).isPresent()) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String raw) {
        return raw.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
    }

    @SuppressWarnings("unchecked")
    private static TagKey<ItemType> tag(String fieldName) {
        try {
            Field field = ItemTypeTagKeys.class.getField(fieldName);
            return (TagKey<ItemType>) field.get(null);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Older MockBukkit/Paper test fixtures do not know newer 26.3 tags.
            // The production 26.3 runtime resolves every tag above.
            return null;
        }
    }
}
