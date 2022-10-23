package me.M0dii.ExtraEnchants.Enchants;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class RegisterEnchants {
    public static void register() {
        List<Enchantment> list = Arrays.stream(Enchantment.values()).toList();

        registerEnchantment(CustomEnchants.TELEPATHY, list);
        registerEnchantment(CustomEnchants.PLOW, list);
        registerEnchantment(CustomEnchants.SMELT, list);
        registerEnchantment(CustomEnchants.BEHEADING, list);
        registerEnchantment(CustomEnchants.LAVA_WALKER, list);
        registerEnchantment(CustomEnchants.BONDED, list);
        registerEnchantment(CustomEnchants.HASTE_MINER, list);
        registerEnchantment(CustomEnchants.VEIN_MINER, list);
        registerEnchantment(CustomEnchants.ANTI_THORNS, list);
        registerEnchantment(CustomEnchants.EXPERIENCE_MINER, list);
        registerEnchantment(CustomEnchants.LIFESTEAL, list);
    }

    private static void registerEnchantment(final Enchantment enchantment, List<Enchantment> list) {
        if (list.contains(enchantment)) {
            return;
        }

        try {
            final Field f = Enchantment.class.getDeclaredField("acceptingNew");

            f.setAccessible(true);
            f.set(null, true);

            Enchantment.registerEnchantment(enchantment);
        } catch (Exception e) {
            Bukkit.getLogger().severe("[M0-ExtraEnchants] Could not register enchantment: " + enchantment.getKey());
        }
    }
}
