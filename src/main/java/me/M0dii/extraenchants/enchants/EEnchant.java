package me.m0dii.extraenchants.enchants;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.utils.Messenger;
import org.apache.commons.lang.StringUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum EEnchant {
    TELEPATHY,
    PLOW,
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
    DEATH_SIPHON;

    private final ExtraEnchants instance = ExtraEnchants.getInstance();

    private Enchantment enchant;

    public String getDisplayName() {
        if(!this.name().contains("_")) {
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

    public Enchantment getEnchantment() {
        return this.enchant;
    }

    public void setEnchantment(Enchantment enchant) {
        this.enchant = enchant;
    }

    public int getTriggerChance() {
        return instance.getCfg().getInt("enchants." + getConfigName() + ".trigger-chance", -1);
    }

    public boolean isDisabled() {
        Messenger.debug("Is disabled, path: 'enchants." + getConfigName() + ".enabled'");
        return !instance.getCfg().getBoolean("enchants." + getConfigName() + ".enabled", true);
    }

    public int getEnchantChance() {
        return instance.getCfg().getInt("enchants." + getConfigName() + ".table-chance", -1);
    }


    public boolean conflictsWith(Enchantment enchant) {
        return this.enchant.conflictsWith(enchant);
    }

    public boolean canEnchantItem(ItemStack item) {
        return this.enchant.canEnchantItem(item);
    }

    public boolean equals(Enchantment other) {
        return this.enchant.equals(other);
    }

    public void enchant(ItemStack item) {
        enchant(item, 1);
    }

    public void enchant(ItemStack item, int level) {
        item.addUnsafeEnchantment(enchant, level);
    }

    public static EEnchant parse(String value) {
        return Arrays.stream(EEnchant.values())
                .filter(v -> v.getDisplayName().equalsIgnoreCase(value.trim())
                        || v.getDisplayName().replace(" ", "").equalsIgnoreCase(value.trim())
                        || v.getDisplayName().replace(" ", "_").equalsIgnoreCase(value.trim())
                        || v.getConfigName().equalsIgnoreCase(value.trim())
                )
                .findFirst()
                .orElse(null);
    }

    public static Enchantment toEnchant(String value) {
        return Arrays.stream(EEnchant.values())
                .filter(v -> v.getDisplayName().equalsIgnoreCase(value.trim())
                        || v.getDisplayName().replace(" ", "").equalsIgnoreCase(value.trim())
                        || v.getDisplayName().replace(" ", "_").equalsIgnoreCase(value.trim())
                        || v.getConfigName().equalsIgnoreCase(value.trim())
                )
                .map(EEnchant::getEnchantment)
                .findFirst()
                .orElse(null);
    }
}
