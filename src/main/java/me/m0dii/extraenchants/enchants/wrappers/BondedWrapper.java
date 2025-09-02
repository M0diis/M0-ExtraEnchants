package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@EnchantWrapper(name = "Bonded", maxLevel = 1)
public class BondedWrapper extends CustomEnchantment {

    public BondedWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ALL;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.values());
    }

    private final Map<UUID, List<ItemStack>> keptItems = new HashMap<>();

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        if (!keptItems.containsKey(p.getUniqueId())) {
            return;
        }

        List<ItemStack> kept = keptItems.get(p.getUniqueId());

        List<ItemStack> updated = new ArrayList<>();

        if (kept != null && kept.size() > 0) {
            for (ItemStack item : kept) {
                ItemStack copy = item.clone();

                if (!copy.hasItemMeta()) {
                    continue;
                }

                ItemMeta m = copy.getItemMeta();

                List<String> lore = m.getLore();

                if (lore != null && lore.size() > 0)
                    for (int i = 0; i < lore.size(); i++) {
                        String s = lore.get(i);

                        if (ChatColor.stripColor(s).startsWith("Bonded")) {
                            lore.remove(s);
                        }
                    }

                m.setLore(lore);
                copy.setItemMeta(m);

                updated.add(copy);
            }
        }

        for (ItemStack item : updated) {
            p.getInventory().addItem(item);
        }

        keptItems.remove(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeath(PlayerDeathEvent e) {
        List<ItemStack> drops = e.getDrops();

        Player p = e.getEntity().getPlayer();

        if (p == null) {
            return;
        }

        List<ItemStack> bondedItems = new ArrayList<>();

        for (int i = 0; i < drops.size(); i++) {
            ItemStack item = drops.get(i);

            if (item.hasItemMeta() && item.getItemMeta() != null) {
                ItemMeta meta = item.getItemMeta();

                if (InventoryUtils.hasEnchant(item, EEnchant.BONDED.getEnchantment())) {
                    drops.remove(item);
                    meta.removeEnchant(EEnchant.BONDED.getEnchantment());
                    item.setItemMeta(meta);
                    bondedItems.add(item);
                }
            }
        }

        keptItems.put(p.getUniqueId(), bondedItems);
    }

    @EventHandler
    public void onDrop(final PlayerDropItemEvent e) {
        if (EEnchant.BONDED.isDisabled()) {
            return;
        }

        ItemStack i = e.getItemDrop().getItemStack();

        if (i.getType() == Material.ENCHANTED_BOOK) {
            return;
        }

        if (!i.hasItemMeta()) {
            return;
        }

        ItemMeta m = i.getItemMeta();

        if (m == null) {
            return;
        }

        if (m.hasEnchant(EEnchant.BONDED.getEnchantment())) {
            e.setCancelled(true);
        }
    }
}
