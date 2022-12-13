package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.utils.InventoryUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class AnvilCombine implements Listener {
    private final ExtraEnchants plugin;

    public AnvilCombine(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemPrepare(final PrepareAnvilEvent event) {
        ItemStack srcItem = event.getInventory().getItem(0);
        ItemStack result = event.getResult();

        if (srcItem == null || result == null) {
            return;
        }

        if(InventoryUtils.hasEnchant(srcItem, EEnchant.ANTI_THORNS.getEnchantment())
        || InventoryUtils.hasEnchant(result, EEnchant.ANTI_THORNS.getEnchantment())) {
            event.setResult(null);
        }
    }

    public static void removeFromItem(ItemStack item, Enchantment enchantment, int level) {
        if (!item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();

            if(lore == null) {
                return;
            }

            for(int i = 0; i < lore.size(); i++) {
                String line = lore.get(i);
                if (line.contains(enchantment.getName())) {
                    lore.remove(i);
                    break;
                }
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        item.removeEnchantment(enchantment);
    }
}