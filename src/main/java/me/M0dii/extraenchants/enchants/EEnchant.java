package me.m0dii.extraenchants.enchants;

import io.papermc.paper.enchantments.EnchantmentRarity;
import lombok.Getter;
import lombok.Setter;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum EEnchant {
    TELEPATHY,
    PLOW,
    EXCAVATOR,
    SMELT,
    BEHEADING,
    BONDED,
    LAVA_WALKER,
    HASTE_MINER,
    VEIN_MINER,
    ANTI_THORNS,
    EXPERIENCE_MINER,
    LIFESTEAL,
    ASSASSIN,
    REPLANTER,
    BERSERK,
    DISPOSER,
    TUNNEL,
    WITHERING,
    BARBARIAN,
    ARMOR_BREAKER,
    COLD_STEEL,
    BAT_VISION,
    TURTLE_SHELL,
    REINFORCED,
    GILLS,
    WEBBING,
    DEATH_SIPHON,
    TIMBER,
    STAT_TRACK,
    ESSENCE_DRAIN,
    DEBUFFING;

    private final ExtraEnchants instance = ExtraEnchants.getInstance();

    @Setter
    @Getter
    private Enchantment enchantment;
    @Setter
    @Getter
    private CustomEnchantment customEnchantment;

    public static EEnchant parse(String value) {
        return Arrays.stream(EEnchant.values())
                .filter(getFilterPredicate(value))
                .findFirst()
                .orElse(null);
    }


    public static Enchantment toEnchant(String value) {
        return Arrays.stream(EEnchant.values())
                .filter(getFilterPredicate(value))
                .map(EEnchant::getEnchantment)
                .findFirst()
                .orElse(null);
    }

    private static Predicate<EEnchant> getFilterPredicate(String value) {
        return enchant -> {
            String fromString = value.trim();

            return enchant.getDisplayName().equalsIgnoreCase(fromString)
                    || enchant.name().equalsIgnoreCase(fromString)
                    || enchant.getDisplayName().replace(" ", "").equalsIgnoreCase(fromString)
                    || enchant.getConfigName().equalsIgnoreCase(fromString)
                    || enchant.getConfigName().replace("_", "").equalsIgnoreCase(fromString)
                    || enchant.getConfigName().equalsIgnoreCase(fromString.replace(" ", ""));
        };
    }

    public static EEnchant fromEnchant(@NotNull Enchantment enchant) {
        return Arrays.stream(EEnchant.values())
                .filter(v -> v.getEnchantment().equals(enchant))
                .findFirst()
                .orElse(null);
    }

    public String getDisplayName() {
        if (!this.name().contains("_")) {
            return StringUtils.capitalize(this.name().toLowerCase());
        }

        String[] split = this.name().split("_");

        return Arrays.stream(split)
                .map(s -> StringUtils.capitalize(s.toLowerCase()))
                .map(String::trim)
                .collect(Collectors.joining(" "));
    }

    public String getConfigName() {
        return name().toLowerCase().replace("_", "");
    }

    public int getTriggerChance() {
        return instance.getCfg().getInt("enchants." + getConfigName() + ".trigger-chance", -1);
    }

    public boolean isDisabled() {
        return !instance.getCfg().getBoolean("enchants." + getConfigName() + ".enabled", true);
    }

    public int getEnchantChance() {
        return instance.getCfg().getInt("enchants." + getConfigName() + ".table-chance", -1);
    }

    public boolean isCursed() {
        return instance.getCfg().getBoolean("enchants." + getConfigName() + ".cursed", false);
    }

    public boolean isTreasure() {
        return instance.getCfg().getBoolean("enchants." + getConfigName() + ".treasure", false);
    }

    public int getDuration() {
        return instance.getCfg().getInt("enchants." + getConfigName() + ".extra.duration", 0);
    }

    public boolean isPlayerOnly() {
        return instance.getCfg().getBoolean("enchants." + getConfigName() + ".extra.player-only", false);
    }

    public EnchantmentRarity getRarity() {
        return EnchantmentRarity.valueOf(instance.getCfg().getString("enchants." + getConfigName() + ".rarity", "COMMON").toUpperCase());
    }

    public String getDisplayInLore(int level, boolean formatted) {
        String name = instance.getCfg().getString("enchants." + getConfigName() + ".enchanted-item-lore-name", getDisplayName())
                .replace("%level%", Utils.arabicToRoman(level));

        return formatted ? ChatColor.translateAlternateColorCodes('&', name) : name;
    }

    public boolean conflictsWith(@NotNull Enchantment enchant) {
        return this.enchantment.conflictsWith(enchant);
    }

    public boolean defaultConflictsEnabled() {
        return instance.getCfg().getBoolean("enchants." + getConfigName() + ".default-conflicts");
    }

    public boolean canEnchantItem(@Nullable ItemStack item) {
        if(item == null || item.getType().isAir()) {
            return false;
        }

        return this.customEnchantment.canEnchantItem(item);
    }

    public boolean canEnchantItemCustom(@Nullable ItemStack item) {
        if (getEnchantableTypes().isEmpty() || item == null || item.getType().isAir()) {
            return false;
        }

        return getEnchantableTypes().stream()
                .anyMatch(type -> EnchantableItemTypeUtil.canEnchantItemCustom(item, type));
    }

    public boolean equals(@Nullable Enchantment other) {
        return this.enchantment.equals(other);
    }

    public void enchant(@NotNull ItemStack item) {
        enchant(item, 1);
    }

    public void enchant(@NotNull ItemStack item, int level) {
        item.addUnsafeEnchantment(enchantment, level);
    }

    public List<EnchantableItemTypeUtil.ItemType> getEnchantableTypes() {
        return instance.getCfg().getStringList("enchants." + getConfigName() + ".enchantable-items")
                .stream()
                .map(EnchantableItemTypeUtil.ItemType::parse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Enchantment> getCustomConflicts() {
        return instance.getCfg().getStringList("enchants." + getConfigName() + ".conflicts")
                .stream()
                .map(s -> {
                    Enchantment byName = Enchantment.getByName(s.toUpperCase());

                    if (byName != null) {
                        return byName;
                    }

                    EEnchant parsed = EEnchant.parse(s);

                    if (parsed == null || parsed.getEnchantment() == null) {
                        return null;
                    }

                    return parsed.getEnchantment();
                })
                .toList();
    }

    public String getLore() {
        List<String> lore = instance.getCfg().getStringList(String.format("enchants.%s.lore", getConfigName()))
                .stream()
                .map(l -> l.replace("%level%", "<lygis>")
                        .replace("%duration%", getDuration() + "")
                        .replace("%trigger-chance%", getTriggerChance() + "%"))
                .map(Utils::format)
                .toList();

        return String.join("\n", lore);
    }
}