package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.utils.Enchantables;
import me.m0dii.extraenchants.utils.Enchanter;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SignEnchantInteract implements Listener {
    private final ExtraEnchants plugin;

    public SignEnchantInteract(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignPlace(final SignChangeEvent e) {
        if(plugin.getEconomy() == null) {
            return;
        }

        if(!plugin.getCfg().getBoolean("enchant-signs.enabled")) {
            return;
        }

        Player player = e.getPlayer();

        if(!player.hasPermission("extraenchants.signs.create")) {
            player.sendMessage(Utils.format(plugin.getCfg().getString("messages.no-permission")));
            e.getBlock().breakNaturally();
            return;
        }

        Block block = e.getBlock();

        if (!(block.getState() instanceof Sign sign)) {
            return;
        }

        if (!(sign.getBlockData() instanceof WallSign ws)) {
            e.getBlock().breakNaturally();
            return;
        }

        String firstLine = Utils.stripColor(e.line(0));

        if (!firstLine.equalsIgnoreCase("[Enchant]")) {
            return;
        }

        String itemTypeString = Utils.stripColor(e.line(1));

        Enchantables.ItemType itemType = Enchantables.ItemType.parse(itemTypeString);

        if (itemType == null) {
            player.sendMessage(Utils.format(plugin.getCfg().getString("enchant-signs.messages.invalid-item-type")));
            e.getBlock().breakNaturally();
            return;
        }

        String enchantmentString = Utils.stripColor(e.line(2));

        EEnchant enchant = EEnchant.parse(enchantmentString);

        if (enchant == null) {
            player.sendMessage(Utils.format(plugin.getCfg().getString("enchant-signs.messages.invalid-enchant")));
            e.getBlock().breakNaturally();
            return;
        }

        String costString = Utils.stripColor(e.line(3));

        String[] costSplit = costString.split(":");

        if (costSplit.length != 2) {
            player.sendMessage(Utils.format(plugin.getCfg().getString("enchant-signs.messages.invalid-cost-level")));
            e.getBlock().breakNaturally();
            return;
        }

        String cost = costSplit[0].trim()
                .replaceAll("\\D", "");

        if(!StringUtils.isNumeric(cost) || StringUtils.isEmpty(cost)) {
            player.sendMessage(Utils.format(plugin.getCfg().getString("enchant-signs.messages.invalid-cost-level")));
            e.getBlock().breakNaturally();
            return;
        }

        String level = costSplit[1].trim();

        if(!StringUtils.isNumeric(level)) {
            player.sendMessage(Utils.format(plugin.getCfg().getString("enchant-signs.messages.invalid-cost-level")));
            e.getBlock().breakNaturally();
            return;
        }

        player.sendMessage(Utils.format(plugin.getCfg().getString("enchant-signs.messages.sign-created")));
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent e) {
        if(plugin.getEconomy() == null) {
            return;
        }

        if(!plugin.getCfg().getBoolean("enchant-signs.enabled")) {
            return;
        }

        Player player = e.getPlayer();

        if(!player.hasPermission("extraenchants.signs.use")) {
            player.sendMessage(Utils.format(plugin.getCfg().getString("messages.no-permission")));
            return;
        }

        Block block = e.getClickedBlock();

        if(block == null) {
            return;
        }

        if (!(block.getState() instanceof Sign sign)) {
            return;
        }

        if (!(sign.getBlockData() instanceof WallSign ws)) {
            return;
        }

        String firstLine = Utils.stripColor(sign.line(0));

        if (!firstLine.equalsIgnoreCase(plugin.getCfg().getString("enchant-signs.first-line-format"))) {
            return;
        }

        String itemTypeString = Utils.stripColor(sign.line(1));

        Enchantables.ItemType itemType = Enchantables.ItemType.parse(itemTypeString);


        if (itemType == null) {
            return;
        }

        String enchantmentString = Utils.stripColor(sign.line(2));

        EEnchant enchant = EEnchant.parse(enchantmentString);

        if (enchant == null) {
            player.sendMessage(Utils.format(plugin.getCfg().getString("enchant-signs.messages.invalid-enchant")));
            return;
        }

        String costString = Utils.stripColor(sign.line(3));

        String[] costSplit = costString.split(":");

        if (costSplit.length != 2) {
            return;
        }

        String cost = costSplit[0].trim()
                .replaceAll("\\D", "");

        if(!StringUtils.isNumeric(cost) || StringUtils.isEmpty(cost)) {
            return;
        }

        String level = costSplit[1].trim();

        if(!StringUtils.isNumeric(level)) {
            return;
        }

        int costInt = Integer.parseInt(cost);

        int levelInt = Integer.parseInt(level);

        Economy econ = plugin.getEconomy();

        if(!econ.has(player, costInt)) {
            player.sendMessage(Utils.format(plugin.getCfg().getString("enchant-signs.messages.not-enough-money")));
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();

        if(hand.getType().isAir()) {
            player.sendMessage(Utils.format(plugin.getCfg().getString("enchant-signs.messages.no-item-in-hand")));
            return;
        }

        if(!Enchantables.canEnchantItemCustom(hand, itemType)) {
            player.sendMessage(Utils.format(plugin.getCfg().getString("enchant-signs.messages.cannot-enchant-item")));
            return;
        }

        if(InventoryUtils.hasEnchant(hand, enchant)) {
            player.sendMessage(Utils.format(plugin.getCfg().getString("enchant-signs.messages.already-enchanted")));
            return;
        }

        if(econ.withdrawPlayer(player, costInt).transactionSuccess()) {
            Enchanter.applyEnchant(hand, enchant, levelInt, false);
        }
    }
}