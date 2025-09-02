package me.m0dii.extraenchants.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.m0dii.extraenchants.enchants.EEnchant;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public @Nonnull List<String> getPlaceholders() {
        return PLACEHOLDERS;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    @Nonnull
    public String getAuthor() {
        return "m0dii";
    }

    @Override
    @Nonnull
    public String getIdentifier() {
        return "extraenchants";
    }

    @Override
    @Nonnull
    public String getVersion() {
        return "1.0.0";
    }


    @Override
    public String onPlaceholderRequest(Player player, @Nonnull String id) {
        if (id.startsWith("lore_")) {
            String key = id.replace("lore_", "");

            EEnchant enchant = EEnchant.parse(key);

            if (enchant != null) {
                return enchant.getLore();
            }
        }

        if (id.startsWith("trigger_chance")) {
            String key = id.replace("trigger_chance_", "");

            EEnchant enchant = EEnchant.parse(key);

            if (enchant != null) {
                return enchant.getTriggerChance() + "%";
            }
        }

        if (id.startsWith("is_disabled")) {
            String key = id.replace("is_disabled_", "");

            EEnchant enchant = EEnchant.parse(key);

            if (enchant != null) {
                return enchant.isDisabled() ? "true" : "false";
            }
        }

        if (id.startsWith("is_cursed")) {
            String key = id.replace("is_cursed_", "");

            EEnchant enchant = EEnchant.parse(key);

            if (enchant != null) {
                return enchant.isCursed() ? "true" : "false";
            }
        }

        if (id.startsWith("is_treasure")) {
            String key = id.replace("is_treasure_", "");

            EEnchant enchant = EEnchant.parse(key);

            if (enchant != null) {
                return enchant.isTreasure() ? "true" : "false";
            }
        }

        if (id.startsWith("is_player_only")) {
            String key = id.replace("is_player_only_", "");

            EEnchant enchant = EEnchant.parse(key);

            if (enchant != null) {
                return enchant.isPlayerOnly() ? "true" : "false";
            }
        }

        if (id.startsWith("duration")) {
            String key = id.replace("duration_", "");

            EEnchant enchant = EEnchant.parse(key);

            if (enchant != null) {
                return String.valueOf(enchant.getDuration());
            }
        }

        if (id.startsWith("enchant_chance")) {
            String key = id.replace("enchant_chance_", "");

            EEnchant enchant = EEnchant.parse(key);

            if (enchant != null) {
                return String.valueOf(enchant.getEnchantChance());
            }
        }

        return null;
    }

    public static List<String> PLACEHOLDERS = Arrays.asList(
            "%extraenchants_lore_<enchant-key>%",
            "%extraenchants_trigger_chance_<enchant-key>%",
            "%extraenchants_is_disabled_<enchant-key>%",
            "%extraenchants_is_cursed_<enchant-key>%",
            "%extraenchants_is_treasure_<enchant-key>%",
            "%extraenchants_is_player_only_<enchant-key>%",
            "%extraenchants_duration_<enchant-key>%",
            "%extraenchants_enchant_chance_<enchant-key>%"
    );
}