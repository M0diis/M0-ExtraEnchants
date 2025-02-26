package me.m0dii.extraenchants.events;

import lombok.Getter;
import me.m0dii.extraenchants.enchants.EEnchant;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class CombineEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private final @NotNull Player player;
    private final @NotNull InventoryClickEvent event;
    @Getter
    private final @NotNull EEnchant enchant;
    @Getter
    private final @NotNull Enchantment enchantment;
    private final String enchantName;
    @Getter
    private final int enchantLevel;
    private boolean isCancelled;

    public CombineEvent(@NotNull Player p, @NotNull InventoryClickEvent e, @NotNull EEnchant enchant, int level) {
        this.player = p;
        this.event = e;
        this.enchantment = enchant.getEnchantment();
        this.enchant = enchant;
        this.enchantName = this.enchantment.key().asString();
        this.enchantLevel = level;
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

    public InventoryClickEvent getInventoryClickEvent() {
        return this.event;
    }

    public String getEnchantString() {
        return this.enchantName;
    }
}