package me.M0dii.ExtraEnchants.Events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class TelepathyEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final BlockBreakEvent event;
    private final Block block;
    private final Collection<ItemStack> drops;
    private boolean isCancelled;

    public TelepathyEvent(Player p, BlockBreakEvent e, Collection<ItemStack> drops) {
        this.player = p;
        this.event = e;
        this.block = e.getBlock();
        this.drops = drops;
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

    public Collection<ItemStack> getDrops() {
        return this.drops;
    }
}