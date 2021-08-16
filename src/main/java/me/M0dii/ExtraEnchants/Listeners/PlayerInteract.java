package me.M0dii.ExtraEnchants.Listeners;

import me.M0dii.ExtraEnchants.ExtraEnchants;
import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerInteract implements Listener
{
    private ExtraEnchants plugin;
    
    public PlayerInteract(ExtraEnchants plugin)
    {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onItemClick(PlayerInteractEvent e)
    {
        if(!e.hasItem())
            return;
        
        if(!e.getItem().hasItemMeta())
            return;
     
        if(!e.getItem().getType().equals(Material.ENCHANTED_BOOK))
            return;
    
        ItemMeta meta = e.getItem().getItemMeta();
        
        if((!meta.hasEnchant(CustomEnchants.TELEPATHY))
        && (!meta.hasEnchant(CustomEnchants.PLOW))
        && (!meta.hasEnchant(CustomEnchants.LAVA_WALKER))
        && (!meta.hasEnchant(CustomEnchants.BONDED))
        && (!meta.hasEnchant(CustomEnchants.SMELT))
        )
            return;
        
        e.getPlayer().sendMessage(this.plugin.format(e.getItem().getItemMeta().getDisplayName()));
        
        for(String l : meta.getLore())
            e.getPlayer().sendMessage(this.plugin.format(l));
    }
}