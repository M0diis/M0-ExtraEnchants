package me.m0dii.extraenchants.utils;

import com.jeff_media.morepersistentdatatypes.DataType;
import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class InventoryUtils {
    private static final NamespacedKey enchantKey = new NamespacedKey(ExtraEnchants.getInstance(), "extraenchants_enchant");

    private static final Random random = new Random();

    public static boolean hasEnchant(ItemStack item, CustomEnchantment enchant) {
        ItemMeta itemMeta = item.getItemMeta();

        if(itemMeta == null) {
            return false;
        }

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        Map<String, Integer> current = pdc.getOrDefault(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), new HashMap<>());

        return itemMeta.getEnchants().containsKey(enchant) || current.containsKey(enchant.translationKey());
    }

    public static boolean hasEnchant(ItemStack item, Enchantment enchant) {
        if (item == null) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();

        if(itemMeta == null) {
            return false;
        }

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        Map<String, Integer> current = pdc.getOrDefault(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), new HashMap<>());

        return itemMeta.getEnchants().containsKey(enchant) || item.getEnchantments().containsKey(enchant) || current.containsKey(enchant.getKey().getKey());
    }

    public static boolean hasEnchant(ItemStack item, EEnchant enchant) {
        if (item == null) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();

        if(itemMeta == null) {
            return false;
        }

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        Map<String, Integer> current = pdc.getOrDefault(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), new HashMap<>());

        return itemMeta.getEnchants().containsKey(enchant.getEnchantment()) || item.getEnchantments().containsKey(enchant.getEnchantment())
                || current.containsKey(enchant.getEnchantment().key().asString());
    }

    public static int getEnchantLevel(ItemStack item, EEnchant enchant) {
        if (item == null) {
            return 0;
        }

        ItemMeta itemMeta = item.getItemMeta();

        if(itemMeta == null) {
            return 0;
        }

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        Map<String, Integer> current = pdc.getOrDefault(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), new HashMap<>());

        if(current.containsKey(enchant.getEnchantment().key().asString())) {
            return current.get(enchant.getEnchantment().key().asString());
        }

        return itemMeta.getEnchants().getOrDefault(enchant.getEnchantment(), 0);
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

    public static void applyDurabilityChanced(Player player, ItemStack item, int chance) {
        if (item == null || item.getType().isAir()) {
            return;
        }

        if (random.nextInt(100) > chance) {
            return;
        }

        applyDurability(player, item);
    }

    public static void applyDurability(Player player, ItemStack item) {
        if (!(item.getItemMeta() instanceof Damageable damageable)) {
            return;
        }

        int unbreakingLevel = 0;

        if (InventoryUtils.hasEnchant(item, Enchantment.UNBREAKING)) {
            unbreakingLevel = item.getItemMeta().getEnchants().get(Enchantment.UNBREAKING);
        }

        int chance = (100) / (1 + unbreakingLevel);

        int res = random.nextInt(100 - 1) + 1;

        if (res < chance) {
            damageable.setDamage(damageable.getDamage() + 1);
        }

        if (damageable.getDamage() >= item.getType().getMaxDurability()) {
            if (player.getInventory().getItemInMainHand().equals(item)) {
                player.getInventory().setItemInMainHand(null);
            } else {
                player.getInventory().removeItem(item);
            }

            item.setType(Material.AIR);
            item.setAmount(0);
        }

        item.setItemMeta(damageable);
    }

    public static Map<String, Integer> getEnchantmentMapFromPDC(@Nonnull ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();

        if(itemMeta == null) {
            return new HashMap<>();
        }

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        return pdc.getOrDefault(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), new HashMap<>());
    }
}
