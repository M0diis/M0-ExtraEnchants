package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.EssenceDrainEvent;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class OnEssenceDrain implements Listener {
    private final ExtraEnchants plugin;

    public OnEssenceDrain(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEssenceDrain(final EssenceDrainEvent e) {
        Messenger.debug("EssenceDrainEvent called");

        if (!Utils.shouldTrigger(EEnchant.ESSENCE_DRAIN)) {
            Messenger.debug("EssenceDrainEvent cancelled");
            return;
        }

        EntityDeathEvent entityDeathEvent = e.getEntityDeathEvent();

        int droppedExp = entityDeathEvent.getDroppedExp();

        int newDroppedExp = droppedExp * 2;

        entityDeathEvent.setDroppedExp(newDroppedExp);
    }
}
