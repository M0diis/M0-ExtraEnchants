package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.events.AssassinEvent;
import me.m0dii.extraenchants.events.BerserkEvent;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnBerserk implements Listener {

    private final ExtraEnchants plugin;

    public OnBerserk(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBerserk(final BerserkEvent e) {
        Player damager = e.getPlayer();

        double healthHas = damager.getHealth();
        AttributeInstance attribute = damager.getAttribute(Attribute.GENERIC_MAX_HEALTH);

        if(attribute == null) {
            return;
        }

        double healthMax = attribute.getValue();
        double healthDiff = healthMax - healthHas;

        double healthPoint = 0.5;

        if (healthHas >= healthMax || healthDiff < healthPoint) {
            return;
        }

        int pointAmount = (int) (healthDiff / healthPoint);

        if (pointAmount == 0) {
            return;
        }

        double damageAmount = 0.1;
        double damageCap = 1.35;
        double damageFinal = Math.min(damageCap, 1D + damageAmount * pointAmount);

        e.getEntityDamageEvent().setDamage(e.getEntityDamageEvent().getDamage() * damageFinal);
    }
}
