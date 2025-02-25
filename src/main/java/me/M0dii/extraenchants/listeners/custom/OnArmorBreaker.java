package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.ArmorBreakerEvent;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class OnArmorBreaker implements Listener {
    private final ExtraEnchants plugin;

    public OnArmorBreaker(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArmorBreaker(final ArmorBreakerEvent e) {
        if (!Utils.shouldTrigger(EEnchant.ARMOR_BREAKER)) {
            return;
        }

        Player damager = e.getPlayer();

        Entity target = e.getEntityDamageEvent().getEntity();

        if (!(target instanceof Player targetPlayer)) {
            return;
        }

        int level = e.getEnchantLevel();

        ItemStack helmet = targetPlayer.getInventory().getHelmet();
        ItemStack chestplate = targetPlayer.getInventory().getChestplate();
        ItemStack leggings = targetPlayer.getInventory().getLeggings();
        ItemStack boots = targetPlayer.getInventory().getBoots();

        int random = (int) (Math.random() * 4);

        switch (random) {
            case 0:
                if (helmet != null) {
                    InventoryUtils.applyDurability(damager, helmet);
                }
                break;
            case 1:
                if (chestplate != null) {
                    InventoryUtils.applyDurability(damager, chestplate);
                }
                break;
            case 2:
                if (leggings != null) {
                    InventoryUtils.applyDurability(damager, leggings);
                }
                break;
            case 3:
                if (boots != null) {
                    InventoryUtils.applyDurability(damager, boots);
                }
                break;
        }
    }
}
