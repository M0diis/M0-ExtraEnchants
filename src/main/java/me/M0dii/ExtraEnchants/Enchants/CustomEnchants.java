package me.M0dii.ExtraEnchants.Enchants;

import me.M0dii.ExtraEnchants.Enchants.Wrappers.*;
import org.bukkit.enchantments.Enchantment;

public class CustomEnchants {
    public static final Enchantment TELEPATHY =
            new TelepathyWrapper("Telepathy", 1);

    public static final Enchantment PLOW =
            new PlowWrapper("Plow", 2);

    public static final Enchantment SMELT =
            new SmeltWrapper("Smelt", 1);

    public static final Enchantment BEHEADING =
            new BeheadingWrapper("Beheading", 1);

    public static final Enchantment BONDED =
            new BondedWrapper("Bonded", 1);

    public static final Enchantment LAVA_WALKER =
            new LavaWalkerWrapper("Lava Walker", 1);

    public static final Enchantment HASTE_MINER =
            new HasteMinerWrapper("Haste Miner", 2);

    public static final Enchantment VEIN_MINER =
            new VeinMinerWrapper("Vein Miner", 2);

    public static final Enchantment ANTI_THORNS =
            new AntiThornsWrapper("Anti Thorns", 1);
    
    public static final Enchantment EXPERIENCE_MINER =
            new ExperienceMinerWrapper("Experience Miner", 1);

    public static final Enchantment LIFESTEAL =
            new LifestealWrapper("Lifesteal", 1);

    public static Enchantment parse(String en) {
        return switch (en.toLowerCase()) {
            case "telepathy" -> TELEPATHY;
            case "smelt" -> SMELT;
            case "plow" -> PLOW;
            case "beheading" -> BEHEADING;
            case "lifesteal" -> LIFESTEAL;
            case "lavawalker", "lava_walker" -> LAVA_WALKER;
            case "hasteminer", "haste_miner" -> HASTE_MINER;
            case "veinminer", "vein_miner" -> VEIN_MINER;
            case "antithorns", "anti_thorns" -> ANTI_THORNS;
            case "experienceminer", "experience_miner" -> EXPERIENCE_MINER;
            case "bonded" -> BONDED;
            default -> null;
        };
    }
}