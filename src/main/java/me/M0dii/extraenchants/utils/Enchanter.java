package me.m0dii.extraenchants.utils;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Enchanter {
    private static final ExtraEnchants plugin = ExtraEnchants.getInstance();
    private static final FileConfiguration cfg = plugin.getCfg();

    public static ItemStack getBook(String type, int level) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);

        EEnchant enchant = EEnchant.parse(type);

        if(enchant == null) {
            return null;
        }

        Enchantment enchantment = enchant.getEnchantment();

        item.addUnsafeEnchantment(enchantment, level);

        ItemMeta meta = item.getItemMeta();

        String name = type.toLowerCase();

        String displayName = cfg.getString(String.format("enchants.%s.book-display-name", name));

        if(displayName == null) {
            return item;
        }

        meta.setDisplayName(Utils.format(
            displayName.replace("%level%", Utils.arabicToRoman(level)))
        );

        List<String> lore = new ArrayList<>();

        for (String l : cfg.getStringList(String.format("enchants.%s.lore", name))) {
            String line = l.replace("%level%", Utils.arabicToRoman(level))
                           .replace("%duration%", enchant.getDuration() + "")
                           .replace("%trigger-chance%", enchant.getTriggerChance() + "%");

            lore.add(Utils.format(line));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    public static void applyEnchant(ItemStack item, EEnchant enchant, int level, boolean onlyLore) {
        List<String> lore = new ArrayList<>();

        lore.add(enchant.getDisplayInLore(level, true));

        ItemMeta meta = item.getItemMeta();

        if (meta.getLore() != null) {
            for (String l : meta.getLore()) {
                lore.add(Utils.format(l));
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        if(!onlyLore) {
            item.addUnsafeEnchantment(enchant.getEnchantment(), level);
        }
    }

    public static void applyEnchant(ItemStack item, Enchantment enchant, int level) {
        item.addUnsafeEnchantment(enchant, level);
    }
}