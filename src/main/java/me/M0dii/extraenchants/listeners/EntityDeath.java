package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.BeheadingEvent;
import me.m0dii.extraenchants.events.DeathSiphonEvent;
import me.m0dii.extraenchants.utils.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EntityDeath implements Listener {

    private final ExtraEnchants plugin;

    public EntityDeath(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(final EntityDeathEvent e) {
        if (e.getEntity().getKiller() == null) {
            return;
        }

        ItemStack hand = e.getEntity().getKiller().getInventory().getItemInMainHand();

        if(hand == null || hand.getType().isAir()) {
            return;
        }

        ItemMeta itemMeta = hand.getItemMeta();

        if(itemMeta == null) {
            return;
        }

        if (itemMeta.hasEnchant(EEnchant.BEHEADING.getEnchantment())) {
            Bukkit.getPluginManager().callEvent(new BeheadingEvent(e.getEntity().getKiller(), e));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeathDeathSiphon(final EntityDeathEvent e) {
        Messenger.debug("EntityDeathEvent called");

        if(EEnchant.DEATH_SIPHON.isDisabled()) {
            return;
        }

        Player killer = e.getEntity().getKiller();

        if (killer == null) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new DeathSiphonEvent(killer, e));
    }
}