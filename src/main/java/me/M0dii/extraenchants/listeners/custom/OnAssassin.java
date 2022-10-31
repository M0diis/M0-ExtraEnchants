package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.AssassinEvent;
import me.m0dii.extraenchants.events.LifestealEvent;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.Random;

public class OnAssassin implements Listener {
    private final ExtraEnchants plugin;

    public OnAssassin(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAssassin(AssassinEvent e) {
        if (!Utils.shouldTrigger(EEnchant.ASSASSIN)) {
            return;
        }

        Player damager = e.getPlayer();

        Entity target = e.getEntityDamageEvent().getEntity();

        double damage = e.getEntityDamageEvent().getDamage();

        Location loc = target.getLocation();
        Location loc2 = damager.getLocation();

        double distance = loc.distance(loc2);

        switch ((int) Math.round(distance)) {
            case 0 -> e.getEntityDamageEvent().setDamage(damage * 1.30);
            case 1 -> e.getEntityDamageEvent().setDamage(damage * 1.15);
            case 2 -> e.getEntityDamageEvent().setDamage(damage * 0.8);
            case 3 -> e.getEntityDamageEvent().setDamage(damage * 0.65);
            default -> e.getEntityDamageEvent().setDamage(damage);
        }
    }
}
