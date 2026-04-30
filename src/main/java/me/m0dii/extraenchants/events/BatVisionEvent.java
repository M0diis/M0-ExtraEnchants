package me.m0dii.extraenchants.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class BatVisionEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final PlayerMoveEvent playerMoveEvent;
    @Setter
    private boolean isCancelled;

    public BatVisionEvent(Player p, PlayerMoveEvent e) {
        this.player = p;
        this.playerMoveEvent = e;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}