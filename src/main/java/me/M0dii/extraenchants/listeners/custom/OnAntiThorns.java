package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.AntiThornsEvent;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OnAntiThorns implements Listener {
    private static final Random random = new Random();
    private final ExtraEnchants plugin;

    public OnAntiThorns(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAntiThorns(final AntiThornsEvent e) {
        if (!Utils.shouldTrigger(EEnchant.ANTI_THORNS)) {
            return;
        }

        ItemStack helmet = e.getPlayer().getInventory().getHelmet();
        ItemStack chestplate = e.getPlayer().getInventory().getChestplate();
        ItemStack leggings = e.getPlayer().getInventory().getLeggings();
        ItemStack boots = e.getPlayer().getInventory().getBoots();

        int deflectPercentage = 0;

        if(helmet != null) {
            if(helmet.containsEnchantment(EEnchant.ANTI_THORNS.getEnchantment())) {
                InventoryUtils.applyDurability(helmet);

                deflectPercentage += 25;
            }
        }

        if(chestplate != null) {
            if(chestplate.containsEnchantment(EEnchant.ANTI_THORNS.getEnchantment())) {
                InventoryUtils.applyDurability(chestplate);

                deflectPercentage += 25;
            }
        }

        if(leggings != null) {
            if(leggings.containsEnchantment(EEnchant.ANTI_THORNS.getEnchantment())) {
                InventoryUtils.applyDurability(leggings);

                deflectPercentage += 25;
            }
        }

        if(boots != null) {
            if(boots.containsEnchantment(EEnchant.ANTI_THORNS.getEnchantment())) {
                InventoryUtils.applyDurability(boots);

                deflectPercentage += 25;
            }
        }

        if(deflectPercentage == 100) {
            e.getEntityDamageEvent().setCancelled(true);
        } else {
            if(random.nextInt(100) < deflectPercentage) {
                e.getEntityDamageEvent().setCancelled(true);
            }
        }
    }
}
