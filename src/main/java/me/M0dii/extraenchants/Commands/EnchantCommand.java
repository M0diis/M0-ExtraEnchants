package me.m0dii.extraenchants.commands;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.utils.EnchantListGUI;
import me.m0dii.extraenchants.utils.Enchanter;
import me.m0dii.extraenchants.utils.Messenger;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        if (isArgument(0, args, "list") && sender instanceof Player player) {
            EnchantListGUI list = new EnchantListGUI();

            list.open(player);

            return true;
        }

        if (isArgument(0, args, "apply") && sender instanceof Player target) {
            if (!sender.hasPermission("extraenchants.command.apply")) {
                sender.sendMessage(Utils.format(cfg.getString("messages.no-permission")));

                return true;
            }

            String enchantName = args[1].replace("_", "");

            EEnchant enchant = EEnchant.parse(enchantName);

            if(enchant == null) {
                sender.sendMessage(Utils.format(cfg.getString("messages.enchantment-list")));

                return true;
            }

            Enchantment enchantment = enchant.getEnchantment();

            if(enchantment == null) {
                sender.sendMessage(Utils.format(cfg.getString("messages.enchantment-list")));

                return true;
            }

            ItemStack item = target.getInventory().getItemInMainHand();

            if(item.getType() == Material.AIR) {
                sender.sendMessage(Utils.format("&cYou must hold an item in your hand."));

                return true;
            }

            Enchanter.applyEnchant(item, enchantment, 1, false);

            sender.sendMessage(Utils.format("&aSuccessfully applied enchantment to item."));

            return true;
        }

        // /extraenchants give <player> <enchantment> <level>
        if(isArgument(0, args, "give") && args.length >= 3) {
            if (!sender.hasPermission("extraenchants.command.give")) {
                sender.sendMessage(Utils.format(cfg.getString("messages.no-permission")));

                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);

            String enchantName = args[2].replace("_", "");

            if(target == null) {
                sender.sendMessage(Utils.format("&cPlayer not found."));

                return true;
            }

            int level = 1;

            try {
                if(args.length >= 4) {
                    level = Integer.parseInt(args[3]);
                }
            } catch (NumberFormatException ignored) { }

            if (giveBook(enchantName, target, level)) {
                return true;
            }

            sender.sendMessage(Utils.format(cfg.getString("messages.enchantment-list")));
        }

        sender.sendMessage(Utils.format(cfg.getString("messages.usage")));

        return true;
    }

    private boolean giveBook(String enchantName, Player player, int level) {
        if (player == null) {
            return false;
        }

        ItemStack item = Enchanter.getBook(enchantName, level);

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


        if(args.length == 1) {
            if("list".contains(args[0].toLowerCase())) {
                completes.add("list");
            }

            if("apply".contains(args[0].toLowerCase())) {
                completes.add("apply");
            }

            if("give".contains(args[0].toLowerCase())) {
                completes.add("give");
            }
        }

        // /extraenchants apply <enchant> <level>
        if (args.length == 2 && args[0].equalsIgnoreCase("apply")) {
            if(sender.hasPermission("extraenchants.command.apply")) {
                CustomEnchants.getAllEnchants()
                        .stream()
                        .map(s -> s.getKey().getKey().toLowerCase())
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .forEach(completes::add);
            }
        }

        // /extraenchants give <player> <enchant> <level>
        if(args.length == 2 && args[0].equalsIgnoreCase("give")) {
            Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .forEach(completes::add);
        }

        // /extraenchants give <player> <enchant> <level>
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            if(sender.hasPermission("extraenchants.command.give")) {
                CustomEnchants.getAllEnchants()
                        .stream()
                        .map(s -> s.getKey().getKey().toLowerCase())
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .forEach(completes::add);
            }
        }

        if(args.length == 4) {
            completes.add("1");
            completes.add("2");
        }

        return completes;
    }

    private boolean isArgument(int index, String[] args, String argument) {
        if(args.length < index) {
            return false;
        }

        return args[index].equalsIgnoreCase(argument);
    }
}