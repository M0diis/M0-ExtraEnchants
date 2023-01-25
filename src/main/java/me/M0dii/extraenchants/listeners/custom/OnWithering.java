package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.WitheringEvent;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

public class OnWithering implements Listener {
    private final ExtraEnchants plugin;

    public OnWithering(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWithering(final WitheringEvent e) {
        if (!Utils.shouldTrigger(EEnchant.WITHERING)) {
            return;
        }

        Entity target = e.getEntityDamageEvent().getEntity();

        if (EEnchant.WEBBING.isPlayerOnly() && !(target instanceof Player)) {
            return;
        }

        Player targetPlayer = (Player)target;

        int level = e.getEnchantLevel();

        Optional<PotionEffect> current = targetPlayer.getActivePotionEffects()
                .stream()
                .filter(effect -> effect.getType().equals(PotionEffectType.WITHER))
                .findFirst();

        if(current.isPresent()) {
            return;
        }

        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (level * 60), level - 1));
    }
}
