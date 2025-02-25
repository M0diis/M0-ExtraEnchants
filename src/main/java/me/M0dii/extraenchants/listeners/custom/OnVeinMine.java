package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.VeinMinerEvent;
import me.m0dii.extraenchants.utils.Messenger;
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

public class OnVeinMine implements Listener {
    private static final BlockFace[] AREA = {
            BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH
    };
    private static final String META_BLOCK_VEINED = "veinminer_block_veined";
    private final ExtraEnchants plugin;

    public OnVeinMine(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVeinMine(final VeinMinerEvent e) {
        Messenger.debug("VeinMiner event called.");

        if (!Utils.shouldTrigger(EEnchant.VEIN_MINER)) {
            return;
        }

        Player p = e.getPlayer();

        Block source = e.getBlock();

        if (source.hasMetadata(META_BLOCK_VEINED)) {
            return;
        }

        Set<Block> ores = new HashSet<>();
        Set<Block> prepare = getNearby(source, p);

        int limit = 15;

        if (prepare.size() == 0) {
            return;
        }

        while (ores.addAll(prepare) && ores.size() < limit) {
            Set<Block> nearby = new HashSet<>();
            prepare.forEach(prepared -> nearby.addAll(getNearby(prepared, p)));
            prepare.clear();
            prepare.addAll(nearby);
        }

        ores.remove(source);

        ores.forEach(ore -> {
            ore.setMetadata(META_BLOCK_VEINED, new FixedMetadataValue(plugin, true));
            ore.breakNaturally(p.getInventory().getItemInMainHand());
            ore.removeMetadata(META_BLOCK_VEINED, plugin);
        });

        p.getWorld().playSound(source.getLocation(), Sound.ITEM_TOTEM_USE, 0.03F, 0.5F);
    }

    @NotNull
    private Set<Block> getNearby(@NotNull Block block, Player player) {
        return Stream.of(AREA).map(block::getRelative)
                .filter(blockAdded -> blockAdded.getType() == block.getType())
                .filter(b -> b.getType().name().contains("ORE"))
                .filter(b -> Utils.allowedAt(player, b.getLocation()))
                .collect(Collectors.toSet());
    }
}
