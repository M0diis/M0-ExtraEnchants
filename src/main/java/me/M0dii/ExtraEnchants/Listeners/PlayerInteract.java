package me.M0dii.ExtraEnchants.Listeners;

import me.M0dii.ExtraEnchants.ExtraEnchants;
import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteract implements org.bukkit.event.Listener
{
    private ExtraEnchants plugin;
    
    public PlayerInteract(ExtraEnchants plugin)
    {
        this.plugin = plugin;
    }
    
    @org.bukkit.event.EventHandler
    public void onItemClick(PlayerInteractEvent e)
    {
        if(!e.hasItem())
            return;
        
        if(!e.getItem().hasItemMeta())
            return;
     
        if(!e.getItem().getType().equals(Material.ENCHANTED_BOOK))
            return;
        
        if((!e.getItem().getItemMeta().hasEnchant(CustomEnchants.TELEPATHY))
        && (!e.getItem().getItemMeta().hasEnchant(CustomEnchants.PLOW))
        && (!e.getItem().getItemMeta().hasEnchant(CustomEnchants.SMELT))
        )
            return;
        
        e.getPlayer().sendMessage(this.plugin.format(e.getItem().getItemMeta().getDisplayName()));
        
        for(String l : e.getItem().getItemMeta().getLore())
        {
            e.getPlayer().sendMessage(this.plugin.format(l));
        }
    }
}