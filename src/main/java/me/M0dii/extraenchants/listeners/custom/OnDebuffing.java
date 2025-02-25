package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.DebuffingEvent;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class OnDebuffing implements Listener {
    private final ExtraEnchants plugin;
    private final List<Block> webs = new ArrayList<>();

    public OnDebuffing(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDebuffing(final DebuffingEvent e) {
        Messenger.debug("Debuffing called");

        if (!Utils.shouldTrigger(EEnchant.DEBUFFING)) {
            return;
        }

        Entity target = e.getEntityDamageEvent().getEntity();

        if (!(target instanceof Player targetPlayer)) {
            return;
        }

        if (targetPlayer.getHealth() > 8) {
            return;
        }

        Messenger.debug("Removing potion effects");

        targetPlayer.getActivePotionEffects().forEach(effect -> targetPlayer.removePotionEffect(effect.getType()));
    }
}
