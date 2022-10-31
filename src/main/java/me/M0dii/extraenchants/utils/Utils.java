package me.m0dii.extraenchants.utils;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import me.m0dii.extraenchants.enchants.EEnchant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Random;

public class Utils {
    private static final Residence res = Residence.getInstance();
    private static final ResidenceManager rm = res.getResidenceManager();

    private static final Random random = new Random();

    public static String format(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static boolean shouldTrigger(EEnchant enchant) {
        if(enchant.isDisabled()) {
            return false;
        }

        if(enchant.getTriggerChance() == -1 || enchant.getTriggerChance() == 100) {
            return true;
        }

        return random.nextInt(100) <= enchant.getTriggerChance();
    }

    public static String stripColor(Component component) {
        return ChatColor.stripColor(PlainTextComponentSerializer.plainText().serializeOr(component, ""));
    }

    public static boolean allowed(Player p, Location loc) {
        if (res == null) {
            return true;
        }

        ClaimedResidence residence = rm.getByLoc(loc);

        if (residence == null) {
            return true;
        }

        ResidencePermissions perms = residence.getPermissions();

        return perms.playerHas(p, Flags.build, true)
                || residence.isOwner(p)
                || residence.isTrusted(p)
                || res.isResAdminOn(p);
    }

    public static String arabicToRoman(int level) {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(level);
        };
    }

    public static String romanToArabic(String roman) {
        return switch (roman) {
            case "I" -> "1";
            case "II" -> "2";
            case "III" -> "3";
            case "IV" -> "4";
            case "V" -> "5";
            case "VI" -> "6";
            case "VII" -> "7";
            case "VIII" -> "8";
            case "IX" -> "9";
            case "X" -> "10";
            default -> roman;
        };
    }
}
