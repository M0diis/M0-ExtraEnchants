package me.m0dii.extraenchants.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Random;

public class InventoryUtils {
    private static final Random random = new Random();
    public static boolean hasUnbreaking(ItemStack item) {
        if(item.getItemMeta() == null) {
            return false;
        }

        return item.getItemMeta().getEnchants()
                .containsKey(Enchantment.DURABILITY);
    }

    public static void applyDurability(ItemStack hand, Damageable itemDam) {
        int unbreakingLevel = 0;

        if (InventoryUtils.hasUnbreaking(hand)) {
            unbreakingLevel = hand.getItemMeta().getEnchants().get(Enchantment.DURABILITY);
        }

        int chance = (100) / (1 + unbreakingLevel);

        int res = random.nextInt(100 - 1) + 1;

        if (res < chance) {
            itemDam.setDamage(itemDam.getDamage() + 1);
        }

        hand.setItemMeta(itemDam);
    }
}
