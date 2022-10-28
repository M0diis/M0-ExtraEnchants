package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.AntiThornsEvent;
import me.m0dii.extraenchants.events.AssassinEvent;
import me.m0dii.extraenchants.events.BerserkEvent;
import me.m0dii.extraenchants.events.LifestealEvent;
import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PlayerDamage implements Listener {
    private ExtraEnchants plugin;

    public PlayerDamage(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageAntiThorns(final EntityDamageEvent e) {
        if(EEnchant.ANTI_THORNS.isDisabled()) {
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
    public void onEntityDamageLifesteal(final EntityDamageByEntityEvent e) {
        if(shouldSkip(e, EEnchant.LIFESTEAL)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new LifestealEvent((Player)e.getDamager(), e));
    }

    @EventHandler
    public void onEntityDamageAssassin(EntityDamageByEntityEvent e) {
        if(shouldSkip(e, EEnchant.ASSASSIN)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new AssassinEvent((Player)e.getDamager(), e));
    }

    @EventHandler
    public void onEntityDamageBerserk(final EntityDamageByEntityEvent e) {
        if(shouldSkip(e, EEnchant.BERSERK)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new BerserkEvent((Player)e.getDamager(), e));
    }

    private boolean shouldSkip(EntityDamageByEntityEvent e, EEnchant enchant) {
        if(enchant.isDisabled()) {
            return true;
        }

        if(!(e.getDamager() instanceof Player p)) {
            return true;
        }

        ItemStack mainHand = p.getInventory().getItemInMainHand();

        if(mainHand == null || mainHand.getType().isAir()) {
            return true;
        }

        if(!mainHand.hasItemMeta()) {
            return true;
        }

        if(!mainHand.getItemMeta().hasEnchant(enchant.getEnchantment())) {
            return true;
        }

        return false;
    }
}