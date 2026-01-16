package me.m0dii.extraenchants.listeners;


import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.utils.Messenger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LootEnchantmentFilter implements Listener {

    private final ExtraEnchants plugin;

    public LootEnchantmentFilter(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        List<ItemStack> loot = event.getLoot();
        ArrayList<ItemStack> lootCopy = new ArrayList<>(loot);

        for (ItemStack item : lootCopy) {
            if (item == null) continue;

            ItemMeta meta = item.getItemMeta();

            if (meta == null) continue;

            if (meta instanceof EnchantmentStorageMeta storageMeta) {
                storageMeta.getStoredEnchants().forEach((ench, level) -> {
                    if ("custom".equals(ench.getKey().getNamespace()) || ench.getKey().getNamespace().contains("custom")) {
                        loot.remove(item);
                        Messenger.debug("Excluded item with custom enchantment from loot: " + item);
                    }
                });
            }
        }
    }
}