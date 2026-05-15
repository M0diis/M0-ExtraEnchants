package me.m0dii.extraenchants.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;
import me.m0dii.extraenchants.utils.EnchantListGUI;
import me.m0dii.extraenchants.utils.Enchanter;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Locale;

@SuppressWarnings("UnstableApiUsage")
public class ExtraEnchantCommand {
    private static String msg(String path) {
        return Utils.format(ExtraEnchants.getInstance().getCfg().getString(path));
    }

    public static LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("extraenchant")
                .then(Commands.literal("list")
                        .executes(ExtraEnchantCommand::runListLogic)
                )
                .then(Commands.literal("apply")
                        .then(Commands.argument("enchant", StringArgumentType.string())
                                .suggests((ctx, builder) -> {
                                    String remaining = builder.getRemainingLowerCase();

                                    Arrays.stream(EEnchant.values())
                                            .map(EEnchant::name)
                                            .filter(name -> name.toLowerCase().startsWith(remaining))
                                            .forEach(builder::suggest);

                                    ExtraEnchants.getInstance().getCustomEnchantFramework().getDefinitions().values().stream()
                                            .map(CustomEnchantDefinition::getId)
                                            .map(id -> id.toUpperCase(Locale.ROOT))
                                            .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(remaining))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                        .executes(ExtraEnchantCommand::runApplyLogic)
                                )
                        )
                )
                .then(Commands.literal("give")
                        .then(Commands.argument("player", StringArgumentType.string())
                                .suggests((ctx, builder) -> {
                                    String remaining = builder.getRemainingLowerCase();

                                    Bukkit.getOnlinePlayers().stream()
                                            .map(Player::getName)
                                            .filter(name -> name.toLowerCase().startsWith(remaining))
                                            .forEach(builder::suggest);

                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("enchant", StringArgumentType.string())
                                        .suggests((ctx, builder) -> {
                                            String remaining = builder.getRemainingLowerCase();

                                            Arrays.stream(EEnchant.values())
                                                    .map(EEnchant::name)
                                                    .filter(name -> name.toLowerCase().startsWith(remaining))
                                                    .forEach(builder::suggest);

                                            ExtraEnchants.getInstance().getCustomEnchantFramework().getDefinitions().values().stream()
                                                    .map(CustomEnchantDefinition::getId)
                                                    .map(id -> id.toUpperCase(Locale.ROOT))
                                                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(remaining))
                                                    .forEach(builder::suggest);

                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(ExtraEnchantCommand::runGiveLogic)
                                        )
                                )
                        )
                )
                .then(Commands.literal("reload").executes(ExtraEnchantCommand::runReloadLogic))
                .then(Commands.literal("debug")
                        .executes(ctx -> runDebugToggleLogic(ctx, null))
                        .then(Commands.argument("state", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("on");
                                    builder.suggest("off");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> runDebugToggleLogic(ctx, StringArgumentType.getString(ctx, "state")))
                        )
                )
                .then(Commands.literal("debugitem").executes(ExtraEnchantCommand::runDebugItemLogic))
                .build();
    }

    private static int runListLogic(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (sender instanceof Player player) {
            if (!player.hasPermission("extraenchants.command.list")) {
                player.sendMessage(msg("messages.no-permission"));
                return Command.SINGLE_SUCCESS;
            }

            EnchantListGUI list = new EnchantListGUI();
            list.open(player);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int runApplyLogic(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        String enchantName = StringArgumentType.getString(ctx, "enchant");
        int level = IntegerArgumentType.getInteger(ctx, "level");

        if (sender instanceof Player player) {
            if (!player.hasPermission("extraenchants.command.apply")) {
                player.sendMessage(msg("messages.no-permission"));
                return Command.SINGLE_SUCCESS;
            }

            EEnchant enchant = EEnchant.parse(enchantName);

            if (enchant == null) {
                CustomEnchantDefinition custom = ExtraEnchants.getInstance().getCustomEnchantFramework().getDefinition(enchantName);
                if (custom == null) {
                    sender.sendMessage(msg("messages.enchantment-list"));
                    return Command.SINGLE_SUCCESS;
                }

                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) {
                    sender.sendMessage(msg("messages.hold-item"));
                    return Command.SINGLE_SUCCESS;
                }

                ExtraEnchants.getInstance().getCustomEnchantFramework().applyEnchant(item, custom.getId(), level);
                sender.sendMessage(msg("messages.enchant-applied"));
                return Command.SINGLE_SUCCESS;
            }

            Enchantment enchantment = enchant.getEnchantment();

            if (enchantment == null) {
                sender.sendMessage(msg("messages.enchantment-list"));
                return Command.SINGLE_SUCCESS;
            }

            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getType() == Material.AIR) {
                sender.sendMessage(msg("messages.hold-item"));
                return Command.SINGLE_SUCCESS;
            }

            Enchanter.applyEnchant(item, enchant, level, false);
            sender.sendMessage(msg("messages.enchant-applied"));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int runGiveLogic(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        String playerName = StringArgumentType.getString(ctx, "player");
        String enchantName = StringArgumentType.getString(ctx, "enchant");
        int level = IntegerArgumentType.getInteger(ctx, "level");

        if (!sender.hasPermission("extraenchants.command.give")) {
            sender.sendMessage(msg("messages.no-permission"));
            return Command.SINGLE_SUCCESS;
        }

        Player target = Bukkit.getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(msg("messages.player-not-found"));
            return Command.SINGLE_SUCCESS;
        }

        if (giveBook(enchantName, target, level)) {
            return Command.SINGLE_SUCCESS;
        }

        sender.sendMessage(msg("messages.enchantment-list"));
        return Command.SINGLE_SUCCESS;
    }

    private static int runReloadLogic(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!sender.hasPermission("extraenchants.command.reload")) {
            sender.sendMessage(msg("messages.no-permission"));
            return Command.SINGLE_SUCCESS;
        }

        ExtraEnchants.getInstance().getConfigManager().reloadConfig();
        ExtraEnchants.getInstance().setDebugMode(ExtraEnchants.getInstance().getCfg().getBoolean("debug", false));
        ExtraEnchants.getInstance().getCustomEnchantFramework().reload();
        sender.sendMessage(msg("messages.reloaded"));

        return Command.SINGLE_SUCCESS;
    }

    private static int runDebugToggleLogic(CommandContext<CommandSourceStack> ctx, String stateArg) {
        CommandSender sender = ctx.getSource().getSender();

        if (!sender.hasPermission("extraenchants.command.debug")) {
            sender.sendMessage(msg("messages.no-permission"));
            return Command.SINGLE_SUCCESS;
        }

        ExtraEnchants plugin = ExtraEnchants.getInstance();

        if (stateArg == null || stateArg.isBlank()) {
            sender.sendMessage(Utils.format("&eDebug mode is currently: &f" + (plugin.isDebugMode() ? "ON" : "OFF")));
            return Command.SINGLE_SUCCESS;
        }

        String state = stateArg.toLowerCase(Locale.ROOT);
        if (!state.equals("on") && !state.equals("off")) {
            sender.sendMessage(Utils.format("&cUsage: /ee debug [on/off]"));
            return Command.SINGLE_SUCCESS;
        }

        boolean enabled = state.equals("on");
        plugin.setDebugMode(enabled);
        sender.sendMessage(Utils.format("&aDebug mode set to: &f" + (enabled ? "ON" : "OFF")));
        return Command.SINGLE_SUCCESS;
    }

    private static int runDebugItemLogic(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!sender.hasPermission("extraenchants.command.debugitem")) {
            sender.sendMessage(msg("messages.no-permission"));
            return Command.SINGLE_SUCCESS;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Utils.format("&cOnly players can use this command!"));
            return Command.SINGLE_SUCCESS;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            sender.sendMessage(Utils.format("&cYou must be holding an item!"));
            return Command.SINGLE_SUCCESS;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        sender.sendMessage(Utils.format("&7&m----------------------------------------"));
        sender.sendMessage(Utils.format("&6&lItem Data"));
        sender.sendMessage(Utils.format("&7&m----------------------------------------"));
        sender.sendMessage(Utils.format("&eItem: &f" + item.getType().name()));
        sender.sendMessage(Utils.format("&eAmount: &f" + item.getAmount()));
        sender.sendMessage(Utils.format("&eDurability: &f" + item.getDurability()));
        sender.sendMessage(Utils.format("&eMax Durability: &f" + item.getType().getMaxDurability()));
        sender.sendMessage(Utils.format("&eDisplay Name: &f" + meta.getDisplayName()));
        sender.sendMessage(Utils.format("&eLore: &f" + item.getItemMeta().getLore()));
        sender.sendMessage(Utils.format("&eEnchantments: &f" + item.getEnchantments()));
        sender.sendMessage(Utils.format("&eItem Flags: &f" + meta.getItemFlags()));
        sender.sendMessage(Utils.format("&eItem Type: &f" + item.getType().name()));
        sender.sendMessage(Utils.format("&eBlockData Compatible: &f" + item.getType().isBlock()));
        sender.sendMessage(Utils.format("&ePDC Keys:"));
        pdc.getKeys().forEach(key -> {
            sender.sendMessage(Utils.format("&e- &f" + key + " : " + readPdcValue(pdc, key)));
        });
        sender.sendMessage(Utils.format("&7&m----------------------------------------"));

        return Command.SINGLE_SUCCESS;
    }

    private static String readPdcValue(PersistentDataContainer pdc, NamespacedKey key) {
        if (pdc.has(key, PersistentDataType.STRING)) {
            return String.valueOf(pdc.get(key, PersistentDataType.STRING));
        }

        if (pdc.has(key, PersistentDataType.INTEGER)) {
            return String.valueOf(pdc.get(key, PersistentDataType.INTEGER));
        }

        if (pdc.has(key, PersistentDataType.LONG)) {
            return String.valueOf(pdc.get(key, PersistentDataType.LONG));
        }

        if (pdc.has(key, PersistentDataType.DOUBLE)) {
            return String.valueOf(pdc.get(key, PersistentDataType.DOUBLE));
        }

        if (pdc.has(key, PersistentDataType.FLOAT)) {
            return String.valueOf(pdc.get(key, PersistentDataType.FLOAT));
        }

        if (pdc.has(key, PersistentDataType.BYTE)) {
            return String.valueOf(pdc.get(key, PersistentDataType.BYTE));
        }

        if (pdc.has(key, PersistentDataType.SHORT)) {
            return String.valueOf(pdc.get(key, PersistentDataType.SHORT));
        }

        if (pdc.has(key, PersistentDataType.INTEGER_ARRAY)) {
            return Arrays.toString(pdc.get(key, PersistentDataType.INTEGER_ARRAY));
        }

        if (pdc.has(key, PersistentDataType.LONG_ARRAY)) {
            return Arrays.toString(pdc.get(key, PersistentDataType.LONG_ARRAY));
        }

        if (pdc.has(key, PersistentDataType.BYTE_ARRAY)) {
            return "byte[" + pdc.get(key, PersistentDataType.BYTE_ARRAY).length + "]";
        }

        return "<complex/unsupported tag>";
    }

    private static boolean giveBook(String enchantName, Player player, int level) {
        if (player == null) {
            return false;
        }

        ItemStack item = Enchanter.getBook(enchantName, level);

        if (item == null) {
            CustomEnchantDefinition custom = ExtraEnchants.getInstance().getCustomEnchantFramework().getDefinition(enchantName);
            if (custom == null) {
                return false;
            }

            item = ExtraEnchants.getInstance().getCustomEnchantFramework().createBook(custom.getId(), level);
            if (item == null) {
                return false;
            }
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        } else {
            player.getInventory().addItem(item);
        }

        return true;
    }
}
