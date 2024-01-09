package me.m0dii.extraenchants.events;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class CombineEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final @NotNull Player player;
    private final @NotNull InventoryClickEvent event;
    private final @NotNull Enchantment enchant;
    private final String enchantName;
    private final int enchantLevel;
    private boolean isCancelled;

    public CombineEvent(@NotNull Player p, @NotNull InventoryClickEvent e, Enchantment enchant, int level) {
        this.player = p;
        this.event = e;
        this.enchant = enchant;
        this.enchantName = enchant.translationKey();
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

    public Player getPlayer() {
        return this.player;
    }

    public InventoryClickEvent getInventoryClickEvent() {
        return this.event;
    }

    public String getEnchantString() {
        return this.enchantName;
    }

    public Enchantment getEnchant() {
        return this.enchant;
    }

    public int getEnchantLevel() {
        return this.enchantLevel;
    }
}