package me.m0dii.extraenchants.commands;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class UnenchantCommand extends Command {
    private final FileConfiguration cfg;

    public UnenchantCommand(ExtraEnchants plugin) {
        super("unenchant");
        this.cfg = plugin.getCfg();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Console cannot perform this command");
            return true;
        }

        if (!player.hasPermission("extraenchants.command.unenchant")) {
            sender.sendMessage(Utils.format(cfg.getString("messages.no-permission")));

            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType().isAir()) {
            sender.sendMessage(ChatColor.RED + "Must be holding an item to do this.");

            return true;
        }

        if (hand.getItemMeta() == null) {
            sender.sendMessage(ChatColor.RED + "Must be holding an enchanted item to do this.");

            return true;
        }

        CustomEnchants.getAllEnchants().forEach((enchant) -> {
            if (InventoryUtils.hasEnchant(hand, enchant)) {
                removeEnchant(sender, hand, enchant, hand, enchant.getName());
            }
        });

        return true;
    }

    private void removeEnchant(@Nonnull CommandSender sender, ItemStack hand, Enchantment ench, ItemStack itemInMainHand, String enchName) {
        ItemMeta meta = itemInMainHand.getItemMeta();

        hand.removeEnchantment(ench);
        meta.removeEnchant(ench);

        List<String> lore;

        if (meta.getLore() != null) {
            lore = meta.getLore().stream()
                    .filter(l -> !l.contains(enchName))
                    .collect(Collectors.toList());

            meta.setLore(lore);
        }

        hand.setItemMeta(meta);

        String removed = cfg.getString("messages.enchant-removed");

        if (removed == null) {
            return;
        }

        sender.sendMessage(Utils.format(removed.replace("%enchant_name%", enchName)));
    }
}