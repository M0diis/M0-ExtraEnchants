package me.M0dii.ExtraEnchants.Listeners;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import me.M0dii.ExtraEnchants.Events.CombineEvent;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryAction;
import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantmentCombine implements Listener
{
    @EventHandler
    public void onCombine(final InventoryClickEvent e)
    {
        ItemStack cursor = e.getCursor();
        
        if (cursor != null && !e.getCursor().hasItemMeta())
            return;
        
        if (cursor != null && !cursor.getType().equals(Material.ENCHANTED_BOOK))
            return;
        
        if (cursor != null && !cursor.getItemMeta().hasEnchants())
            return;
        
        ItemMeta meta = cursor.getItemMeta();
        
        if (!meta.hasEnchant(CustomEnchants.TELEPATHY)
        && !meta.hasEnchant(CustomEnchants.PLOW)
        && !meta.hasEnchant(CustomEnchants.LAVA_WALKER)
        && !meta.hasEnchant(CustomEnchants.BONDED)
        && !meta.hasEnchant(CustomEnchants.SMELT)
        )
            return;
        
        if (e.getAction() != InventoryAction.SWAP_WITH_CURSOR)
            return;
        
        Player p = (Player)e.getWhoClicked();
    
        combine(cursor, p, CustomEnchants.BEHEADING, "BEHEADING", e);
        combine(cursor, p, CustomEnchants.TELEPATHY, "TELEPATHY", e);
        combine(cursor, p, CustomEnchants.PLOW, "PLOW", e);
        combine(cursor, p, CustomEnchants.SMELT, "SMELT", e);
        combine(cursor, p, CustomEnchants.LAVA_WALKER, "LAVA_WALKER", e);
        combine(cursor, p, CustomEnchants.BONDED, "BONDED", e);
    }
    
    private void combine(ItemStack item, Player p, Enchantment ench, String name, InventoryClickEvent e)
    {
        if (item != null && item.getItemMeta().hasEnchant(ench))
            Bukkit.getPluginManager().callEvent(new CombineEvent(p, e, name));
    }
}