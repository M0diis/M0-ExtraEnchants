package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.HasteMinerEvent;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

public class OnHasteMine implements Listener {
    private final ExtraEnchants plugin;

    public OnHasteMine(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHasteMine(final HasteMinerEvent e) {
        if (!Utils.shouldTrigger(EEnchant.HASTE_MINER)) {
            return;
        }

        Player p = e.getPlayer();

        Optional<PotionEffect> current = p.getActivePotionEffects()
                .stream()
                .filter(effect -> effect.getType().equals(PotionEffectType.FAST_DIGGING))
                .findFirst();

        if(current.isPresent()) {
            return;
        }

        int level = e.getEnchantLevel();

        int duration = (level == 0 ? 1 : level) * 100;

        p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, duration, level - 1));

        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_HIT, 0.1F, 0.1F);
    }
}
