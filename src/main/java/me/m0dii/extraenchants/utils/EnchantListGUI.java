package me.m0dii.extraenchants.utils;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnchantListGUI implements InventoryHolder {
    private static final ExtraEnchants plugin = ExtraEnchants.getInstance();

    private final Inventory inventory;

    public EnchantListGUI() {
        this.inventory = Bukkit.createInventory(this, 9 * 3, Utils.format("&8&lEnchant List"));

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
        Arrays.stream(EEnchant.values())
                .filter(e -> !e.isDisabled())
                .forEach(e -> {
                    ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
                    ItemMeta meta = item.getItemMeta();
                    String displayName = plugin.getCfg().getString(String.format("enchants.%s.book-display-name", e.getConfigName()));
                    if (displayName == null) {
                        return;
                    }
                    meta.setDisplayName(Utils.format(
                            displayName.replace("%level%", ""))
                    );
                    List<String> lore = new ArrayList<>();
                    for (String l : plugin.getCfg().getStringList(String.format("enchants.%s.lore", e.getConfigName()))) {
                        String line = l.replace("%level%", "")
                                .replace("%trigger-chance%", e.getTriggerChance() + "%");

                        lore.add(Utils.format(line));
                    }
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    this.inventory.addItem(item);
                });
    }
}