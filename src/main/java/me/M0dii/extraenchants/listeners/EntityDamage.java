package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.*;
import me.m0dii.extraenchants.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class EntityDamage implements Listener {
    private final ExtraEnchants plugin;

    public EntityDamage(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageLifesteal(final EntityDamageByEntityEvent e) {
        if(shouldSkip(e, EEnchant.LIFESTEAL)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new LifestealEvent((Player)e.getDamager(), e));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageAssassin(EntityDamageByEntityEvent e) {
        if(shouldSkip(e, EEnchant.ASSASSIN)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new AssassinEvent((Player)e.getDamager(), e));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageBerserk(final EntityDamageByEntityEvent e) {
        if(shouldSkip(e, EEnchant.BERSERK)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new BerserkEvent((Player)e.getDamager(), e));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageBarbarian(final EntityDamageByEntityEvent e) {
        if(shouldSkip(e, EEnchant.BARBARIAN)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new BarbarianEvent((Player)e.getDamager(), e));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageWebbing(final EntityDamageByEntityEvent e) {
        if(shouldSkip(e, EEnchant.WEBBING)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new WebbingEvent((Player)e.getDamager(), e));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageWithering(final EntityDamageByEntityEvent e) {
        if(shouldSkip(e, EEnchant.WITHERING)) {
            return;
        }

        Player damager = (Player) e.getDamager();

        int level = InventoryUtils.getEnchantLevelHand(damager, EEnchant.WITHERING);

        Bukkit.getPluginManager().callEvent(new WitheringEvent(damager, e, level));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageColdSteel(final EntityDamageByEntityEvent e) {
        if(EEnchant.COLD_STEEL.isDisabled()) {
            return;
        }

        if(!(e.getDamager() instanceof Player p)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new ColdSteelEvent(p, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageTurtleShell(final EntityDamageByEntityEvent e) {
        if(EEnchant.COLD_STEEL.isDisabled()) {
            return;
        }

        if(!(e.getDamager() instanceof Player p)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new TurtleShellEvent(p, e));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageArmorBreaker(final EntityDamageByEntityEvent e) {
        if(EEnchant.COLD_STEEL.isDisabled()) {
            return;
        }

        if(!(e.getDamager() instanceof Player p)) {
            return;
        }

        if(!(e.getEntity() instanceof Player)) {
            return;
        }

        int level = InventoryUtils.getEnchantLevelHand(p, EEnchant.ARMOR_BREAKER);

        Bukkit.getPluginManager().callEvent(new ArmorBreakerEvent(p, e, level));
    }

    private boolean shouldSkip(EntityDamageByEntityEvent e, EEnchant enchant) {
        if(enchant.isDisabled()) {
            return true;
        }

        if(e.isCancelled()) {
            return true;
        }

        if(!(e.getDamager() instanceof Player p)) {
            return true;
        }

        ItemStack mainHand = p.getInventory().getItemInMainHand();

        return !InventoryUtils.hasEnchant(mainHand, enchant);
    }
}