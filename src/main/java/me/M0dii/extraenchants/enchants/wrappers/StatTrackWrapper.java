package me.m0dii.extraenchants.enchants.wrappers;


import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.StatTrackEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@EnchantWrapper(name = "Stat Track", maxLevel = 1)
public class StatTrackWrapper extends CustomEnchantment {

    public StatTrackWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isTool(item, false) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
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
