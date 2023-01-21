package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.utils.Enchanter;
import me.m0dii.extraenchants.utils.Messenger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;

public class EnchantItem implements Listener {
    private final static Random rnd = new Random();
    private final ExtraEnchants plugin;

    public EnchantItem(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEnchantPopulateEnchantingTable(final EnchantItemEvent e) {
        Messenger.debug("EnchantItemEvent called.");

        ItemStack target = e.getItem();

        if (e.getExpLevelCost() < 30) {
            return;
        }

        EEnchant eenchant = Arrays.stream(EEnchant.values())
                .filter(o -> o.getEnchantChance() != -1)
                .filter(o -> !o.isDisabled())
                .filter(o -> o.canEnchantItemCustom(target))
                .filter(o -> rnd.nextInt(100) < o.getEnchantChance())
                .findFirst()
                .orElse(null);

        if (eenchant == null) {
            return;
        }

        if (e.getEnchantsToAdd().keySet()
                .stream()
                .anyMatch(add -> eenchant.conflictsWith(add)
                       || add.conflictsWith(eenchant.getEnchantment()))) {
            return;
        }

        e.getEnchantsToAdd().put(eenchant.getEnchantment(), 1);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            ItemStack result = e.getInventory().getItem(0);

            if (result == null) {
                return;
            }

            ItemMeta meta = result.getItemMeta();

            if (meta instanceof EnchantmentStorageMeta tableMeta) {
                e.getEnchantsToAdd().forEach((en, lvl) -> {
                    if (!tableMeta.hasStoredEnchant(en)) {
                        tableMeta.addStoredEnchant(en, lvl, true);
                    }
                });
                result.setItemMeta(tableMeta);
            }

            Enchanter.applyEnchant(result, eenchant, 1, true);

            e.getInventory().setItem(0, result);
        });
    }
}
