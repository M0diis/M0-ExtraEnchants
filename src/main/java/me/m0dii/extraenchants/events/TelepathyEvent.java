package me.m0dii.extraenchants.events;

import lombok.Getter;
import lombok.Setter;
import me.m0dii.extraenchants.utils.pipeline.BlockBreakContext;
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

@Getter
@Setter
public class TelepathyEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack tool;
    @Nullable
    private final BlockBreakEvent blockBreakEvent;
    @Nullable
    private final PlayerInteractEvent playerInteractEvent;
    private final Block block;
    private final Collection<ItemStack> drops;
    private boolean isCancelled;

    public TelepathyEvent(BlockBreakContext ctx) {
        this.player = ctx.player();
        this.tool = ctx.toolUsed();
        this.blockBreakEvent = ctx.getEvent();
        this.playerInteractEvent = null;
        this.block = ctx.block();
        this.drops = ctx.getDrops();
    }

    public TelepathyEvent(Player p, ItemStack tool, PlayerInteractEvent e, Collection<ItemStack> drops) {
        this.player = p;
        this.tool = tool;
        this.blockBreakEvent = null;
        this.playerInteractEvent = e;
        this.block = e.getClickedBlock();
        this.drops = drops;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}