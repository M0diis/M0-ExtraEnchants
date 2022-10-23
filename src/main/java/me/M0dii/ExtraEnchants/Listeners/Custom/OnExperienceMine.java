package me.M0dii.ExtraEnchants.Listeners.Custom;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import javassist.P;
import me.M0dii.ExtraEnchants.Events.ExperienceMinerEvent;
import me.M0dii.ExtraEnchants.Events.VeinMinerEvent;
import me.M0dii.ExtraEnchants.ExtraEnchants;
import org.bukkit.Location;
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

        e.getBlockBreakEvent().setExpToDrop(rnd.nextInt(p.getLevel()) + 1);
    }
}
