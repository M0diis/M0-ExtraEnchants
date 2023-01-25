package me.m0dii.extraenchants.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;

public class BerserkEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final EntityDamageByEntityEvent event;
    private boolean isCancelled;

    public BerserkEvent(Player p, EntityDamageByEntityEvent e) {
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

    public EntityDamageByEntityEvent getEntityDamageEvent() {
        return this.event;
    }
}