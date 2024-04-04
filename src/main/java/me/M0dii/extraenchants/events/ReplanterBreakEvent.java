package me.m0dii.extraenchants.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class ReplanterBreakEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final BlockBreakEvent event;
    private final int enchantLevel;
    private boolean isCancelled;

    public ReplanterBreakEvent(Player p, BlockBreakEvent e, int enchantLevel) {
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

    public BlockBreakEvent getBreakEvent() {
        return this.event;
    }

    public int getEnchantLevel() {
        return this.enchantLevel;
    }
}