package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.AntiThornsEvent;
import me.m0dii.extraenchants.events.AssassinEvent;
import me.m0dii.extraenchants.events.LifestealEvent;
import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDamage implements org.bukkit.event.Listener {
    private ExtraEnchants plugin;

    public PlayerDamage(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageAntiThorns(EntityDamageEvent e) {
        if (!plugin.getCfg().getBoolean("enchants.antithorns.enabled")) {
            return;
        }

        if(!(e.getEntity() instanceof Player p)) {
            return;
        }

        if(!e.getCause().equals(EntityDamageEvent.DamageCause.THORNS)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new AntiThornsEvent(p, e));
    }
    
    @EventHandler
    public void onEntityDamageLifesteal(EntityDamageByEntityEvent e) {
        if (!plugin.getCfg().getBoolean("enchants.lifesteal.enabled")) {
            return;
        }

        if(!(e.getDamager() instanceof Player p)) {
            return;
        }

        ItemStack mainHand = p.getInventory().getItemInMainHand();

        if(mainHand == null || mainHand.getType().equals(Material.AIR)) {
            return;
        }

        if(!mainHand.hasItemMeta()) {
            return;
        }

        if(!mainHand.getItemMeta().hasEnchant(EEnchant.LIFESTEAL.getEnchant())) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new LifestealEvent(p, e));
    }

    @EventHandler
    public void onEntityDamageAssassin(EntityDamageByEntityEvent e) {
        if (!plugin.getCfg().getBoolean("enchants.assassin.enabled")) {
            return;
        }

        if(!(e.getDamager() instanceof Player p)) {
            return;
        }

        ItemStack mainHand = p.getInventory().getItemInMainHand();

        if(mainHand == null || mainHand.getType().equals(Material.AIR)) {
            return;
        }

        if(!mainHand.hasItemMeta()) {
            return;
        }

        if(!mainHand.getItemMeta().hasEnchant(EEnchant.LIFESTEAL.getEnchant())) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new AssassinEvent(p, e));
    }
}