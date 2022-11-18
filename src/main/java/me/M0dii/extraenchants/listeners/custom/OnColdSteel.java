package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.ColdSteelEvent;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class OnColdSteel implements Listener {
    private final ExtraEnchants plugin;

    public OnColdSteel(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onColdSteel(final ColdSteelEvent e) {
        Messenger.debug("ColdSteelEvent called.");

        if (!Utils.shouldTrigger(EEnchant.COLD_STEEL)) {
            return;
        }

        boolean apply = false;

        ItemStack helmet = e.getPlayer().getInventory().getHelmet();
        ItemStack chestplate = e.getPlayer().getInventory().getChestplate();
        ItemStack leggings = e.getPlayer().getInventory().getLeggings();
        ItemStack boots = e.getPlayer().getInventory().getBoots();

        if(InventoryUtils.hasEnchant(helmet, EEnchant.COLD_STEEL)) {
            if (Utils.shouldTrigger(EEnchant.COLD_STEEL)) {
                InventoryUtils.applyDurability(helmet);

                apply = true;
            }
        }

        if(InventoryUtils.hasEnchant(chestplate, EEnchant.COLD_STEEL)) {
            if (Utils.shouldTrigger(EEnchant.COLD_STEEL)) {
                InventoryUtils.applyDurability(chestplate);

                apply = true;
            }
        }

        if(InventoryUtils.hasEnchant(leggings, EEnchant.COLD_STEEL)) {
            if(Utils.shouldTrigger(EEnchant.COLD_STEEL)) {
                InventoryUtils.applyDurability(leggings);

                apply = true;
            }
        }

        if(InventoryUtils.hasEnchant(boots, EEnchant.COLD_STEEL)) {
            if (Utils.shouldTrigger(EEnchant.COLD_STEEL)) {
                InventoryUtils.applyDurability(boots);

                apply = true;
            }
        }

        if(!apply) {
            return;
        }

        if(!(e.getEntityDamageEvent().getEntity() instanceof Player receiver)) {
            return;
        }

        receiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 0));
        receiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 60, 0));
    }
}
