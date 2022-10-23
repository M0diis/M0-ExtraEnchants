package me.M0dii.ExtraEnchants.Listeners;

import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import me.M0dii.ExtraEnchants.ExtraEnchants;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemDrop implements Listener {
    private final ExtraEnchants plugin;

    public ItemDrop(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!plugin.getCfg().getBoolean("enchants.bonded.enabled")) {
            return;
        }

        ItemStack i = e.getItemDrop().getItemStack();

        if (i.getType() == Material.ENCHANTED_BOOK) {
            return;
        }

        if (!i.hasItemMeta()) {
            return;
        }

        ItemMeta m = i.getItemMeta();

        if (m == null) {
            return;
        }

        if (m.hasEnchant(CustomEnchants.BONDED)) {
            e.setCancelled(true);
        }
    }

}
