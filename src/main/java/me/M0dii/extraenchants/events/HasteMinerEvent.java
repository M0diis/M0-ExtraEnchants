package me.m0dii.extraenchants.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class HasteMinerEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final BlockBreakEvent event;
    private final Block block;
    private boolean isCancelled;

    public HasteMinerEvent(Player p, BlockBreakEvent e) {
        this.player = p;
        this.event = e;
        this.block = e.getBlock();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Block getBlock() {
        return this.block;
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

    public BlockBreakEvent getBlockBreakEvent() {
        return this.event;
    }
}