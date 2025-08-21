package me.m0dii.extraenchants.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Getter
@Setter
public class TimberEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack tool;
    private final BlockBreakEvent blockBreakEvent;
    private final Block block;
    private final Collection<ItemStack> drops;
    private boolean isCancelled;

    public TimberEvent(Player p,ItemStack tool, BlockBreakEvent e, Collection<ItemStack> drops) {
        this.player = p;
        this.tool = tool;
        this.blockBreakEvent = e;
        this.drops = drops;
        this.block = e.getBlock();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}