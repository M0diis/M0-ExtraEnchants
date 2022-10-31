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
import java.util.stream.Stream;

public class CustomCombine implements Listener {

    private final ExtraEnchants plugin;

    public CustomCombine(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCustomCombine(CombineEvent e) {
        ItemStack curr = e.getInventoryClickEvent().getCurrentItem();

        if (curr == null) {
            return;
        }

        Enchantment enchant = e.getEnchant();

        ItemMeta meta = curr.getItemMeta();

        Stream<Enchantment> enchants = meta.getEnchants().keySet().stream();

        int enchantLevel = e.getEnchantLevel();

        Player p = e.getPlayer();

        if (!enchant.canEnchantItem(curr)) {
            p.sendMessage(Utils.format("&cKerėjimas negali būti uždėtas ant šio daikto."));

            return;
        }

        if ((curr.hasItemMeta()) && (meta.hasEnchant(enchant))) {
            return;
        }

        if (enchants.anyMatch(enchant::conflictsWith)) {
            p.sendMessage(Utils.format("&cKerėjimas negali būti uždėtas kartu su žemiau nurodytai kerėjimais:"));

            meta.getEnchants().keySet().stream().filter(enchant::conflictsWith)
                    .forEach((conflict) -> {
                        p.sendMessage(Utils.format( "&8• &7" + conflict.getName().replace("_", " ")));
                    });

            return;
        }

        e.getInventoryClickEvent().setCancelled(true);

        if (combine(e, curr, enchant, enchantLevel)) {
            return;
        }

        e.getInventoryClickEvent().setCursor(null);
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
