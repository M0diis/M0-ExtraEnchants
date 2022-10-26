package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.BeheadingEvent;
import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeath implements org.bukkit.event.Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity().getKiller() != null) {
            if (e.getEntity().getKiller().getInventory().getItemInMainHand()
                    .getItemMeta().hasEnchant(EEnchant.BEHEADING.getEnchant())) {
                Bukkit.getPluginManager().callEvent(new BeheadingEvent(e.getEntity().getKiller(), e));
            }
        }
    }
}