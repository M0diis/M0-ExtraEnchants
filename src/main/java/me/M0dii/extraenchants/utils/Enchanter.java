package me.m0dii.extraenchants.utils;

import com.jeff_media.morepersistentdatatypes.DataType;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Enchanter {
    private static final NamespacedKey enchantKey = new NamespacedKey(ExtraEnchants.getInstance(), "extraenchants_enchant");

    private static final ExtraEnchants plugin = ExtraEnchants.getInstance();
    private static final FileConfiguration cfg = plugin.getCfg();

    public static ItemStack getBook(@NotNull String type, int level) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);

        EEnchant enchant = EEnchant.parse(type);

        if (enchant == null) {
            return null;
        }

        Enchantment enchantment = enchant.getEnchantment();

        item.addUnsafeEnchantment(enchantment, level);

        ItemMeta meta = item.getItemMeta();

        String name = type.toLowerCase();

        String displayName = cfg.getString(String.format("enchants.%s.book-display-name", name));

        if (displayName == null) {
            return item;
        }

        meta.displayName(Utils.colorize(displayName.replace("%level%", Utils.arabicToRoman(level))));

        List<Component> lore = new ArrayList<>();

        for (String l : cfg.getStringList(String.format("enchants.%s.lore", name))) {
            String line = l.replace("%level%", Utils.arabicToRoman(level))
                    .replace("%duration%", enchant.getDuration() + "")
                    .replace("%trigger-chance%", enchant.getTriggerChance() + "%");

            lore.add(Utils.colorize(line));
        }

        meta.lore(lore);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        Map<String, Integer> current = pdc.getOrDefault(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), new HashMap<>());

        Map<String, Integer> map = Map.of(enchant.getEnchantment().key().asString(), level);

        current.putAll(map);

        pdc.set(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), current);

        item.setItemMeta(meta);

        return item;
    }

    public static void applyEnchant(@NotNull ItemStack item, @NotNull EEnchant enchant, int level, boolean onlyLore) {
        ItemMeta meta = item.getItemMeta();

        if (!onlyLore) {
            meta.addEnchant(enchant.getEnchantment(), level, true);
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        Map<String, Integer> current = pdc.getOrDefault(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), new HashMap<>());

        Map<String, Integer> map = Map.of(enchant.getEnchantment().key().asString(), level);

        current.putAll(map);

        pdc.set(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), current);

        item.setItemMeta(meta);
    }

    public static void applyEnchantWithoutLore(@NotNull ItemStack item, @NotNull EEnchant enchant, int level) {
        ItemMeta meta = item.getItemMeta();

        meta.addEnchant(enchant.getEnchantment(), level, true);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        Map<String, Integer> current = pdc.getOrDefault(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), new HashMap<>());

        Map<String, Integer> map = Map.of(enchant.getEnchantment().key().asString(), level);

        current.putAll(map);

        pdc.set(enchantKey, DataType.asMap(DataType.STRING, DataType.INTEGER), current);

        item.setItemMeta(meta);
    }

    public static void addUnsafe(@NotNull ItemStack item, @NotNull Enchantment enchant, int level) {
        item.addUnsafeEnchantment(enchant, level);
    }
}