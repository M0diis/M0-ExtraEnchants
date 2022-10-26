package me.m0dii.extraenchants.enchants;

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
    TUNNEL("Tunnel");

    final String name;

    Enchantment enchant;

    EEnchant(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Enchantment getEnchant() {
        return this.enchant;
    }

    public void setEnchant(Enchantment enchant) {
        this.enchant = enchant;
    }

    public static EEnchant get(String value) {
        return Arrays.stream(EEnchant.values())
                .filter(v -> v.getName().equalsIgnoreCase(value) || v.getName().replace(" ", "").equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
