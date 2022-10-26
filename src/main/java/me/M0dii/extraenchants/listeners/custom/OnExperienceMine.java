package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.events.ExperienceMinerEvent;
import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;

public class OnExperienceMine implements Listener {
    private static final Random rnd = new Random();
    private final ExtraEnchants plugin;

    public OnExperienceMine(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExperienceMine(ExperienceMinerEvent e) {
        if (rnd.nextInt(100) > 10) {
            return;
        }

        Player p = e.getPlayer();

        Block source = e.getBlock();

        if(source.hasMetadata("EXPERIENCE_MINER")) {
            return;
        }

        source.setMetadata("EXPERIENCE_MINER", new FixedMetadataValue(this.plugin, true));

        e.getBlockBreakEvent().setExpToDrop(rnd.nextInt(p.getLevel() + 1) + 1);
    }
}
