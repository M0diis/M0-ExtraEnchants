package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PlayerDeathRespawn implements Listener {
    private final Map<UUID, List<ItemStack>> keptItems = new HashMap<>();
    private final ExtraEnchants plugin;

    public PlayerDeathRespawn(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        if (!keptItems.containsKey(p.getUniqueId())) {
            return;
        }

        List<ItemStack> kept = keptItems.get(p.getUniqueId());

        List<ItemStack> updated = new ArrayList<>();

        if (kept != null && kept.size() > 0) {
            for (ItemStack item : kept) {
                ItemStack copy = item.clone();

                if (!copy.hasItemMeta()) {
                    continue;
                }

                ItemMeta m = copy.getItemMeta();

                List<String> lore = m.getLore();

                if (lore != null && lore.size() > 0)
                    for (int i = 0; i < lore.size(); i++) {
                        String s = lore.get(i);

                        if (ChatColor.stripColor(s).startsWith("Bonded")) {
                            lore.remove(s);
                        }
                    }

                m.setLore(lore);
                copy.setItemMeta(m);

                updated.add(copy);
            }
        }

        for (ItemStack item : updated) {
            p.getInventory().addItem(item);
        }

        keptItems.remove(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent e) {
        List<ItemStack> drops = e.getDrops();

        Player p = e.getEntity().getPlayer();

        if (p == null) {
            return;
        }

        List<ItemStack> bondedItems = new ArrayList<>();

        for (int i = 0; i < drops.size(); i++) {
            ItemStack item = drops.get(i);

            if (item.hasItemMeta() && item.getItemMeta() != null) {
                ItemMeta meta = item.getItemMeta();

                if (meta.hasEnchant(EEnchant.BONDED.getEnchantment())) {
                    drops.remove(item);
                    meta.removeEnchant(EEnchant.BONDED.getEnchantment());
                    item.setItemMeta(meta);
                    bondedItems.add(item);
                }
            }
        }

        keptItems.put(p.getUniqueId(), bondedItems);
    }
}
