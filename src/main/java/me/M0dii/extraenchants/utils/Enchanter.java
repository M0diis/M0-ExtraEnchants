package me.m0dii.extraenchants.utils;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Enchanter {
    private static final ExtraEnchants plugin = ExtraEnchants.getPlugin(ExtraEnchants.class);
    private static final FileConfiguration cfg = plugin.getCfg();

    public static ItemStack getBook(String type, int level) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);

        Enchantment enchant = EEnchant.toEnchant(type);

        if(enchant == null) {
            return null;
        }

        Messenger.debug("Adding unsafe enchantment to item.");

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

        Messenger.debug("Current enchantments:");

        Set<Enchantment> enchants = item.getEnchantments().keySet();

        for(Enchantment ench : enchants)
        {
            Messenger.debug(ench.getKey().getKey());
        }

        return item;
    }

    public static void applyEnchant(ItemStack item, Enchantment enchant, int level, boolean onlyLore) {
        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.GRAY + PlainTextComponentSerializer.plainText().serialize(enchant.displayName(level)));

        ItemMeta meta = item.getItemMeta();

        if (meta.getLore() != null) {
            for (String l : meta.getLore()) {
                lore.add(Utils.format(l));
            }
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        if(!onlyLore) {
            item.addUnsafeEnchantment(enchant, level);
        }
    }
}