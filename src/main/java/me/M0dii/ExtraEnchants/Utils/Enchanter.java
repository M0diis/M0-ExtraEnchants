package me.M0dii.ExtraEnchants.Utils;

import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import me.M0dii.ExtraEnchants.ExtraEnchants;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Enchanter {
    private static final ExtraEnchants plugin = ExtraEnchants.getPlugin(ExtraEnchants.class);
    private static final FileConfiguration cfg = plugin.getCfg();

    public static ItemStack getBook(String type, int level) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);

        Enchantment enchant = CustomEnchants.parse(type);

        if(enchant == null) {
            return null;
        }

        item.addUnsafeEnchantment(enchant, level);

        ItemMeta meta = item.getItemMeta();

        String name = type.toLowerCase();

        String displayName = cfg.getString(String.format("enchants.%s.displayname", name));

        if(displayName == null) {
            return item;
        }

        meta.setDisplayName(Utils.format(
                displayName.replace("%level%", Utils.arabicToRoman(level)))
        );

        List<String> lore = new ArrayList<>();

        for (String l : cfg.getStringList(String.format("enchants.%s.lore", name))) {
            lore.add(Utils.format(l.replace("%level%", Utils.arabicToRoman(level))));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

}