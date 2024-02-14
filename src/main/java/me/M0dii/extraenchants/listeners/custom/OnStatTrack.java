package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.StatTrackEvent;
import me.m0dii.extraenchants.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class OnStatTrack implements Listener {
    private static final NamespacedKey key = new NamespacedKey(ExtraEnchants.getInstance(), "memory_blocks");

    private final ExtraEnchants plugin;

    public OnStatTrack(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onStatTrack(final StatTrackEvent e) {
        if (!Utils.shouldTrigger(EEnchant.STAT_TRACK)) {
            return;
        }

        ItemStack tool = e.getPlayer().getInventory().getItemInMainHand();

        if (tool.getType().equals(Material.AIR)) {
            return;
        }

        ItemMeta meta = tool.getItemMeta();

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        int memory = pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);

        pdc.set(key, PersistentDataType.INTEGER, memory + 1);

        List<Component> currentLore = meta.lore();

        if (currentLore == null || currentLore.isEmpty()) {
            return;
        }

        int memoryLineIndex = -1;

        for (int i = 0; i < currentLore.size(); i++) {
            Component line = currentLore.get(i);

            if (Utils.stripColor(line).contains("• Mined:")) {
                memoryLineIndex = i;
                break;
            }
        }

        if (memoryLineIndex == -1) {
            currentLore.add(Component.text(" "));
            currentLore.add(Utils.colorize("&8• &7Mined: &3" + (memory + 1)));
        } else {
            currentLore.remove(memoryLineIndex);
            currentLore.add(memoryLineIndex, Utils.colorize("&8• &7Mined: &3" + (memory + 1)));
        }

        meta.lore(currentLore);

        tool.setItemMeta(meta);
    }
}