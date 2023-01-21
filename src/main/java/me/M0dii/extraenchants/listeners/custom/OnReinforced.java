package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.ReinforcedEvent;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class OnReinforced implements Listener {
    private final ExtraEnchants plugin;

    public OnReinforced(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onColdSteel(final ReinforcedEvent e) {
        Messenger.debug("ReinforcedEvent called.");

        if (!Utils.shouldTrigger(EEnchant.REINFORCED)) {
            return;
        }

        if(!(e.getEntityDamageEvent().getEntity() instanceof Player receiver)) {
            return;
        }

        if(!(e.getEntityDamageEvent().getDamager() instanceof Player)) {
            return;
        }

        double percentage = 0.015;

        int maxCount = 1;

        ItemStack helmet = receiver.getInventory().getHelmet();
        ItemStack chestplate = receiver.getInventory().getChestplate();
        ItemStack leggings = receiver.getInventory().getLeggings();
        ItemStack boots = receiver.getInventory().getBoots();

        if(InventoryUtils.hasEnchant(helmet, EEnchant.REINFORCED)) {
            if (Utils.shouldTrigger(EEnchant.REINFORCED)) {
                InventoryUtils.applyDurability(helmet);

                maxCount++;
            }
        }

        if(InventoryUtils.hasEnchant(chestplate, EEnchant.REINFORCED)) {
            if (Utils.shouldTrigger(EEnchant.REINFORCED)) {
                InventoryUtils.applyDurability(chestplate);

                maxCount++;
            }
        }

        if(InventoryUtils.hasEnchant(leggings, EEnchant.REINFORCED)) {
            if(Utils.shouldTrigger(EEnchant.REINFORCED)) {
                InventoryUtils.applyDurability(leggings);

                maxCount++;
            }
        }

        if(InventoryUtils.hasEnchant(boots, EEnchant.REINFORCED)) {
            if (Utils.shouldTrigger(EEnchant.REINFORCED)) {
                InventoryUtils.applyDurability(boots);

                maxCount++;
            }
        }

        if(maxCount == 1) {
            return;
        }

        double damage = e.getEntityDamageEvent().getDamage();

        Messenger.debug("Damage before: " + damage);

        e.getEntityDamageEvent().setDamage(damage * (1 - (percentage * maxCount)));

        Messenger.debug("Damage after: " + e.getEntityDamageEvent().getDamage());
    }
}
