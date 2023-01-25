package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.AssassinEvent;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class OnAssassin implements Listener {
    private final ExtraEnchants plugin;

    public OnAssassin(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAssassin(final AssassinEvent e) {
        if (!Utils.shouldTrigger(EEnchant.ASSASSIN)) {
            return;
        }

        EntityDamageEvent damageEvent = e.getEntityDamageEvent();

        Entity target = damageEvent.getEntity();

        if (EEnchant.ASSASSIN.isPlayerOnly() && !(target instanceof Player)) {
            return;
        }

        Player damager = e.getPlayer();

        double damage = damageEvent.getDamage();

        Location loc = target.getLocation();
        Location loc2 = damager.getLocation();

        double distance = loc.distance(loc2);

        if(distance <= 0.15) {
            damageEvent.setDamage(damage * 1.20);
        }

        if(distance > 0.15 && distance <= 0.5) {
            damageEvent.setDamage(damage * 1.15);
        }

        if(distance > 0.5 && distance <= 1) {
            damageEvent.setDamage(damage * 1.10);
        }

        if(distance > 1 && distance <= 1.5) {
            damageEvent.setDamage(damage * 1.05);
        }

        if(distance > 1.5 && distance <= 2) {
            damageEvent.setDamage(damage * 0.90);
        }

        if(distance > 2 && distance <= 2.5) {
            damageEvent.setDamage(damage * 0.85);
        }

        if(distance > 2.5 && distance <= 3) {
            damageEvent.setDamage(damage * 0.80);
        }

        if(distance > 3) {
            damageEvent.setDamage(damage * 0.7);
        }


        Messenger.debug("Distance: " + distance + " damage: " + damageEvent.getDamage());

    }
}
