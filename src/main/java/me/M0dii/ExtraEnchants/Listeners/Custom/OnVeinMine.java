package me.M0dii.ExtraEnchants.Listeners.Custom;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import me.M0dii.ExtraEnchants.Events.HasteMinerEvent;
import me.M0dii.ExtraEnchants.Events.VeinMinerEvent;
import me.M0dii.ExtraEnchants.ExtraEnchants;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OnVeinMine implements Listener {
    private static final BlockFace[] AREA = {
            BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH
    };

    private static final String META_BLOCK_VEINED = "veinminer_block_veined";

    private static final Random rnd = new Random();
    private final ExtraEnchants plugin;
    private final Residence res;
    private ResidenceManager rm;
    public OnVeinMine(ExtraEnchants plugin) {
        this.plugin = plugin;

        this.res = Residence.getInstance();

        if (res != null) {
            this.rm = res.getResidenceManager();
        }
    }

    @EventHandler
    public void onVeinMine(VeinMinerEvent e) {
        if (rnd.nextInt(100) > 10) {
            return;
        }

        Player p = e.getPlayer();

        Block source = e.getBlock();

        if(source.hasMetadata(META_BLOCK_VEINED)) {
            return;
        }

        Set<Block> ores = new HashSet<>();
        Set<Block> prepare = getNearby(source, p);

        int limit = 15;

        if(prepare.size() == 0) {
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
                .filter(b -> allowed(player, b.getLocation()))
                .collect(Collectors.toSet());
    }

    private boolean allowed(Player p, Location loc) {
        if (res == null) {
            return true;
        }

        ClaimedResidence residence = rm.getByLoc(loc);

        if (residence == null) {
            return true;
        }

        ResidencePermissions perms = residence.getPermissions();

        return perms.playerHas(p, Flags.build, true)
                || residence.isOwner(p)
                || residence.isTrusted(p)
                || res.isResAdminOn(p);
    }
}
