package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.events.PlowEvent;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@EnchantWrapper(name = "Plow", maxLevel = 2)
public class PlowWrapper extends CustomEnchantment {

    public PlowWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isHoe(item) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        if (enchant.getCustomConflicts().contains(enchantment)) {
            return true;
        }

        if (!enchant.defaultConflictsEnabled()) {
            return false;
        }

        return enchantment.equals(Enchantment.SILK_TOUCH) || EEnchant.PLOW.equals(enchantment);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    private static final List<Material> SEEDS = Arrays.asList(
            Material.CARROT,
            Material.POTATO,
            Material.WHEAT_SEEDS,
            Material.PUMPKIN_SEEDS,
            Material.BEETROOT_SEEDS,
            Material.MELON_SEEDS
    );

    @EventHandler
    public void onCustomTill(final PlowEvent event) {
        if (!Utils.shouldTrigger(EEnchant.PLOW)) return;

        final Player p = event.getPlayer();
        final Block clickedBlock = event.getPlayerInteractEvent().getClickedBlock();

        if (clickedBlock == null) return;

        if (!clickedBlock.getType().equals(Material.DIRT) && !clickedBlock.getType().equals(Material.GRASS_BLOCK))
            return;

        Location loc = clickedBlock.getLocation();
        int level = event.getEnchantLevel();

        BlockFace face = p.getFacing();
        int[][] offsets = switch (face) {
            case NORTH -> new int[][]{{0, 1, 0}, {-1, 1, 0}, {-2, 1, 0}, {1, 1, 0}, {2, 1, 0}};
            case SOUTH -> new int[][]{{0, 1, 0}, {1, 1, 0}, {2, 1, 0}, {-1, 1, 0}, {-2, 1, 0}};
            case EAST -> new int[][]{{0, 1, 0}, {0, 1, -1}, {0, 1, -2}, {0, 1, 1}, {0, 1, 2}};
            default -> new int[][]{{0, 1, 0}, {0, 1, 1}, {0, 1, 2}, {0, 1, -1}, {0, 1, -2}};
        };

        int count = (level == 2) ? 5 : 3;
        Block[] plants = new Block[count];
        Block[] blocks = new Block[count];
        for (int i = 0; i < count; i++) {
            plants[i] = loc.clone().add(offsets[i][0], offsets[i][1], offsets[i][2]).getBlock();
            blocks[i] = loc.clone().add(offsets[i][0], offsets[i][1] - 1, offsets[i][2]).getBlock();
        }

        ItemStack[] contents = p.getInventory().getStorageContents();
        ItemStack fee = null;
        int slot = -1;
        Material type = null;
        int amount = 0;
        for (int j = 0; j < contents.length && j < 9; ++j) {
            ItemStack i = contents[j];
            if (i != null && SEEDS.contains(i.getType())) {
                type = i.getType();
                amount = i.getAmount();
                fee = i;
                slot = j;
                break;
            }
        }
        if (fee == null || type == null || slot == -1) return;

        Material plantType = switch (type) {
            case BEETROOT_SEEDS -> Material.BEETROOTS;
            case WHEAT_SEEDS -> Material.WHEAT;
            case PUMPKIN_SEEDS -> Material.PUMPKIN_STEM;
            case MELON_SEEDS -> Material.MELON_STEM;
            case CARROT -> Material.CARROTS;
            case POTATO -> Material.POTATOES;
            default -> null;
        };

        if (plantType == null) return;

        for (int i = 0; i < count; i++) {
            amount = plantSeed(p, blocks[i], plants[i], amount, slot, fee, plantType);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractPlow(final PlayerInteractEvent e) {
        if (EEnchant.PLOW.isDisabled()) {
            return;
        }
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        Player p = e.getPlayer();

        if (hand.getType().isAir()) {
            return;
        }

        if (hand.getItemMeta() == null) {
            return;
        }

        if (!hand.getItemMeta().hasEnchant(EEnchant.PLOW.getEnchantment())) {
            return;
        }

        if (!EnchantableItemTypeUtil.isHoe(hand)) {
            return;
        }

        if ((p.getGameMode() == GameMode.CREATIVE) || (p.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        int level = InventoryUtils.getEnchantLevel(hand, EEnchant.PLOW);

        Bukkit.getPluginManager().callEvent(new PlowEvent(p, e, level));
    }

    private int plantSeed(Player p, Block block, Block plant, int amount, int slot, ItemStack fee, Material seed) {
        if (plant == null || block == null) {
            return amount;
        }
        if (Utils.allowedAt(p, plant.getLocation())) {
            if (plant.getType().equals(Material.AIR) && amount >= 1 &&
                    (block.getType().equals(Material.DIRT) ||
                            block.getType().equals(Material.GRASS_BLOCK))) {
                block.setType(Material.FARMLAND);
                plant.setType(seed);

                --amount;
                fee.setAmount(amount);
                p.getInventory().setItem(slot, fee);
                p.updateInventory();
            }
        }
        return amount;
    }
}
