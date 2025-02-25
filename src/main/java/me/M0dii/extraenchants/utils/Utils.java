package me.m0dii.extraenchants.utils;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.regex.Pattern;

public class Utils {
    private static final Pattern HEX_PATTERN =
            Pattern.compile("&#([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])");

    private static final Random random = new Random();

    public static String format(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes(
                '&',
                HEX_PATTERN.matcher(text).replaceAll("&x&$1&$2&$3&$4&$5&$6")
        );
    }

    public static boolean shouldTrigger(EEnchant enchant) {
        if (enchant.isDisabled()) {
            return false;
        }

        if (enchant.getTriggerChance() == -1 || enchant.getTriggerChance() == 100) {
            return true;
        }

        int roll = random.nextInt(100);

        return enchant.getTriggerChance() > roll;
    }

    public static String stripColor(Component component) {
        return ChatColor.stripColor(PlainTextComponentSerializer.plainText().serializeOr(component, ""));
    }

    public static TextComponent colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        return Component.text(ChatColor.translateAlternateColorCodes('&', text));
    }

    public static boolean allowedAt(Player p, Location loc) {
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);

        if(res == null || p.hasPermission("residence.admin")) {
            return true;
        }

        return res.getPermissions().playerHas(p, "build", true)
                || res.getPermissions().getOwnerUUID().equals(p.getUniqueId());
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

    public static void copy(InputStream in, File file) {
        if (in == null) {
            return;
        }

        try {
            OutputStream out = new FileOutputStream(file);

            byte[] buf = new byte[1024];

            int len;

            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);

            out.close();
            in.close();
        } catch (Exception ex) {
            ExtraEnchants.getInstance().getLogger().warning("Error copying config file..");
        }
    }
}
