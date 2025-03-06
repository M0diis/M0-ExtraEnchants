package me.m0dii.extraenchants.events;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SmeltEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final Player player;
    private final BlockBreakEvent event;
    @Getter
    private final Block block;
    @Getter
    private final Collection<ItemStack> drops;
    private boolean isCancelled;

    public SmeltEvent(Player p, BlockBreakEvent e, Collection<ItemStack> drops) {
        this.player = p;
        this.event = e;
        this.drops = drops;
        this.block = e.getBlock();
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