package me.m0dii.extraenchants.commands;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.utils.Enchanter;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class EnchantCommand implements CommandExecutor, TabCompleter {
    private final FileConfiguration cfg;

    public EnchantCommand(ExtraEnchants plugin) {
        this.cfg = plugin.getCfg();
    }

    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd,
                             @Nonnull String label, @Nonnull String[] args) {
        if (!sender.hasPermission("extraenchants.command.enchant")) {
            sender.sendMessage(Utils.format(this.cfg.getString("messages.no-permission")));

            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Utils.format(this.cfg.getString("messages.usage")));

            return true;
        }

        if (args.length == 1 && !(sender instanceof Player)) {
            sender.sendMessage(Utils.format(this.cfg.getString("messages.usage")));

            return true;
        }

        if (sender instanceof Player target) {
            if (args.length == 2) {
                target = Bukkit.getPlayer(args[1]);
            }

            String ench = args[0].replace("_", "");

            int level = 1;

            try {
                if(args.length == 3) {
                    level = Integer.parseInt(args[2]);
                }
            } catch (NumberFormatException ignored) { }

            if (giveTargetBook(sender, target, ench, level)) {
                return true;
            }

            return true;
        }
        else {
            Player target = Bukkit.getPlayer(args[1]);

            if(target == null) {
                sender.sendMessage(Utils.format("Nenurodytas žaidėjas!"));

                return true;
            }

            String enchant = args[0].replace("_", "");

            int level = 1;

            try {
                if(args.length >= 3) {
                    level = Integer.parseInt(args[2]);
                }
            } catch (NumberFormatException ignored) { }

            if (giveTargetBook(sender, target, enchant, level)) {
                return true;
            }
        }

        return true;
    }

    private boolean giveTargetBook(@Nonnull CommandSender sender, Player target, String ench, int level) {
        if (giveBook(ench, target,  level))
            return true;

        sender.sendMessage(Utils.format(cfg.getString("messages.enchantment-list")));

        return false;
    }

    private boolean giveBook(String arg0, Player player, int level) {
        if (player == null) {
            return false;
        }

        ItemStack item = Enchanter.getBook(arg0, level);

        if(item == null) {
            return false;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        } else {
            player.getInventory().addItem(item);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd,
                                      @Nonnull String alias, @Nonnull String[] args) {
        List<String> completes = new ArrayList<>();

        if (args.length == 1) {
            CustomEnchants.getAllEnchants()
                    .stream()
                    .map(s -> s.getKey().getKey().toLowerCase())
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .forEach(completes::add);
        }

        if (args.length == 2) {
            Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .forEach(completes::add);
        }

        if(args.length == 3) {
            completes.add("1");
            completes.add("2");
        }

        return completes;
    }
}