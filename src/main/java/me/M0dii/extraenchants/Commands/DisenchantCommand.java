package me.m0dii.extraenchants.commands;

import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.utils.Utils;
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

public class DisenchantCommand implements CommandExecutor {
    private final FileConfiguration cfg;

    public DisenchantCommand(ExtraEnchants plugin) {

        this.cfg = plugin.getCfg();
    }

    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd,
                             @Nonnull String label, @Nonnull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Console cannot perform this command");
            return true;
        }

        if (!player.hasPermission("extraenchants.command.unenchant")) {
            sender.sendMessage(Utils.format(cfg.getString("messages.no-permission")));

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

        CustomEnchants.getAllEnchants().forEach((enchant) -> {
            if (hand.getItemMeta().hasEnchant(enchant)) {
                removeEnchant(sender, hand, enchant, hand, enchant.getName());
            }
        });

        return true;
    }

    private void removeEnchant(@Nonnull CommandSender sender, ItemStack hand, Enchantment ench, ItemStack itemInMainHand, String enchName) {
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

        String removed = cfg.getString("messages.enchant-removed");

        if(removed == null) {
            return;
        }

        sender.sendMessage(Utils.format(removed.replace("%enchant_name%", enchName)));
    }
}