package me.m0dii.extraenchants.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.utils.Enchanter;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class UnenchantCommand {
    private static final FileConfiguration cfg = ExtraEnchants.getInstance().getCfg();

    public static LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("unenchant")
                .executes(UnenchantCommand::runUnenchantLogic)
                .build();
    }

    private static int runUnenchantLogic(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Console cannot perform this command");
            return Command.SINGLE_SUCCESS;
        }

        if (!player.hasPermission("extraenchants.command.unenchant")) {
            sender.sendMessage(Utils.format(cfg.getString("messages.no-permission")));

            return Command.SINGLE_SUCCESS;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();

        if (hand.getType().isAir()) {
            sender.sendMessage(ChatColor.RED + "Must be holding an item to do this.");

            return Command.SINGLE_SUCCESS;
        }

        if (hand.getItemMeta() == null) {
            sender.sendMessage(ChatColor.RED + "Must be holding an enchanted item to do this.");

            return Command.SINGLE_SUCCESS;
        }

        CustomEnchants.getAllEEnchants().forEach((enchant) -> {
            if (InventoryUtils.hasEnchant(hand, enchant)) {
                removeEnchant(sender, hand, enchant);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    private static void removeEnchant(@Nonnull CommandSender sender,
                                      ItemStack hand,
                                      EEnchant enchant) {
        Enchanter.removeEnchant(hand, enchant);

        String removed = cfg.getString("messages.enchant-removed");

        if (removed == null) {
            return;
        }

        sender.sendMessage(Utils.format(removed.replace("%enchant_name%", enchant.getDisplayName())));
    }
}