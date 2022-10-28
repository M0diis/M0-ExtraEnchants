package me.m0dii.extraenchants.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class ReplanterEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final PlayerInteractEvent event;
    private boolean isCancelled;

    public ReplanterEvent(Player p, PlayerInteractEvent e) {
        this.player = p;
        this.event = e;
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

}