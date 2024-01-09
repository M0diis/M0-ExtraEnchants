package me.m0dii.extraenchants.listeners;

import com.google.common.base.Preconditions;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.CombineEvent;
import me.m0dii.extraenchants.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class InventoryClick implements Listener {
    private static final NamespacedKey enchantKey = new NamespacedKey(ExtraEnchants.getInstance(), "extraenchants_enchant");
    private static final NamespacedKey enchantLevelKey = new NamespacedKey(ExtraEnchants.getInstance(), "extraenchants_enchant_level");
    private static final Set<InventoryAction> ITEM_TAKE_ACTIONS = Collections.unmodifiableSet(EnumSet.of(InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ALL, InventoryAction.COLLECT_TO_CURSOR, InventoryAction.HOTBAR_SWAP, InventoryAction.MOVE_TO_OTHER_INVENTORY));
    /**
     * Holds all the actions that should be considered "place" actions
     */
    private static final Set<InventoryAction> ITEM_PLACE_ACTIONS = Collections.unmodifiableSet(EnumSet.of(InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ALL));
    /**
     * Holds all actions relating to swapping items
     */
    private static final Set<InventoryAction> ITEM_SWAP_ACTIONS = Collections.unmodifiableSet(EnumSet.of(InventoryAction.HOTBAR_SWAP, InventoryAction.SWAP_WITH_CURSOR, InventoryAction.HOTBAR_MOVE_AND_READD));
    /**
     * Holds all actions relating to dropping items
     */
    private static final Set<InventoryAction> ITEM_DROP_ACTIONS = Collections.unmodifiableSet(EnumSet.of(InventoryAction.DROP_ONE_SLOT, InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ALL_CURSOR));
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

        for (Enchantment ench : cursor.getEnchantments().keySet()) {
            Messenger.debug("Meta has enchantment: " + ench.getKey().getKey());
        }

        CustomEnchants.getAllEnchants()
                .stream()
                .filter(meta::hasEnchant)
                .findFirst()
                .ifPresent(enchantment -> {
                    Bukkit.getPluginManager().callEvent(
                            new CombineEvent(p, e, enchantment, meta.getEnchantLevel(enchantment)));
                });
    }

    @EventHandler
    public void onInventoryClickEnchantList(final InventoryClickEvent e) {
        Inventory clickedInv = e.getClickedInventory();

        if (clickedInv == null) {
            return;
        }

        if (clickedInv.getHolder() instanceof EnchantListGUI list) {
            e.setCancelled(true);

            Player p = (Player) e.getWhoClicked();

            ItemStack item = e.getCurrentItem();

            if (item == null) {
                return;
            }

            ItemMeta meta = item.getItemMeta();

            if (meta == null) {
                return;
            }

            String name = Utils.stripColor(meta.displayName());

            if (name == null) {
                return;
            }

            EEnchant enchant = EEnchant.parse(name);

            if (enchant == null) {
                return;
            }

            EnchantInfoGUI gui = new EnchantInfoGUI(enchant.getEnchantment());

            gui.open(p);
        }
    }

    @EventHandler
    public void onInventoryClickEnchantInfo(final InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof final EnchantInfoGUI gui)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();

        if (clicked != null && clicked.getType().equals(Material.PAPER)) {
            Player p = (Player) event.getWhoClicked();

            EnchantListGUI list = new EnchantListGUI();

            list.open(p);

            return;
        }

        if ((isPlaceItemEvent(event)) || (!isTakeItemEvent(event)) || (isSwapItemEvent(event))
                || (isDropItemEvent(event)) || (!isOtherEvent(event))) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    private boolean isTakeItemEvent(final InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        if (clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER || inventory.getType() == InventoryType.PLAYER) {
            return false;
        }

        return action == InventoryAction.MOVE_TO_OTHER_INVENTORY || isTakeAction(action);
    }

    private boolean isPlaceItemEvent(final InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                && clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER
                && inventory.getType() != clickedInventory.getType()) {
            return true;
        }

        return isPlaceAction(action)
                && (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER)
                && inventory.getType() != InventoryType.PLAYER;
    }

    private boolean isSwapItemEvent(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        return isSwapAction(action)
                && (clickedInventory == null || clickedInventory.getType() != InventoryType.PLAYER)
                && inventory.getType() != InventoryType.PLAYER;
    }

    private boolean isDropItemEvent(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        return isDropAction(action)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER);
    }

    private boolean isOtherEvent(final InventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        final Inventory inventory = event.getInventory();
        final Inventory clickedInventory = event.getClickedInventory();
        final InventoryAction action = event.getAction();

        return isOtherAction(action)
                && (clickedInventory != null || inventory.getType() != InventoryType.PLAYER);
    }

    private boolean isDraggingOnGui(final InventoryDragEvent event) {
        final int topSlots = event.getView().getTopInventory().getSize();
        return event.getRawSlots().stream().anyMatch(slot -> slot < topSlots);
    }

    private boolean isTakeAction(final InventoryAction action) {
        return ITEM_TAKE_ACTIONS.contains(action);
    }

    private boolean isPlaceAction(final InventoryAction action) {
        return ITEM_PLACE_ACTIONS.contains(action);
    }

    private boolean isSwapAction(final InventoryAction action) {
        return ITEM_SWAP_ACTIONS.contains(action);
    }

    private boolean isDropAction(final InventoryAction action) {
        return ITEM_DROP_ACTIONS.contains(action);
    }

    private boolean isOtherAction(final InventoryAction action) {
        return action == InventoryAction.CLONE_STACK || action == InventoryAction.UNKNOWN;
    }

    @EventHandler
    public void onInventoryDragEnchantInfo(final InventoryDragEvent e) {
        Inventory clickedInv = e.getInventory();

        if (!(clickedInv.getHolder() instanceof EnchantInfoGUI list)) {
            return;
        }

        e.setCancelled(true);

        ItemStack clicked = e.getOldCursor();

        if (!clicked.getType().equals(Material.PAPER)) {
            return;
        }

        Player p = (Player) e.getWhoClicked();

        EnchantListGUI gui = new EnchantListGUI();

        gui.open(p);
    }

    @EventHandler
    public void onInventoryDragEnchantList(final InventoryDragEvent e) {
        Inventory clickedInv = e.getInventory();

        if (clickedInv.getHolder() instanceof EnchantListGUI list) {
            e.setCancelled(true);

            Player p = (Player) e.getWhoClicked();

            ItemStack item = e.getOldCursor();

            if (item == null) {
                return;
            }

            ItemMeta meta = item.getItemMeta();

            if (meta == null) {
                return;
            }

            String name = Utils.stripColor(meta.displayName());

            if (name == null) {
                return;
            }

            Enchantment enchant = EEnchant.toEnchant(name);

            if (enchant == null) {
                return;
            }

            EnchantInfoGUI gui = new EnchantInfoGUI(enchant);

            gui.open(p);
        }
    }

    @EventHandler
    public void onInventoryClickFixEnchant(final InventoryClickEvent e) {
        ItemStack item = e.getCursor();

        if (item == null) {
            Messenger.debug("Item is null.");
            return;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        }

        if (meta == null) {
            Messenger.debug("Meta is null.");
            return;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (!pdc.has(enchantKey, PersistentDataType.STRING)) {
            Messenger.debug("Item does not have enchant key.");
            return;
        }

        String enchantName = pdc.get(enchantKey, PersistentDataType.STRING);

        EEnchant enchant = EEnchant.parse(enchantName);

        if (enchant == null) {
            Messenger.debug("Failed to parse enchant.");
            return;
        }

        if (InventoryUtils.hasEnchant(item, enchant)) {
            Messenger.debug("Item already has enchant.");
            return;
        }

        int level = pdc.getOrDefault(enchantLevelKey, PersistentDataType.INTEGER, 1);

        Enchanter.applyEnchantWithoutLore(item, enchant, level);
    }
}