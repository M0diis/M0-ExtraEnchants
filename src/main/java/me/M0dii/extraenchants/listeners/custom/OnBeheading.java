package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.BeheadingEvent;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class OnBeheading implements Listener {
    private final ExtraEnchants plugin;
    private final List<String> heads = List.of(
            "CREEPER_HEAD",
            "WITHER_SKELETON_SKULL",
            "ZOMBIE_HEAD",
            "DRAGON_HEAD"
    );

    public OnBeheading(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBehead(final BeheadingEvent e) {
        if (!Utils.shouldTrigger(EEnchant.BEHEADING)) {
            return;
        }

        Player killer = e.getPlayer();

        Entity died = e.getEntityDeathEvent().getEntity();

        ItemStack head;

        if (died instanceof Player) {
            head = new ItemStack(Material.PLAYER_HEAD);

            ((SkullMeta) head.getItemMeta()).setOwningPlayer((OfflinePlayer) died);

            died.getWorld().dropItemNaturally(died.getLocation(), head);

            return;
        }

        if (died instanceof Skeleton) {
            head = new ItemStack(Material.SKELETON_SKULL);

            died.getWorld().dropItemNaturally(died.getLocation(), head);

            return;
        }

        if (died instanceof Sheep) {
            head = new ItemStack(Material.PLAYER_HEAD);

            ((SkullMeta) head.getItemMeta()).setOwningPlayer((OfflinePlayer) died);

            died.getWorld().dropItemNaturally(died.getLocation(), head);

            return;
        }

        for (String hn : heads) {
            if (died.getName().equalsIgnoreCase(hn)) {
                Material mat = Material.getMaterial(hn);

                if (mat == null) {
                    continue;
                }

                head = new ItemStack(mat);

                died.getWorld().dropItemNaturally(died.getLocation(), head);

                break;
            }
        }
    }
}
