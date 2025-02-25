package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.TurtleShellEvent;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class OnTurtleShell implements Listener {
    private final ExtraEnchants plugin;

    public OnTurtleShell(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onColdSteel(final TurtleShellEvent e) {
        Messenger.debug("TurtleShellEvent called.");

        if (!Utils.shouldTrigger(EEnchant.TURTLE_SHELL)) {
            return;
        }

        if (!(e.getEntityDamageEvent().getEntity() instanceof Player receiver)) {
            return;
        }

        if (!(e.getEntityDamageEvent().getDamager() instanceof Player attacker)) {
            return;
        }

        double dot = attacker.getLocation().getDirection().dot(receiver.getLocation().getDirection());

        Messenger.debug("Dot: " + dot);

        if (dot < 0.8) {
            Messenger.debug("Dot is less than 0.75.");
            return;
        }

        double percentage = 0;

        ItemStack helmet = receiver.getInventory().getHelmet();
        ItemStack chestplate = receiver.getInventory().getChestplate();
        ItemStack leggings = receiver.getInventory().getLeggings();
        ItemStack boots = receiver.getInventory().getBoots();

        if (InventoryUtils.hasEnchant(helmet, EEnchant.TURTLE_SHELL)) {
            if (Utils.shouldTrigger(EEnchant.TURTLE_SHELL)) {
                InventoryUtils.applyDurability(e.getPlayer(), helmet);

                percentage += 0.025;
            }
        }

        if (InventoryUtils.hasEnchant(chestplate, EEnchant.TURTLE_SHELL)) {
            if (Utils.shouldTrigger(EEnchant.TURTLE_SHELL)) {
                InventoryUtils.applyDurability(e.getPlayer(), chestplate);

                percentage += 0.025;
            }
        }

        if (InventoryUtils.hasEnchant(leggings, EEnchant.TURTLE_SHELL)) {
            if (Utils.shouldTrigger(EEnchant.TURTLE_SHELL)) {
                InventoryUtils.applyDurability(e.getPlayer(), leggings);

                percentage += 0.025;
            }
        }

        if (InventoryUtils.hasEnchant(boots, EEnchant.TURTLE_SHELL)) {
            if (Utils.shouldTrigger(EEnchant.TURTLE_SHELL)) {
                InventoryUtils.applyDurability(e.getPlayer(), boots);

                percentage += 0.025;
            }
        }

        if (percentage == 0) {
            return;
        }

        Messenger.debug("Percentage: " + percentage);

        double damage = e.getEntityDamageEvent().getDamage();

        Messenger.debug("Damage before: " + damage);

        e.getEntityDamageEvent().setDamage(damage * (1 - percentage));

        Messenger.debug("Damage after: " + e.getEntityDamageEvent().getDamage());
    }
}
