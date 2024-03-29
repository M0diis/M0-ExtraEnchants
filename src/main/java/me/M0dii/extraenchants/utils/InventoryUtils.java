package me.m0dii.extraenchants.utils;

import me.m0dii.extraenchants.enchants.EEnchant;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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
        if (item == null) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();

        return (itemMeta != null &&
                itemMeta.getEnchants().containsKey(enchant.getEnchantment())) ||
                item.getEnchantments().containsKey(enchant.getEnchantment());
    }

    public static int getEnchantLevel(ItemStack item, EEnchant enchant) {
        if (item == null) {
            return 0;
        }

        return item.getEnchantmentLevel(enchant.getEnchantment());
    }

    public static int getEnchantLevelHand(Player p, EEnchant enchant) {
        if (p == null) {
            return 0;
        }

        ItemStack item = p.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            return 0;
        }

        return getEnchantLevel(item, enchant);
    }

    public static void applyDurabilityChanced(ItemStack item, int chance) {
        if (item == null || item.getType().isAir()) {
            return;
        }

        if (random.nextInt(100) > chance) {
            return;
        }

        applyDurability(item);
    }

    public static void applyDurability(ItemStack item) {
        if (!(item.getItemMeta() instanceof Damageable damageable)) {
            return;
        }

        int unbreakingLevel = 0;

        if (InventoryUtils.hasEnchant(item, Enchantment.DURABILITY)) {
            unbreakingLevel = item.getItemMeta().getEnchants().get(Enchantment.DURABILITY);
        }

        int chance = (100) / (1 + unbreakingLevel);

        int res = random.nextInt(100 - 1) + 1;

        if (res < chance) {
            damageable.setDamage(damageable.getDamage() + 1);
        }

        item.setItemMeta(damageable);

        if (damageable.getDamage() >= item.getType().getMaxDurability()) {
            item.setType(Material.AIR);
        }
    }
}
