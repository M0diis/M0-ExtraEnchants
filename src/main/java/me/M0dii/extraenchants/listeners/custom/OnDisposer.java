package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.DisposerEvent;
import me.m0dii.extraenchants.events.VeinMinerEvent;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OnDisposer implements Listener {
    private final ExtraEnchants plugin;

    public OnDisposer(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDisposer(final DisposerEvent e) {
        if(!Utils.shouldTrigger(EEnchant.DISPOSER)) {
            return;
        }

        e.getBlockBreakEvent().setDropItems(false);
    }
}
