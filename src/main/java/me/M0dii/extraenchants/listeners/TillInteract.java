package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.PlowEvent;
import me.m0dii.extraenchants.events.ReplanterEvent;
import me.m0dii.extraenchants.utils.Enchantables;
import me.m0dii.extraenchants.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TillInteract implements Listener {
    private final ExtraEnchants plugin;

    public TillInteract(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractPlow(final PlayerInteractEvent e) {
        if(EEnchant.PLOW.isDisabled()) {
            return;
        }

        if (e.isCancelled()) {
            return;
        }

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        Player p = e.getPlayer();

        if (hand == null) {
            return;
        }

        if (hand.getItemMeta() == null) {
            return;
        }

        if (!hand.getItemMeta().hasEnchant(EEnchant.PLOW.getEnchantment())) {
            return;
        }

        if (!Enchantables.isHoe(hand)) {
            return;
        }

        if ((p.getGameMode() == GameMode.CREATIVE)
         || (p.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        int level = InventoryUtils.getEnchantLevel(hand, EEnchant.PLOW);

        Bukkit.getPluginManager().callEvent(new PlowEvent(p, e, level));
    }

    @EventHandler
    public void onPlayerInteractReplanter(final PlayerInteractEvent e) {
        if(EEnchant.REPLANTER.isDisabled()) {
            return;
        }

        if (e.isCancelled()) {
            return;
        }

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        Player p = e.getPlayer();

        if (hand == null) {
            return;
        }

        if (hand.getItemMeta() == null) {
            return;
        }

        if (!hand.getItemMeta().hasEnchant(EEnchant.REPLANTER.getEnchantment())) {
            return;
        }

        if (!Enchantables.isHoe(hand)) {
            return;
        }

        if ((p.getGameMode() == GameMode.CREATIVE)
         || (p.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new ReplanterEvent(p, e));
    }
}
