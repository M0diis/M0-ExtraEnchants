package me.m0dii.extraenchants.enchants;

import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public enum EEnchant {
    TELEPATHY("Telepathy"),
    PLOW("Plow"),
    SMELT("Smelt"),
    BEHEADING("Beheading"),
    BONDED("Bonded"),
    LAVA_WALKER("Lava Walker"),
    HASTE_MINER("Haste Miner"),
    VEIN_MINER("Vein Miner"),
    ANTI_THORNS("Anti Thorns"),
    EXPERIENCE_MINER("Experience Miner"),
    LIFESTEAL("Lifesteal"),
    ASSASSIN("Assassin"),
    REPLANTER("Replanter"),
    BERSERK("Berserk"),
    DISPOSER("Disposer"),
    TUNNEL("Tunnel"),
    COLD_STEEL("Cold Steel");

    private final ExtraEnchants instance = ExtraEnchants.getInstance();

    final String displayName;

    Enchantment enchant;

    EEnchant(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
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
        return !instance.getCfg().getBoolean("enchants." + getConfigName() + ".enabled", true);
    }

    public int getEnchantChance() {
        return instance.getCfg().getInt("enchants." + getConfigName() + ".table-chance", -1);
    }

    public String getConfigName() {
        return name().toLowerCase().replace("_", "");
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
                .filter(v -> v.getDisplayName().equalsIgnoreCase(value)
                        || v.getDisplayName().replace(" ", "").equalsIgnoreCase(value)
                        || v.getDisplayName().replace(" ", "_").equalsIgnoreCase(value)
                )
                .findFirst()
                .orElse(null);
    }

    public static Enchantment toEnchant(String value) {
        return Arrays.stream(EEnchant.values())
                .filter(v -> v.getDisplayName().equalsIgnoreCase(value)
                        || v.getDisplayName().replace(" ", "").equalsIgnoreCase(value)
                        || v.getDisplayName().replace(" ", "_").equalsIgnoreCase(value)
                )
                .map(EEnchant::getEnchantment)
                .findFirst()
                .orElse(null);
    }
}
