package me.m0dii.extraenchants.events;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class ExcavatorEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final Player player;
    private final BlockBreakEvent event;
    @Getter
    private final Block block;
    @Getter
    private final int enchantLevel;
    private boolean isCancelled;

    public ExcavatorEvent(Player p, BlockBreakEvent e, int enchantLevel) {
        this.player = p;
        this.event = e;
        this.block = e.getBlock();
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

    public BlockBreakEvent getBlockBreakEvent() {
        return this.event;
    }

}