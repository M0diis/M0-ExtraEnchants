package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.events.CombineEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;
import java.util.stream.Stream;

public class EnchantmentCombine implements Listener {
    @EventHandler
    public void onCombine(final InventoryClickEvent e) {
        ItemStack cursor = e.getCursor();

        if (cursor == null) {
            return;
        }

        if (!e.getCursor().hasItemMeta()) {
            return;
        }

        if (!cursor.getType().equals(Material.ENCHANTED_BOOK)) {
            return;
        }

        if (!cursor.getItemMeta().hasEnchants()) {
            return;
        }

        ItemMeta meta = cursor.getItemMeta();

        if (meta == null) {
            return;
        }

        if (e.getAction() != InventoryAction.SWAP_WITH_CURSOR) {
            return;
        }

        Player p = (Player) e.getWhoClicked();

        CustomEnchants.getAllEnchants()
                .stream()
                .filter(meta::hasEnchant)
                .findFirst()
                .ifPresent(enchantment -> combine(cursor, p, enchantment, e));
    }

    private void combine(ItemStack cursor, Player p, Enchantment enchantment, InventoryClickEvent e) {
        if (cursor != null && cursor.getItemMeta().hasEnchant(enchantment)) {
            int level = cursor.getEnchantmentLevel(enchantment);

            Bukkit.getPluginManager().callEvent(new CombineEvent(p, e, enchantment, level));
        }
    }
}