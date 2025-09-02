package me.m0dii.extraenchants.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class ArmorBreakerEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final EntityDamageEvent entityDamageEvent;
    private final int enchantLevel;
    private boolean isCancelled;

    public ArmorBreakerEvent(Player p, EntityDamageEvent e, int enchantLevel) {
        this.player = p;
        this.entityDamageEvent = e;
        this.enchantLevel = enchantLevel;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}