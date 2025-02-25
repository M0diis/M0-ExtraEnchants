package me.m0dii.extraenchants.utils;

import me.m0dii.extraenchants.enchants.EEnchant;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class BlockProcessor {
    public static void process(Player player, ItemStack hand, Block block) {
        boolean hasSilk = hand.getItemMeta().getEnchants().containsKey(Enchantment.SILK_TOUCH);
        boolean shouldTelepath = InventoryUtils.hasEnchant(hand, EEnchant.TELEPATHY);
        boolean shouldSmelt = InventoryUtils.hasEnchant(hand, EEnchant.SMELT) && !dontSmelt(block.getType()) && !hasSilk;
        boolean hasFortune = hand.getItemMeta().getEnchants().containsKey(Enchantment.LOOT_BONUS_BLOCKS);

        Inventory inv = player.getInventory();

        int fortuneLevel = 1;

        if (hasFortune) {
            fortuneLevel = hand.getItemMeta().getEnchants().get(Enchantment.LOOT_BONUS_BLOCKS);
        }

        List<ItemStack> drops = new ArrayList<>();

        if (shouldSmelt) {
            drops = smelt(block, hasFortune, fortuneLevel);

            if(drops.isEmpty()) {
                drops.addAll(block.getDrops(hand));
            }
        }

        if(shouldTelepath) {
            telepathy(player, block, hasSilk, inv, drops);
        } else {
            for (ItemStack drop : drops) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }
    }

    private static void telepathy(Player player,
                                  Block block,
                                  boolean hasSilk,
                                  Inventory inv,
                                  List<ItemStack> drops) {
        if (!doesFit(player.getInventory(), drops)) {
            for (ItemStack drop : drops) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
            return;
        }

        if (hasSilk) {
            ItemStack itm = new ItemStack(block.getType());

            String name = itm.getType().name();

            if (name.contains("WALL_") || name.contains("BANNER")) {
                if (name.contains("BANNER")) {
                    for (ItemStack i : drops) {
                        inv.addItem(i);
                    }
                } else {
                    Material m = Material.getMaterial(name.replace("WALL_", ""));

                    if (m != null)
                        inv.addItem(new ItemStack(m));
                }
            } else {
                inv.addItem(itm);
            }
        } else if (inv.firstEmpty() == -1) {
            ItemStack item = drops.iterator().next();

            if (inv.contains(item)) {
                addToStack(player, drops);
            }
        } else {
            for (ItemStack i : drops) {
                inv.addItem(i);
            }
        }
    }

    private static List<ItemStack> smelt(Block minedBlock, boolean hasFortune, int fortuneLevel) {
        List<ItemStack> results = new ArrayList<>();

        Iterator<Recipe> recipes = Bukkit.recipeIterator();

        while (recipes.hasNext() && !dontSmelt(minedBlock.getType())) {
            Recipe recipe = recipes.next();

            if (!(recipe instanceof FurnaceRecipe furnaceRecipe)) {
                continue;
            }

            if (furnaceRecipe.getInput().getType() != minedBlock.getType()) {
                continue;
            }

            if (hasFortune && doDouble(minedBlock.getType().name())) {
                int extraDrops = RandomUtils.nextInt(fortuneLevel + 1);

                for (int i = 0; i <= extraDrops; i++) {
                    results.add(furnaceRecipe.getResult());
                }
            } else results.add(furnaceRecipe.getResult());

            break;
        }

        return results;
    }

    private static boolean doDouble(String name) {
        return Stream.of("IRON_ORE", "GOLD_ORE", "COAL_ORE")
                .anyMatch(name::equalsIgnoreCase);
    }

    private static boolean dontSmelt(@NotNull Material material) {
        return EnumSet.of(Material.REDSTONE_ORE,
                        Material.DEEPSLATE_REDSTONE_ORE,
                        Material.DIAMOND_ORE,
                        Material.DEEPSLATE_DIAMOND_ORE,
                        Material.NETHER_QUARTZ_ORE,
                        Material.LAPIS_ORE,
                        Material.DEEPSLATE_LAPIS_ORE,
                        Material.NETHER_GOLD_ORE,
                        Material.DEEPSLATE_EMERALD_ORE,
                        Material.EMERALD_ORE)
                .contains(material);
    }

    private static void addToStack(Player p, Collection<ItemStack> drops) {
        ItemStack item = drops.iterator().next();
        ItemStack[] arrayOfItemStack;

        int j = (arrayOfItemStack = p.getInventory().getContents()).length;

        for (int i = 0; i < j; i++) {
            ItemStack it = arrayOfItemStack[i];

            if ((it.equals(item)) && (it.getAmount() < 64)) {
                for (ItemStack itm : drops) {
                    p.getInventory().addItem(itm);
                }

                break;
            }
        }
    }

    private static boolean doesFit(Inventory inv, Collection<ItemStack> drops) {
        return Arrays.stream(inv.getStorageContents()).anyMatch(Objects::isNull) || hasSpaceForItem(drops, inv);
    }

    private static boolean hasSpaceForItem(Collection<ItemStack> drops, Inventory inv) {
        Iterator<ItemStack> iter = drops.iterator();

        if (iter != null && iter.hasNext()) {
            ItemStack item = iter.next();

            for (ItemStack it : inv.getStorageContents()) {
                if (it != null && it.equals(item) && (it.getAmount() < 64))
                    return true;
            }
        }

        return false;
    }
}
