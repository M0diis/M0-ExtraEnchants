package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class PlayerInteract implements Listener {
    private ExtraEnchants plugin;

    public PlayerInteract(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent e) {
        if (!e.hasItem())
            return;

        if (!e.getItem().hasItemMeta())
            return;

        if (!e.getItem().getType().equals(Material.ENCHANTED_BOOK))
            return;

        ItemMeta meta = e.getItem().getItemMeta();

        if (CustomEnchants.getAllEnchants().stream().allMatch(ench -> (!meta.hasEnchant(ench))))
            return;

        e.getPlayer().sendMessage(Utils.format(e.getItem().getItemMeta().getDisplayName()));

        for (String l : meta.getLore()) {
            e.getPlayer().sendMessage(Utils.format(l));
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantUpdateGrindstoneClick(InventoryClickEvent e) {
        Inventory inventory = e.getInventory();
        if (inventory.getType() != InventoryType.GRINDSTONE) return;
        if (e.getRawSlot() == 2) return;
        this.updateGrindstone(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantUpdateGrindstoneDrag(InventoryDragEvent e) {
        Inventory inventory = e.getInventory();
        if (inventory.getType() != InventoryType.GRINDSTONE) return;
        this.updateGrindstone(inventory);
    }

    private void updateGrindstone(@NotNull Inventory inventory) {
        this.plugin.getServer().getScheduler().runTask(plugin, () -> {
            ItemStack result = inventory.getItem(2);

            if (result == null || result.getType().isAir()) return;

            CustomEnchants.getAllEnchants().forEach(enchantment -> {
                ItemMeta meta = result.getItemMeta();

                result.removeEnchantment(enchantment);

                List<String> lore = meta.getLore();

                if(lore != null) {
                    lore.removeIf(s -> s.toUpperCase().contains(enchantment.getName().toUpperCase()));
                }

                meta.setLore(lore);

                result.setItemMeta(meta);
            });
        });
    }
}