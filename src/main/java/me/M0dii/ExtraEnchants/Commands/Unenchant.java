package me.M0dii.ExtraEnchants.Commands;

import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import me.M0dii.ExtraEnchants.ExtraEnchants;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Unenchant implements CommandExecutor {
    private final FileConfiguration cfg;

    public Unenchant(ExtraEnchants plugin) {

        this.cfg = plugin.getCfg();
    }

    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd,
                             @Nonnull String label, @Nonnull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Console cannot perform this command");
            return true;
        }

        if (!player.hasPermission("extraenchants.command.unenchant")) {
            sender.sendMessage(this.format(this.cfg.getString("messages.no-permission")));

            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand == null) {
            sender.sendMessage(ChatColor.RED + "Must be holding an item to do this.");

            return true;
        }

        if (hand.getItemMeta() == null) {
            sender.sendMessage(ChatColor.RED + "Must be holding an enchanted item to do this.");

            return true;
        }

        removeEnchant(sender, hand, CustomEnchants.SMELT, hand, "Smelt");

        removeEnchant(sender, hand, CustomEnchants.TELEPATHY, hand, "Telepathy");

        removeEnchant(sender, hand, CustomEnchants.PLOW, hand, "Plow");

        removeEnchant(sender, hand, CustomEnchants.BEHEADING, hand, "Beheading");

        removeEnchant(sender, hand, CustomEnchants.BONDED, hand, "Bonded");

        removeEnchant(sender, hand, CustomEnchants.LAVA_WALKER, hand, "Lava Walker");

        removeEnchant(sender, hand, CustomEnchants.VEIN_MINER, hand, "Vein Miner");

        removeEnchant(sender, hand, CustomEnchants.HASTE_MINER, hand, "Haste Miner");

        return true;
    }

    private void removeEnchant(@Nonnull CommandSender sender, ItemStack hand, Enchantment ench, ItemStack itemInMainHand, String enchName) {
        if (hand.getItemMeta().hasEnchant(ench)) {
            hand.removeEnchantment(ench);

            ItemMeta meta = itemInMainHand.getItemMeta();

            List<String> lore = new ArrayList<>();

            if (meta.getLore() != null) {
                for (String l : meta.getLore()) {
                    if (!l.contains(enchName))
                        lore.add(l);
                }

                meta.setLore(lore);
            }

            hand.setItemMeta(meta);

            sender.sendMessage(this.format(this.cfg.getString("messages.enchant-removed")
                    .replace("%enchant_name%", enchName)));
        }
    }

    public String format(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}