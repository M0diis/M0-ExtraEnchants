package me.M0dii.ExtraEnchants.Utils;

import me.M0dii.ExtraEnchants.ExtraEnchants;
import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Enchanter
{
    public static ItemStack getBook(String type)
    {
        ExtraEnchants plugin = ExtraEnchants.getPlugin(ExtraEnchants.class);

        FileConfiguration cfg = plugin.getCfg();
        
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        
        item.addUnsafeEnchantment(CustomEnchants.parse(type), 1);
        
        ItemMeta meta = item.getItemMeta();
        
        String name = type.toLowerCase();
        
        meta.setDisplayName(plugin.format(cfg.getString(
                String.format("enchants.%s.displayname", name))));
        
        List<String> lore = new ArrayList<>();
        
        for(String l : cfg.getStringList(String.format("enchants.%s.lore", name)))
        {
            lore.add(plugin.format(l));
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
}