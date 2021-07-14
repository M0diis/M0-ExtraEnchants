package me.M0dii.ExtraEnchants.Listeners;

import org.bukkit.event.EventHandler;
import me.M0dii.ExtraEnchants.Events.CombineEvent;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryAction;
import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.ItemMeta;

public class EnchantmentCombine implements Listener
{
    @EventHandler
    public void onCombine(final InventoryClickEvent e)
    {
        if (e.getCursor() != null && !e.getCursor().hasItemMeta())
            return;
        
        if (!e.getCursor().getType().equals(Material.ENCHANTED_BOOK))
            return;
        
        if (!e.getCursor().getItemMeta().hasEnchants())
            return;
        
        ItemMeta meta = e.getCursor().getItemMeta();
        
        if (!meta.hasEnchant(CustomEnchants.TELEPATHY)
        && !meta.hasEnchant(CustomEnchants.PLOW)
        && !meta.hasEnchant(CustomEnchants.SMELT)
        )
            return;
        
        if (e.getAction() != InventoryAction.SWAP_WITH_CURSOR)
            return;
        
        Player p = (Player)e.getWhoClicked();
        
        if (e.getCursor().getItemMeta().hasEnchant(CustomEnchants.TELEPATHY))
            Bukkit.getPluginManager().callEvent(new CombineEvent(p, e, "TELEPATHY"));
        else if (e.getCursor().getItemMeta().hasEnchant(CustomEnchants.PLOW))
            Bukkit.getPluginManager().callEvent(new CombineEvent(p, e, "PLOW"));
        else if (e.getCursor().getItemMeta().hasEnchant(CustomEnchants.SMELT))
            Bukkit.getPluginManager().callEvent(new CombineEvent(p, e, "SMELT"));
        
    }
}