package me.m0dii.extraenchants.utils;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnchantListGUI implements InventoryHolder {
    private static final ExtraEnchants plugin = ExtraEnchants.getInstance();
    private static final NamespacedKey ENTRY_KEY = new NamespacedKey(plugin, "ee_list_entry");
    private static final NamespacedKey PAGE_KEY = new NamespacedKey(plugin, "ee_list_page");

    private final Inventory inventory;
    private final int page;
    private final List<ItemStack> entries;

    public EnchantListGUI() {
        this(0);
    }

    public EnchantListGUI(int page) {
        this.page = Math.max(0, page);
        this.entries = buildEntries();
        this.inventory = Bukkit.createInventory(this, 9 * 6, Utils.format("&8&lEnchant List &7(Page " + (this.page + 1) + ")"));
        init();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public void open(@NotNull Player p) {
        p.openInventory(this.inventory);
    }

    public void init() {
        int pageSize = 45;
        int from = this.page * pageSize;
        int to = Math.min(from + pageSize, entries.size());

        for (int i = from; i < to; i++) {
            inventory.addItem(entries.get(i));
        }

        if (this.page > 0) {
            inventory.setItem(45, navItem("&ePrevious Page", this.page - 1));
        }

        if (to < entries.size()) {
            inventory.setItem(53, navItem("&eNext Page", this.page + 1));
        }
    }

    private List<ItemStack> buildEntries() {
        List<ItemStack> list = new ArrayList<>();

        Arrays.stream(EEnchant.values())
                .filter(e -> !e.isDisabled())
                .forEach(e -> {
                    ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
                    ItemMeta meta = item.getItemMeta();
                    String displayName = plugin.getCfg().getString(String.format("enchants.%s.book-display-name", e.getConfigName()));
                    if (meta == null || displayName == null) {
                        return;
                    }

                    meta.setDisplayName(Utils.format(displayName.replace("%level%", "")));
                    List<String> lore = new ArrayList<>();
                    for (String l : plugin.getCfg().getStringList(String.format("enchants.%s.lore", e.getConfigName()))) {
                        String line = l.replace("%level%", "")
                                .replace("%trigger-chance%", e.getTriggerChance() + "%");
                        lore.add(Utils.format(line));
                    }
                    meta.setLore(lore);
                    meta.getPersistentDataContainer().set(ENTRY_KEY, PersistentDataType.STRING, "enum:" + e.name());
                    item.setItemMeta(meta);
                    list.add(item);
                });

        plugin.getCustomEnchantFramework().getDefinitions().values().stream()
                .filter(CustomEnchantDefinition::isEnabled)
                .forEach(definition -> {
                    ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
                    ItemMeta meta = item.getItemMeta();
                    if (meta == null) {
                        return;
                    }

                    String display = definition.getDisplayName();
                    if (display == null || display.isBlank()) {
                        display = definition.getId();
                    }

                    meta.setDisplayName(Utils.format(display));
                    List<String> lore = new ArrayList<>();
                    if (definition.getDescription() != null && !definition.getDescription().isBlank()) {
                        lore.add(Utils.format(definition.getDescription()));
                    }
                    lore.add(Utils.format("&8ID: &7" + definition.getId().toUpperCase()));
                    lore.add(Utils.format("&8Max Level: &7" + definition.getMaxLevel()));
                    meta.setLore(lore);
                    meta.getPersistentDataContainer().set(ENTRY_KEY, PersistentDataType.STRING, "custom:" + definition.getId());
                    item.setItemMeta(meta);
                    list.add(item);
                });

        return list;
    }

    private ItemStack navItem(String name, int targetPage) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.setDisplayName(Utils.format(name));
        meta.getPersistentDataContainer().set(PAGE_KEY, PersistentDataType.INTEGER, targetPage);
        item.setItemMeta(meta);
        return item;
    }

    public static NamespacedKey getEntryKey() {
        return ENTRY_KEY;
    }

    public static NamespacedKey getPageKey() {
        return PAGE_KEY;
    }
}