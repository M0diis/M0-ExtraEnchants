package me.M0dii.ExtraEnchants.Listeners;

import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import me.M0dii.ExtraEnchants.Events.PlowEvent;
import me.M0dii.ExtraEnchants.ExtraEnchants;
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
    public void onTill(PlayerInteractEvent e) {
        if (!plugin.getCfg().getBoolean("enchants.plow.enabled"))
            return;

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        Player p = e.getPlayer();

        if (hand == null) {
            return;
        }

        if (hand.getItemMeta() == null) {
            return;
        }

        if (!hand.getItemMeta().hasEnchant(CustomEnchants.PLOW)) {
            return;
        }

        if (!hand.getType().toString().toUpperCase().contains("HOE")) {
            return;
        }

        if ((p.getGameMode() == GameMode.CREATIVE)
         || (p.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (e.isCancelled()) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new PlowEvent(p, e, hand.getEnchantmentLevel(CustomEnchants.PLOW)));
    }
}
