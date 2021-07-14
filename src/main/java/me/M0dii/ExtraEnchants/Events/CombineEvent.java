package me.M0dii.ExtraEnchants.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CombineEvent extends Event implements org.bukkit.event.Cancellable
{
    private final Player player;
    private boolean isCancelled;
    private final InventoryClickEvent event;
    private final String enchant;
    
    public CombineEvent(Player p, InventoryClickEvent e, String enchant)
    {
        this.player = p;
        this.event = e;
        this.enchant = enchant;
    }
    
    public boolean isCancelled()
    {
        return this.isCancelled;
    }
    
    public void setCancelled(boolean isCancelled)
    {
        this.isCancelled = isCancelled;
    }
    
    private static final HandlerList HANDLERS = new HandlerList();
    
    public HandlerList getHandlers()
    {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList()
    {
        return HANDLERS;
    }
    
    public Player getPlayer()
    {
        return this.player;
    }
    
    public InventoryClickEvent breakEvent()
    {
        return this.event;
    }
    
    public String getEnchantString()
    {
        return this.enchant;
    }
}