package me.m0dii.extraenchants.listeners.custom;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.PlowEvent;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class OnPlow implements Listener {
    private static final List<Material> SEEDS = Arrays.asList(
            Material.CARROT,
            Material.POTATO,
            Material.WHEAT_SEEDS,
            Material.PUMPKIN_SEEDS,
            Material.BEETROOT_SEEDS,
            Material.MELON_SEEDS
    );

    private final ExtraEnchants plugin;

    public OnPlow(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCustomTill(final PlowEvent event) {
        if (!Utils.shouldTrigger(EEnchant.PLOW)) {
            return;
        }

        final Player p = event.getPlayer();
        final Block block1 = event.getInteractEvent().getClickedBlock();

        Material type = null;

        ItemStack hand = p.getInventory().getItemInMainHand();

        InventoryUtils.applyDurability(hand);

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

    private void setSeeds(Player p,
                          Block block1, Block plant1,
                          Block plant2, Block block2,
                          Block block3, Block plant3,
                          Block block4, Block plant4,
                          Block block5, Block plant5,
                          int amount, int slot,
                          ItemStack fee, Material seed) {
        if (Utils.allowed(p, plant1.getLocation())) {
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

        if (Utils.allowed(p, plant.getLocation())) {
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