package me.m0dii.extraenchants.utils;

import me.m0dii.extraenchants.enchants.EEnchant;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class InventoryUtils {
    private static final Random random = new Random();

    public static boolean hasEnchant(ItemStack item, Enchantment enchant) {
        ItemMeta itemMeta = item.getItemMeta();

        return itemMeta != null && itemMeta.getEnchants().containsKey(enchant);
    }

    public static boolean hasEnchant(ItemStack item, EEnchant enchant) {
        if(item == null) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();

        return itemMeta != null && itemMeta.getEnchants().containsKey(enchant.getEnchantment());
    }

    public static void applyDurability(ItemStack hand) {
        if(!(hand.getItemMeta() instanceof Damageable damageable)) {
            return;
        }

        int unbreakingLevel = 0;

        if (InventoryUtils.hasEnchant(hand, Enchantment.DURABILITY)) {
            unbreakingLevel = hand.getItemMeta().getEnchants().get(Enchantment.DURABILITY);
        }

        int chance = (100) / (1 + unbreakingLevel);

        int res = random.nextInt(100 - 1) + 1;

        if (res < chance) {
            damageable.setDamage(damageable.getDamage() + 1);
        }

        hand.setItemMeta(damageable);

        if (damageable.getDamage() >= hand.getType().getMaxDurability()) {
            hand.setType(Material.AIR);
        }
    }
}
