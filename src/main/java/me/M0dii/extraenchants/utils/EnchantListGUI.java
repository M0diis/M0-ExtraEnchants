package me.m0dii.extraenchants.utils;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchants;
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
import java.util.List;

public class EnchantListGUI implements InventoryHolder {
    private final static ExtraEnchants plugin = ExtraEnchants.getPlugin(ExtraEnchants.class);
    private final Inventory inventory;

    public EnchantListGUI()
    {
        this.inventory = Bukkit.createInventory(this, 9 * 3, Utils.format("&8&lEnchant List"));

        init();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public void open(Player p)
    {
        p.openInventory(this.inventory);
    }

    public void init() {
        EEnchant[] enchants = EEnchant.values();

        for(int i = 0; i < enchants.length; i++)
        {
            EEnchant e = enchants[i];

            if(e.isDisabled()) {
                continue;
            }

            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);

            ItemMeta meta = item.getItemMeta();

            String displayName = plugin.getCfg().getString(String.format("enchants.%s.displayname", e.getConfigName()));

            if(displayName == null) {
                continue;
            }

            meta.setDisplayName(Utils.format(
                    displayName.replace("%level%", ""))
            );

            List<String> lore = new ArrayList<>();

            for (String l : plugin.getCfg().getStringList(String.format("enchants.%s.lore", e.getConfigName()))) {
                lore.add(Utils.format(l.replace("%level%", "")));
            }

            meta.setLore(lore);

            item.setItemMeta(meta);

            this.inventory.addItem(item);
        }
    }
}
