package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.ExperienceMinerEvent;
import me.m0dii.extraenchants.utils.Utils;
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
    public void onExperienceMine(final ExperienceMinerEvent e) {
        if (!Utils.shouldTrigger(EEnchant.EXPERIENCE_MINER)) {
            return;
        }

        Player p = e.getPlayer();

        Block source = e.getBlock();

        if (source.hasMetadata("EXPERIENCE_MINER")) {
            return;
        }

        source.setMetadata("EXPERIENCE_MINER", new FixedMetadataValue(this.plugin, true));

        e.getBlockBreakEvent().setExpToDrop(rnd.nextInt(p.getLevel() + 1) + 1);
    }
}
