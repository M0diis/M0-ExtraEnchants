package me.m0dii.extraenchants.events;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class TelepathyEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    @Nullable
    private final BlockBreakEvent blockBreakEvent;
    @Getter
    @Nullable
    private final PlayerInteractEvent playerInteractEvent;
    @Getter
    private final Block block;
    @Getter
    private final Collection<ItemStack> drops;
    private boolean isCancelled;

    public TelepathyEvent(Player p, BlockBreakEvent e, Collection<ItemStack> drops) {
        this.player = p;
        this.blockBreakEvent = e;
        this.playerInteractEvent = null;
        this.block = e.getBlock();
        this.drops = drops;
    }

    public TelepathyEvent(Player p, PlayerInteractEvent e, Collection<ItemStack> drops) {
        this.player = p;
        this.blockBreakEvent = null;
        this.playerInteractEvent = e;
        this.block = e.getClickedBlock();
        this.drops = drops;
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

}