package me.m0dii.extraenchants.enchants;

import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.enchantments.Enchantment;

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
    TUNNEL("Tunnel");

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
        return instance.getCfg().getInt("enchants." + name().toLowerCase() + ".trigger-chance", -1);
    }

    public boolean isDisabled() {
        return !instance.getCfg().getBoolean("enchants." + name().toLowerCase() + ".enabled", true);
    }

    public int getEnchantChance() {
        return instance.getCfg().getInt("enchants." + name().toLowerCase() + ".table-chance", -1);
    }

    public static EEnchant get(String value) {
        return Arrays.stream(EEnchant.values())
                .filter(v -> v.getDisplayName().equalsIgnoreCase(value)
                     || v.getDisplayName().replace(" ", "").equalsIgnoreCase(value)
                     || v.getDisplayName().replace(" ", "_").equalsIgnoreCase(value)
                )
                .findFirst()
                .orElse(null);
    }
}
