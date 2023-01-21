package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.CombineEvent;
import me.m0dii.extraenchants.utils.EnchantInfoGUI;
import me.m0dii.extraenchants.utils.EnchantListGUI;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class InventoryClick implements Listener {

    private final ExtraEnchants plugin;

    public InventoryClick(ExtraEnchants plugin) {

        this.plugin = plugin;
    }

    @EventHandler
    public void onCombine(final InventoryClickEvent e) {
        ItemStack cursor = e.getCursor();

        if (cursor == null) {
            return;
        }

        Messenger.debug("Cursor: " + cursor.getType());

        if (!e.getCursor().hasItemMeta()) {
            return;
        }

        Messenger.debug("Cursor has meta.");

        if (!cursor.getType().equals(Material.ENCHANTED_BOOK)) {
            return;
        }

        Messenger.debug("Cursor is enchanted book.");

        ItemMeta meta = cursor.getItemMeta();

        if (meta == null) {
            return;
        }

        Messenger.debug("Cursor meta is not null.");

        if (e.getAction() != InventoryAction.SWAP_WITH_CURSOR) {
            return;
        }

        Messenger.debug("Action is swap with cursor.");

        Player p = (Player) e.getWhoClicked();

        for(Enchantment ench : cursor.getEnchantments().keySet())
        {
            Messenger.debug("Meta has enchantment: " + ench.getKey().getKey());
        }

        CustomEnchants.getAllEnchants()
                .stream()
                .filter(meta::hasEnchant)
                .findFirst()
                .ifPresent(enchantment -> {
                    Bukkit.getPluginManager().callEvent(
                            new CombineEvent(p, e, enchantment, cursor.getEnchantmentLevel(enchantment)));
                });
    }

    @EventHandler
    public void onInventoryClickEnchantList(final InventoryClickEvent e) {
        Inventory clickedInv = e.getClickedInventory();

        if(clickedInv == null) {
            return;
        }

        if(clickedInv.getHolder() instanceof EnchantListGUI list) {
            e.setCancelled(true);

            Player p = (Player)e.getWhoClicked();

            ItemStack item = e.getCurrentItem();

            if(item == null) {
                return;
            }

            ItemMeta meta = item.getItemMeta();

            if(meta == null) {
                return;
            }

            String name = Utils.stripColor(meta.displayName());

            if(name == null) {
                return;
            }

            EEnchant enchant = EEnchant.parse(name);

            if(enchant == null) {
                return;
            }

            EnchantInfoGUI gui = new EnchantInfoGUI(enchant.getEnchantment());

            gui.open(p);
        }
    }

    @EventHandler
    public void onInventoryClickEnchantInfo(final InventoryClickEvent e) {
        Inventory clickedInv = e.getClickedInventory();

        if(clickedInv == null) {
            return;
        }

        if (!(clickedInv.getHolder() instanceof EnchantInfoGUI)) {
            return;
        }

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || !clicked.getType().equals(Material.PAPER)) {
            return;
        }

        Player p = (Player)e.getWhoClicked();

        EnchantListGUI gui = new EnchantListGUI();

        gui.open(p);
    }

    @EventHandler
    public void onInventoryDragEnchantInfo(final InventoryDragEvent e) {
        Inventory clickedInv = e.getInventory();

        if (!(clickedInv.getHolder() instanceof EnchantInfoGUI list)) {
            return;
        }

        e.setCancelled(true);

        ItemStack clicked = e.getOldCursor();

        if (clicked == null || !clicked.getType().equals(Material.PAPER)) {
            return;
        }

        Player p = (Player)e.getWhoClicked();

        EnchantListGUI gui = new EnchantListGUI();

        gui.open(p);
    }

    @EventHandler
    public void onInventoryDragEnchantList(final InventoryDragEvent e) {
        Inventory clickedInv = e.getInventory();

        if(clickedInv.getHolder() instanceof EnchantListGUI list) {
            e.setCancelled(true);

            Player p = (Player)e.getWhoClicked();

            ItemStack item = e.getOldCursor();

            if(item == null) {
                return;
            }

            ItemMeta meta = item.getItemMeta();

            if(meta == null) {
                return;
            }

            String name = Utils.stripColor(meta.displayName());

            if(name == null) {
                return;
            }

            Enchantment enchant = EEnchant.toEnchant(name);

            if(enchant == null) {
                return;
            }

            EnchantInfoGUI gui = new EnchantInfoGUI(enchant);

            gui.open(p);
        }
    }

    @EventHandler
    public void onInventoryClickFixEnchant(final InventoryClickEvent e) {
        ItemStack item = e.getCursor();

        if(item == null) {
            Messenger.debug("Item is null.");
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if(meta == null) {
            Messenger.debug("Item meta is null.");
            return;
        }

        List<Component> lore = meta.lore();

        if(lore == null || lore.isEmpty()) {
            Messenger.debug("Item lore is null or empty.");
            return;
        }

        for (Component comp : lore) {
            try {
                String text = Utils.stripColor(comp);

                Messenger.debug("Stripped color: " + text);

                String[] split = text.split(" ");

                String level = split[split.length - 1];

                String enchantName = text.substring(0, text.length() - level.length() - 1);

                Messenger.debug("Enchant name: " + enchantName);

                Enchantment enchantment = EEnchant.toEnchant(enchantName);

                if (enchantment == null || meta.hasEnchant(enchantment)) {
                    continue;
                }

                meta.addEnchant(enchantment, 1, true);

                item.setItemMeta(meta);
            } catch (Exception ignored) { }
        }
    }
}