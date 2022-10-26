package me.m0dii.extraenchants.listeners.custom;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.events.TunnelEvent;
import me.m0dii.extraenchants.events.VeinMinerEvent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OnTunnel implements Listener {
    private final ExtraEnchants plugin;
    private final Residence res;
    private ResidenceManager rm;
    public OnTunnel(ExtraEnchants plugin) {
        this.plugin = plugin;

        this.res = Residence.getInstance();

        if (res != null) {
            this.rm = res.getResidenceManager();
        }
    }

    @EventHandler
    public void onTunnel(TunnelEvent e) {
        Player p = e.getPlayer();

        Block source = e.getBlock();

        int level = e.getEnchantLevel();

        if(level == 1) {
            Block opposite = source.getRelative(p.getFacing().getOppositeFace());

            destroy(p, opposite);
        }

        if(level == 2) {
            Block opposite = source.getRelative(p.getFacing().getOppositeFace());

            destroy(p, opposite);

            destroy(p, source.getRelative(BlockFace.DOWN));
            destroy(p, opposite.getRelative(BlockFace.DOWN));
        }
    }

    private void destroy(Player p, Block b) {
        if (b.getType().isSolid()) {
            return;
        }

        if(!allowed(p, b.getLocation())) {
            return;
        }

        b.breakNaturally(p.getInventory().getItemInMainHand());
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
