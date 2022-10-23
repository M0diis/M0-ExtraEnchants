package me.M0dii.ExtraEnchants.Listeners;

import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import me.M0dii.ExtraEnchants.Events.AntiThornsEvent;
import me.M0dii.ExtraEnchants.Events.LifestealEvent;
import me.M0dii.ExtraEnchants.ExtraEnchants;
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

        if(!mainHand.getItemMeta().hasEnchant(CustomEnchants.LIFESTEAL)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new LifestealEvent(p, e));
    }
}