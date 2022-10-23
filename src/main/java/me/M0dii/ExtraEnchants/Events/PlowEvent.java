package me.M0dii.ExtraEnchants.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class PlowEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final PlayerInteractEvent event;
    private final int enchantLevel;
    private boolean isCancelled;

    public PlowEvent(Player p, PlayerInteractEvent e, int enchantLevel) {
        this.player = p;
        this.event = e;
        this.enchantLevel = enchantLevel;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return this.player;
    }

    public PlayerInteractEvent getInteractEvent() {
        return this.event;
    }

    public int getEnchantLevel() {
        return this.enchantLevel;
    }
}