package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.DeathSiphonEvent;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Messenger;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class OnDeathSiphon implements Listener {
    private static final Random random = new Random();
    private final ExtraEnchants plugin;

    public OnDeathSiphon(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAntiThorns(final DeathSiphonEvent e) {
        Messenger.debug("DeathSiphonEvent called");

        if (EEnchant.DEATH_SIPHON.isDisabled()) {
            return;
        }

        Entity target = e.getEntityDeathEvent().getEntity();

        if (EEnchant.DEATH_SIPHON.isPlayerOnly() && !(target instanceof Player)) {
            return;
        }

        ItemStack helmet = e.getPlayer().getInventory().getHelmet();
        ItemStack chestplate = e.getPlayer().getInventory().getChestplate();
        ItemStack leggings = e.getPlayer().getInventory().getLeggings();
        ItemStack boots = e.getPlayer().getInventory().getBoots();

        if (InventoryUtils.hasEnchant(helmet, EEnchant.DEATH_SIPHON)) {
            InventoryUtils.applyDurability(helmet);

            if (random.nextInt(100) < EEnchant.DEATH_SIPHON.getTriggerChance()) {
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 4));
            }
        }

        if (InventoryUtils.hasEnchant(chestplate, EEnchant.DEATH_SIPHON)) {
            InventoryUtils.applyDurability(chestplate);

            if (random.nextInt(100) < EEnchant.DEATH_SIPHON.getTriggerChance()) {
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 4));
            }
        }

        if (InventoryUtils.hasEnchant(leggings, EEnchant.DEATH_SIPHON)) {
            InventoryUtils.applyDurability(leggings);

            if (random.nextInt(100) < EEnchant.DEATH_SIPHON.getTriggerChance()) {
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 4));
            }
        }

        if (InventoryUtils.hasEnchant(boots, EEnchant.DEATH_SIPHON)) {
            InventoryUtils.applyDurability(boots);

            if (random.nextInt(100) < EEnchant.DEATH_SIPHON.getTriggerChance()) {
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 4));
            }
        }
    }
}
