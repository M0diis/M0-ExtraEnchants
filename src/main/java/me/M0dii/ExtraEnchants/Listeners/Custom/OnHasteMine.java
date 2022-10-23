package me.M0dii.ExtraEnchants.Listeners.Custom;

import me.M0dii.ExtraEnchants.Events.BeheadingEvent;
import me.M0dii.ExtraEnchants.Events.HasteMinerEvent;
import me.M0dii.ExtraEnchants.ExtraEnchants;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class OnHasteMine implements Listener {
    private static final Random rnd = new Random();

    @EventHandler
    public void onHasteMine(HasteMinerEvent e) {
        if (rnd.nextInt(100) > 10) {
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

        p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 100, 1));

        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_HIT, 0.1F, 0.1F);
    }
}
