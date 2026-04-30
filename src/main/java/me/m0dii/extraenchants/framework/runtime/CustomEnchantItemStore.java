package me.m0dii.extraenchants.framework.runtime;

import com.jeff_media.morepersistentdatatypes.DataType;
import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.HashMap;
import java.util.Map;

public class CustomEnchantItemStore {
    private final NamespacedKey enchantKey;

    public CustomEnchantItemStore(ExtraEnchants plugin) {
        this.enchantKey = new NamespacedKey(plugin, "extraenchants_enchant");
    }

    public Map<String, Integer> getEnchantMap(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return new HashMap<>();
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return new HashMap<>();
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), new HashMap<>());
    }

    public int getLevel(ItemStack item, String enchantId) {
        if (enchantId == null) {
            return 0;
        }

        return getEnchantMap(item).getOrDefault(namespaced(enchantId), 0);
    }

    public void setLevel(ItemStack item, String enchantId, int level) {
        if (item == null || item.getType().isAir() || enchantId == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Map<String, Integer> map = pdc.getOrDefault(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), new HashMap<>());
        map.put(namespaced(enchantId), level);
        pdc.set(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), map);
        item.setItemMeta(meta);
    }

    public void removeLevel(ItemStack item, String enchantId) {
        if (item == null || item.getType().isAir() || enchantId == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Map<String, Integer> map = pdc.getOrDefault(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), new HashMap<>());
        map.remove(namespaced(enchantId));
        pdc.set(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), map);
        item.setItemMeta(meta);
    }

    public static String namespaced(String id) {
        return id.contains(":") ? id : "custom:" + id.toLowerCase();
    }
}

