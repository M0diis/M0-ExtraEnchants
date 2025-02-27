package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.PlowEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
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
        if (!Utils.shouldTrigger(EEnchant.PLOW)) {
            return;
        }

        final Player p = event.getPlayer();
        final Block block1 = event.getInteractEvent().getClickedBlock();

        Material type = null;

        ItemStack hand = p.getInventory().getItemInMainHand();

        InventoryUtils.applyDurability(p, hand);

        if (block1 == null) {
            return;
        }

        if (!block1.getType().equals(Material.DIRT)
                && !block1.getType().equals(Material.GRASS_BLOCK)) {
            return;
        }

        Location clickedLocation = block1.getLocation();

        Block plant1 = clickedLocation.add(0.0, 1.0, 0.0).getBlock();

        Block plant2 = null;
        Block plant3 = null;

        Block block2 = null;
        Block block3 = null;

        Block plant4 = null;
        Block block4 = null;

        Block plant5 = null;
        Block block5 = null;

        if (p.getFacing() == BlockFace.NORTH) {
            plant2 = clickedLocation.clone().add(-1.0, 0.0, 0.0).getBlock();
            block2 = clickedLocation.clone().add(-1.0, -1.0, 0.0).getBlock();

            plant3 = clickedLocation.clone().add(-2.0, 0.0, 0.0).getBlock();
            block3 = clickedLocation.clone().add(-2.0, -1.0, 0.0).getBlock();

            if (event.getEnchantLevel() == 2) {
                plant4 = clickedLocation.clone().add(1.0, 0.0, 0.0).getBlock();
                block4 = clickedLocation.clone().add(1.0, -1.0, 0.0).getBlock();

                plant5 = clickedLocation.clone().add(2.0, 0.0, 0.0).getBlock();
                block5 = clickedLocation.clone().add(2.0, -1.0, 0.0).getBlock();
            }
        } else if (p.getFacing() == BlockFace.SOUTH) {
            plant2 = clickedLocation.clone().add(1.0, 0.0, 0.0).getBlock();
            block2 = clickedLocation.clone().add(1.0, -1.0, 0.0).getBlock();

            plant3 = clickedLocation.clone().add(2.0, 0.0, 0.0).getBlock();
            block3 = clickedLocation.clone().add(2.0, -1.0, 0.0).getBlock();

            if (event.getEnchantLevel() == 2) {
                plant4 = clickedLocation.clone().add(-1.0, 0.0, 0.0).getBlock();
                block4 = clickedLocation.clone().add(-1.0, -1.0, 0.0).getBlock();

                plant5 = clickedLocation.clone().add(-2.0, 0.0, 0.0).getBlock();
                block5 = clickedLocation.clone().add(-2.0, -1.0, 0.0).getBlock();
            }
        } else if (p.getFacing() == BlockFace.EAST) {
            plant2 = clickedLocation.clone().add(0.0, 0.0, -1.0).getBlock();
            block2 = clickedLocation.clone().add(0.0, -1.0, -1.0).getBlock();

            plant3 = clickedLocation.clone().add(0.0, 0.0, -2.0).getBlock();
            block3 = clickedLocation.clone().add(0.0, -1.0, -2.0).getBlock();

            if (event.getEnchantLevel() == 2) {
                plant4 = clickedLocation.clone().add(0.0, 0.0, 1.0).getBlock();
                block4 = clickedLocation.clone().add(0.0, -1.0, 1.0).getBlock();

                plant5 = clickedLocation.clone().add(0.0, 0.0, 2.0).getBlock();
                block5 = clickedLocation.clone().add(0.0, -1.0, 2.0).getBlock();
            }
        } else {
            plant2 = clickedLocation.clone().add(0.0, 0.0, 1.0).getBlock();
            block2 = clickedLocation.clone().add(0.0, -1.0, 1.0).getBlock();

            plant3 = clickedLocation.clone().add(0.0, 0.0, 2.0).getBlock();
            block3 = clickedLocation.clone().add(0.0, -1.0, 2.0).getBlock();

            if (event.getEnchantLevel() == 2) {
                plant4 = clickedLocation.clone().add(0.0, 0.0, -1.0).getBlock();
                block4 = clickedLocation.clone().add(0.0, -1.0, -1.0).getBlock();

                plant5 = clickedLocation.clone().add(0.0, 0.0, -2.0).getBlock();
                block5 = clickedLocation.clone().add(0.0, -1.0, -2.0).getBlock();
            }
        }

        if (!plant1.getType().equals(Material.AIR)) {
            return;
        }

        int num = 0;
        int amount = 0;

        ItemStack[] contents = p.getInventory().getStorageContents();

        ItemStack fee = null;
        int slot = -1;

        for (int j = 0; j < contents.length; ++j) {
            final ItemStack i = contents[j];

            if (i != null && SEEDS.contains(i.getType())) {
                type = i.getType();
                amount = i.getAmount();

                fee = i;
                slot = j;

                break;
            }

            if (++num > 8)
                return;
        }

        if (fee == null || type == null || slot == -1) {
            return;
        }

        switch (type) {
            case BEETROOT_SEEDS ->
                    setSeeds(p, block1, plant1, plant2, block2, block3, plant3, block4, plant4, block5, plant5, amount, slot, fee, Material.BEETROOTS);
            case WHEAT_SEEDS ->
                    setSeeds(p, block1, plant1, plant2, block2, block3, plant3, block4, plant4, block5, plant5, amount, slot, fee, Material.WHEAT);
            case PUMPKIN_SEEDS ->
                    setSeeds(p, block1, plant1, plant2, block2, block3, plant3, block4, plant4, block5, plant5, amount, slot, fee, Material.PUMPKIN_STEM);
            case MELON_SEEDS ->
                    setSeeds(p, block1, plant1, plant2, block2, block3, plant3, block4, plant4, block5, plant5, amount, slot, fee, Material.MELON_STEM);
            case CARROT ->
                    setSeeds(p, block1, plant1, plant2, block2, block3, plant3, block4, plant4, block5, plant5, amount, slot, fee, Material.CARROTS);
            case POTATO ->
                    setSeeds(p, block1, plant1, plant2, block2, block3, plant3, block4, plant4, block5, plant5, amount, slot, fee, Material.POTATOES);
            default -> {
            }
        }
    }


    @EventHandler
    public void onPlayerInteractPlow(final PlayerInteractEvent e) {
        if (EEnchant.PLOW.isDisabled()) {
            return;
        }

        if (e.isCancelled()) {
            return;
        }

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        Player p = e.getPlayer();

        if (hand == null) {
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

        if ((p.getGameMode() == GameMode.CREATIVE)
                || (p.getGameMode() == GameMode.SPECTATOR)) {
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        int level = InventoryUtils.getEnchantLevel(hand, EEnchant.PLOW);

        Bukkit.getPluginManager().callEvent(new PlowEvent(p, e, level));
    }



    private void setSeeds(Player p,
                          Block block1, Block plant1,
                          Block plant2, Block block2,
                          Block block3, Block plant3,
                          Block block4, Block plant4,
                          Block block5, Block plant5,
                          int amount, int slot,
                          ItemStack fee, Material seed) {
        if (Utils.allowedAt(p, plant1.getLocation())) {
            block1.setType(Material.FARMLAND);
            plant1.setType(seed);

            --amount;

            fee.setAmount(amount);
            p.getInventory().setItem(slot, fee);
        }

        setSeed(p, plant2, block2, amount, slot, fee, seed);
        setSeed(p, plant3, block3, amount, slot, fee, seed);
        setSeed(p, plant4, block4, amount, slot, fee, seed);
        setSeed(p, plant5, block5, amount, slot, fee, seed);
    }

    private void setSeed(Player p, Block plant, Block block, int amount, int slot, ItemStack fee, Material seed) {
        if (plant == null || block == null) {
            return;
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
    }
}
