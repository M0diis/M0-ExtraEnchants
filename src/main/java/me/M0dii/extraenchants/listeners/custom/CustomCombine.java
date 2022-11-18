package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.events.CombineEvent;
import me.m0dii.extraenchants.utils.Utils;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CustomCombine implements Listener {

    private final ExtraEnchants plugin;

    public CustomCombine(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCustomCombine(final CombineEvent e) {
        ItemStack curr = e.getInventoryClickEvent().getCurrentItem();

        if (curr == null) {
            return;
        }

        Enchantment enchant = e.getEnchant();

        ItemMeta meta = curr.getItemMeta();

        Set<Enchantment> enchants = meta.getEnchants().keySet();

        int enchantLevel = e.getEnchantLevel();

        Player p = e.getPlayer();

        if (!enchant.canEnchantItem(curr)) {
            p.sendMessage(Utils.format(plugin.getCfg().getString("messages.cannot-enchant-item")));

            return;
        }

        removeLowerLevel(curr, enchant, meta, enchants, enchantLevel);

        if ((curr.hasItemMeta()) && (meta.hasEnchant(enchant))) {
            return;
        }

        if (hasConflict(enchant, meta, enchants, p)) {
            return;
        }

        e.getInventoryClickEvent().setCancelled(true);

        if (combine(e, curr, enchant, enchantLevel)) {
            return;
        }

        e.getInventoryClickEvent().setCursor(null);
    }

    private boolean hasConflict(Enchantment enchant, ItemMeta meta, Set<Enchantment> enchants, Player p) {
        if (enchants.stream().anyMatch(enchant::conflictsWith)) {
            p.sendMessage(Utils.format(plugin.getCfg().getString("messages.enchant-conflicts")));

            meta.getEnchants().keySet().stream().filter(enchant::conflictsWith)
                    .forEach((conflict) -> {
                        p.sendMessage(Utils.format( "&8• &7" + conflict.getName().replace("_", " ")));
                    });

            return true;
        }
        return false;
    }

    private void removeLowerLevel(ItemStack curr, Enchantment newEnchant, ItemMeta meta, Set<Enchantment> enchants, int enchantLevel) {
        enchants.stream()
                .filter(enchantment -> enchantment.equals(newEnchant))
                .findFirst()
                .ifPresent((oldEnchant -> {
            int currentLevel = meta.getEnchantLevel(oldEnchant);

            if(enchantLevel <= currentLevel) {
                return;
            }

            meta.removeEnchant(oldEnchant);

            List<String> lore = new ArrayList<>();

            if (meta.getLore() != null) {
                for (String l : meta.getLore()) {
                    if (!l.contains(oldEnchant.getName()))
                        lore.add(l);
                }

                meta.setLore(lore);
            }

            curr.setItemMeta(meta);
        }));
    }

    private boolean combine(CombineEvent event, ItemStack curr, Enchantment enchant, int level) {
        ItemStack tool = new ItemStack(curr.getType());

        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.GRAY + PlainTextComponentSerializer.plainText().serialize(enchant.displayName(level)));

        ItemMeta meta = curr.getItemMeta();

        if (meta.getLore() != null) {
            for (String l : meta.getLore()) {
                lore.add(Utils.format(l));
            }
        }

        meta.setLore(lore);
        tool.setItemMeta(meta);

        tool.addUnsafeEnchantment(enchant, level);

        if (curr.getAmount() > 1) {
            curr.setAmount(curr.getAmount() - 1);
            event.getInventoryClickEvent().setCursor(tool);

            return true;
        }

        event.getInventoryClickEvent().setCurrentItem(tool);

        return false;
    }
}
