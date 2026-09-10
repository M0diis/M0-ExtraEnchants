package me.m0dii.extraenchants.listeners;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.framework.model.CustomEnchantDefinition;
import me.m0dii.extraenchants.framework.runtime.CustomEnchantFramework;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Enchanter;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Locale;
import java.util.Map;

/** Handles creation and use of ENCHANT, DISENCHANT and RETRIEVE signs. */
public final class SignEnchantInteract implements Listener {
    private final ExtraEnchants plugin;

    public SignEnchantInteract(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent event) {
        if (!plugin.getCfg().getBoolean("enchant-signs.enabled")) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (!(block.getState() instanceof Sign sign) || !(sign.getBlockData() instanceof WallSign)) {
            return;
        }

        String firstLine = Utils.stripColor(event.line(0));
        if (!isOperation(firstLine)) {
            return;
        }

        if (!player.hasPermission("extraenchants.signs.create")) {
            player.sendMessage(message("messages.no-permission"));
            block.breakNaturally();
            return;
        }

        SignSpec spec = parseSpec(
                Utils.stripColor(event.line(1)),
                Utils.stripColor(event.line(2)),
                Utils.stripColor(event.line(3)));
        if (spec == null) {
            player.sendMessage(message("enchant-signs.messages.invalid-cost-level"));
            block.breakNaturally();
            return;
        }

        player.sendMessage(message("enchant-signs.messages.sign-created"));
        sign.setWaxed(true);
        sign.update(true, false);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.getCfg().getBoolean("enchant-signs.enabled")) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !(block.getState() instanceof Sign sign)
                || !(sign.getBlockData() instanceof WallSign)) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("extraenchants.signs.use")) {
            player.sendMessage(message("messages.no-permission"));
            return;
        }

        String firstLine = Utils.stripColor(sign.getSide(Side.FRONT).line(0));
        SignSpec spec = parseSpec(
                Utils.stripColor(sign.getSide(Side.FRONT).line(1)),
                Utils.stripColor(sign.getSide(Side.FRONT).line(2)),
                Utils.stripColor(sign.getSide(Side.FRONT).line(3)));
        if (spec == null) {
            player.sendMessage(message("enchant-signs.messages.invalid-cost-level"));
            return;
        }

        if (firstLine.equalsIgnoreCase(config("enchant-signs.enchant-first-line-format"))) {
            handleEnchantLogic(spec, player);
        } else if (firstLine.equalsIgnoreCase(config("enchant-signs.disenchant-first-line-format"))) {
            handleDisenchantLogic(spec, player);
        } else if (firstLine.equalsIgnoreCase(config("enchant-signs.retrieve-first-line-format"))) {
            handleRetrieveLogic(spec, player);
        }
    }

    private void handleRetrieveLogic(SignSpec spec, Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) {
            player.sendMessage(message("enchant-signs.messages.no-item-in-hand"));
            return;
        }
        if (!EnchantableItemTypeUtil.canEnchantItemCustom(hand, spec.itemType())) {
            player.sendMessage(message("enchant-signs.messages.cannot-enchant-item"));
            return;
        }

        CustomEnchantFramework framework = plugin.getCustomEnchantFramework();
        int currentLevel = currentLevel(hand, spec);
        if (currentLevel <= 0) {
            player.sendMessage(message("enchant-signs.messages.item-not-enchanted"));
            return;
        }

        ItemStack book = createBook(spec, currentLevel, framework);
        if (book == null) {
            player.sendMessage(message("enchant-signs.messages.invalid-enchant"));
            return;
        }
        if (player.getInventory().firstEmpty() < 0) {
            player.sendMessage(Utils.format("&cYour inventory is full; retrieve cancelled."));
            return;
        }
        if (!charge(spec, player)) {
            return;
        }

        ItemStack original = hand.clone();
        if (!removeEnchant(hand, spec, framework)) {
            refund(spec, player);
            player.sendMessage(message("enchant-signs.messages.item-not-enchanted"));
            return;
        }

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(book);
        if (!leftovers.isEmpty()) {
            player.getInventory().setItemInMainHand(original);
            refund(spec, player);
            player.sendMessage(Utils.format("&cYour inventory is full; retrieve cancelled."));
            return;
        }

        String retrievedText = config("enchant-signs.messages.enchant-retrieved")
                .replace("%enchant_name%", spec.displayName());
        player.sendMessage(Utils.format(retrievedText));
    }

    private void handleEnchantLogic(SignSpec spec, Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) {
            player.sendMessage(message("enchant-signs.messages.no-item-in-hand"));
            return;
        }
        if (!EnchantableItemTypeUtil.canEnchantItemCustom(hand, spec.itemType())
                || (spec.custom() != null && !plugin.getCustomEnchantFramework().isApplicable(hand, spec.custom().getId()))) {
            player.sendMessage(message("enchant-signs.messages.cannot-enchant-item"));
            return;
        }
        if (currentLevel(hand, spec) > 0) {
            player.sendMessage(message("enchant-signs.messages.already-enchanted"));
            return;
        }
        if (!charge(spec, player)) {
            return;
        }

        if (spec.custom() != null) {
            plugin.getCustomEnchantFramework().applyEnchant(hand, spec.custom().getId(), spec.level());
        } else if (spec.wrapper() != null) {
            Enchanter.applyEnchant(hand, spec.wrapper(), spec.level(), false);
        } else {
            Enchanter.addUnsafe(hand, spec.vanilla(), spec.level());
        }
        player.sendMessage(message("enchant-signs.messages.enchant-applied"));
    }

    private void handleDisenchantLogic(SignSpec spec, Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) {
            player.sendMessage(message("enchant-signs.messages.no-item-in-hand"));
            return;
        }
        if (currentLevel(hand, spec) <= 0) {
            player.sendMessage(message("enchant-signs.messages.item-not-enchanted"));
            return;
        }
        if (!charge(spec, player)) {
            return;
        }
        removeEnchant(hand, spec, plugin.getCustomEnchantFramework());

        String removedText = config("enchant-signs.messages.enchant-removed")
                .replace("%enchant_name%", spec.displayName());
        player.sendMessage(Utils.format(removedText));
    }

    private int currentLevel(ItemStack item, SignSpec spec) {
        if (spec.custom() != null) {
            return plugin.getCustomEnchantFramework().getConfigEnchantLevel(item, spec.custom().getId());
        }
        if (spec.wrapper() != null) {
            return InventoryUtils.getEnchantLevel(item, spec.wrapper());
        }
        return InventoryUtils.getEnchantLevel(item, spec.vanilla());
    }

    private boolean removeEnchant(ItemStack item, SignSpec spec, CustomEnchantFramework framework) {
        if (spec.custom() != null) {
            return framework.removeConfigEnchant(item, spec.custom().getId());
        }
        if (spec.wrapper() != null) {
            Enchanter.removeEnchant(item, spec.wrapper());
        } else {
            Enchanter.removeEnchant(item, spec.vanilla());
        }
        return true;
    }

    private ItemStack createBook(SignSpec spec, int level, CustomEnchantFramework framework) {
        if (spec.custom() != null) {
            return framework.createBook(spec.custom().getId(), level);
        }
        if (spec.wrapper() != null) {
            return Enchanter.getBook(spec.wrapper(), level);
        }

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        if (!(book.getItemMeta() instanceof EnchantmentStorageMeta meta)) {
            return null;
        }
        meta.addStoredEnchant(spec.vanilla(), level, true);
        book.setItemMeta(meta);
        return book;
    }

    private boolean charge(SignSpec spec, Player player) {
        return switch (spec.currency()) {
            case XP -> {
                if (player.getTotalExperience() < spec.cost()) {
                    player.sendMessage(message("enchant-signs.messages.not-enough-xp"));
                    yield false;
                }
                player.giveExp(-spec.cost());
                yield true;
            }
            case LEVEL -> {
                if (player.getLevel() < spec.cost()) {
                    player.sendMessage(message("enchant-signs.messages.not-enough-levels"));
                    yield false;
                }
                player.giveExpLevels(-spec.cost());
                yield true;
            }
            case MONEY -> {
                if (plugin.getEconomy() == null || !plugin.getEconomy().has(player, spec.cost())) {
                    player.sendMessage(message("enchant-signs.messages.not-enough-money"));
                    yield false;
                }
                if (!plugin.getEconomy().withdrawPlayer(player, spec.cost()).transactionSuccess()) {
                    player.sendMessage(message("enchant-signs.messages.not-enough-money"));
                    yield false;
                }
                yield true;
            }
        };
    }

    private void refund(SignSpec spec, Player player) {
        switch (spec.currency()) {
            case XP -> player.giveExp(spec.cost());
            case LEVEL -> player.giveExpLevels(spec.cost());
            case MONEY -> {
                if (plugin.getEconomy() != null) {
                    plugin.getEconomy().depositPlayer(player, spec.cost());
                }
            }
        }
    }

    private SignSpec parseSpec(String itemTypeRaw, String enchantRaw, String costRaw) {
        EnchantableItemTypeUtil.ItemType itemType = EnchantableItemTypeUtil.ItemType.parse(itemTypeRaw);
        if (itemType == null || enchantRaw == null || enchantRaw.isBlank()) {
            return null;
        }

        EEnchant wrapper = EEnchant.parse(enchantRaw);
        Enchantment vanilla = null;
        CustomEnchantDefinition custom = null;
        if (wrapper == null) {
            custom = plugin.getCustomEnchantFramework().getDefinition(enchantRaw);
            if (custom == null) {
                NamespacedKey key = NamespacedKey.fromString(enchantRaw.toLowerCase(Locale.ROOT));
                if (key == null) {
                    key = NamespacedKey.minecraft(enchantRaw.toLowerCase(Locale.ROOT));
                }
                vanilla = key == null ? null : Enchantment.getByKey(key);
            }
        }
        if (wrapper == null && custom == null && vanilla == null) {
            return null;
        }

        String[] split = costRaw == null ? new String[0] : costRaw.split(":", 2);
        if (split.length != 2) {
            return null;
        }
        Integer cost = parseDigits(split[0]);
        Integer level = parseDigits(split[1]);
        if (cost == null || level == null || cost < 0 || level < 1) {
            return null;
        }

        String currencyRaw = split[0].trim().toUpperCase(Locale.ROOT);
        Currency currency = currencyRaw.endsWith("XP") ? Currency.XP
                : (currencyRaw.endsWith("L") || currencyRaw.endsWith("LVL") ? Currency.LEVEL : Currency.MONEY);
        return new SignSpec(itemType, wrapper, vanilla, custom, cost, level, currency);
    }

    private Integer parseDigits(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean isOperation(String firstLine) {
        return firstLine.equalsIgnoreCase(config("enchant-signs.enchant-first-line-format"))
                || firstLine.equalsIgnoreCase(config("enchant-signs.disenchant-first-line-format"))
                || firstLine.equalsIgnoreCase(config("enchant-signs.retrieve-first-line-format"));
    }

    private String config(String path) {
        String value = plugin.getCfg().getString(path);
        return value == null ? "" : value;
    }

    private String message(String path) {
        return Utils.format(config(path));
    }

    private record SignSpec(
            EnchantableItemTypeUtil.ItemType itemType,
            EEnchant wrapper,
            Enchantment vanilla,
            CustomEnchantDefinition custom,
            int cost,
            int level,
            Currency currency
    ) {
        private String displayName() {
            if (custom != null) {
                return custom.getDisplayName();
            }
            if (wrapper != null) {
                return wrapper.getDisplayName();
            }
            return vanilla.getKey().getKey();
        }
    }

    private enum Currency {
        MONEY, XP, LEVEL
    }
}
