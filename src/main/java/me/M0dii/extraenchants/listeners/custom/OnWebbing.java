package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.WebbingEvent;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class OnWebbing implements Listener {
    private final ExtraEnchants plugin;
    private final List<Block> webs = new ArrayList<>();

    public OnWebbing(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWithering(final WebbingEvent e) {
        Messenger.debug("WebbingEvent called");

        if (!Utils.shouldTrigger(EEnchant.WEBBING)) {
            return;
        }

        Entity target = e.getEntityDamageEvent().getEntity();

        if (EEnchant.WEBBING.isPlayerOnly() && !(target instanceof Player)) {
            return;
        }

        Location feet = target.getLocation();

        if (!feet.getBlock().getType().isAir()) {
            return;
        }

        if (webs.contains(feet.getBlock())) {
            return;
        }

        webs.add(feet.getBlock());

        feet.getBlock().setType(Material.COBWEB);

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (feet.getBlock().getType() == Material.COBWEB) {
                feet.getBlock().setType(Material.AIR);
                webs.remove(feet.getBlock());
            }
        }, 20L * EEnchant.WEBBING.getDuration());
    }
}
