package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.BarbarianEvent;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnBarbarian implements Listener {
    private final ExtraEnchants plugin;

    public OnBarbarian(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBarbarian(final BarbarianEvent e) {
        if (!Utils.shouldTrigger(EEnchant.BARBARIAN)) {
            return;
        }

        double damage = e.getEntityDamageEvent().getDamage();

        e.getEntityDamageEvent().setDamage(damage * 1.25);
    }
}
