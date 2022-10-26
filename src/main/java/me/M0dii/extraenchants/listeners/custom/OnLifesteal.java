package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.events.LifestealEvent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.Random;

public class OnLifesteal implements Listener {
    private static final Random rnd = new Random();

    private final ExtraEnchants plugin;

    public OnLifesteal(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLifesteal(LifestealEvent e) {
        if(rnd.nextInt(100) > 10) {
            return;
        }

        Player damager = e.getPlayer();

        Entity target = e.getEntityDamageEvent().getEntity();

        double damage = e.getEntityDamageEvent().getDamage();

        double heal = damage * 0.1;

        if(damager.getHealth() + heal > 20) {
            return;
        }

        damager.setHealth(damager.getHealth() + heal);

        damager.getWorld().playSound(damager.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.1F, 0.5F);

        Location locFrom = damager.getLocation();

        Location locTo = target.getLocation();

        Vector dir = locTo.toVector().subtract(locFrom.toVector());

        World world = locFrom.getWorld();

        for (double i = 1; i <= locFrom.distance(locTo); i += 0.25) {
            dir.multiply(i);

            locFrom.add(dir);

            world.spawnParticle(Particle.HEART, locFrom, 1, 0, 0, 0, 0);
            locFrom.subtract(dir);

            dir.normalize();
        }

    }
}
