package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
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
    public void onDrop(final PlayerDropItemEvent e) {
        if (EEnchant.BONDED.isDisabled()) {
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

        if (m.hasEnchant(EEnchant.BONDED.getEnchantment())) {
            e.setCancelled(true);
        }
    }
}
