package me.M0dii.ExtraEnchants.Listeners.Custom;

import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import me.M0dii.ExtraEnchants.Events.SmeltEvent;
import me.M0dii.ExtraEnchants.Events.TelepathyEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.*;
import java.util.stream.Stream;

public class OnSmelt implements Listener {
    private static final Random rnd = new Random();

    @EventHandler
    public void onSmelt(SmeltEvent e) {
        Block b = e.getBlock();
        Player p = e.getPlayer();
        ItemStack tool = p.getInventory().getItemInMainHand();

        Collection<ItemStack> drops = e.getDrops();
        List<ItemStack> results = new ArrayList<>();

        Iterator<Recipe> recipes = Bukkit.recipeIterator();

        boolean hasFortune = tool.getItemMeta().getEnchants()
                .containsKey(Enchantment.LOOT_BONUS_BLOCKS);

        int fortuneLevel = 1;

        if (hasFortune)
            fortuneLevel = tool.getItemMeta().getEnchants()
                    .get(Enchantment.LOOT_BONUS_BLOCKS);

        while (recipes.hasNext() && !dontSmelt(b.getType().name())) {
            Recipe recipe = recipes.next();

            if (!(recipe instanceof FurnaceRecipe furnaceRecipe))
                continue;

            if (furnaceRecipe.getInput().getType() != b.getType())
                continue;

            if (hasFortune) {
                if (doDouble(b.getType().name())) {
                    int extraDrops = rnd.nextInt(fortuneLevel + 1);

                    for (int i = 0; i <= extraDrops; i++)
                        results.add(furnaceRecipe.getResult());
                } else results.add(furnaceRecipe.getResult());
            } else results.add(furnaceRecipe.getResult());

            break;
        }

        if (results.size() == 0) {
            results = new ArrayList<>(drops);
        }

        boolean silk = tool.getItemMeta().getEnchants()
                .containsKey(Enchantment.SILK_TOUCH);

        if (silk) {
            results = new ArrayList<>(drops);
        }

        if (tool != null && tool.getItemMeta() != null &&
                tool.getItemMeta().hasEnchant(CustomEnchants.TELEPATHY)) {
            Bukkit.getPluginManager().callEvent(new TelepathyEvent(p, e.getBlockBreakEvent(), results));
        } else {
            for (ItemStack drop : results) {
                b.getWorld().dropItemNaturally(b.getLocation(), drop);
            }
        }
    }

    private boolean doDouble(String name) {
        return Stream.of("IRON_ORE", "GOLD_ORE", "COAL_ORE").anyMatch(name::equalsIgnoreCase);
    }

    private boolean dontSmelt(String name) {
        return Stream.of("REDSTONE_ORE",
                        "DIAMOND_ORE",
                        "NETHER_QUARTZ_ORE",
                        "LAPIS_ORE",
                        "NETHER_GOLD_ORE",
                        "EMERALD_ORE")
                .anyMatch(name::equalsIgnoreCase);
    }

}
