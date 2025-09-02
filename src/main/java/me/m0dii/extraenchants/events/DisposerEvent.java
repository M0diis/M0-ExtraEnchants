package me.m0dii.extraenchants.events;

import lombok.Getter;
import lombok.Setter;
import me.m0dii.extraenchants.utils.pipeline.BlockBreakContext;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class DisposerEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final BlockBreakContext context;
    private boolean isCancelled;

    public DisposerEvent(BlockBreakContext ctx) {
        this.context = ctx;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}