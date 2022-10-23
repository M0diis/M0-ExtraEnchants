package me.M0dii.ExtraEnchants.Listeners.Custom;

import me.M0dii.ExtraEnchants.Events.BeheadingEvent;
import me.M0dii.ExtraEnchants.Events.LifestealEvent;
import me.M0dii.ExtraEnchants.ExtraEnchants;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OnLifesteal implements Listener {
    private static final Random rnd = new Random();

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
