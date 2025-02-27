package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.SmeltEvent;
import me.m0dii.extraenchants.events.TelepathyEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

@EnchantWrapper(name = "Smelt", maxLevel = 1)
public class SmeltWrapper extends CustomEnchantment {

    public SmeltWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isTool(item, false) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        if (enchant.getCustomConflicts().contains(enchantment)) {
            return true;
        }

        if (!enchant.defaultConflictsEnabled()) {
            return false;
        }

        return Enchantment.SILK_TOUCH.equals(enchantment)
                || EEnchant.TELEPATHY.equals(enchantment);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    private static final Random rnd = new Random();

    @EventHandler
    public void onSmelt(final SmeltEvent e) {
        if (!Utils.shouldTrigger(EEnchant.SMELT)) {
            return;
        }

        Block b = e.getBlock();
        Player p = e.getPlayer();
        ItemStack tool = p.getInventory().getItemInMainHand();

        Collection<ItemStack> drops = e.getDrops();
        List<ItemStack> results = new ArrayList<>();

        Iterator<Recipe> recipes = Bukkit.recipeIterator();

        boolean hasFortune = tool.getItemMeta().getEnchants()
                .containsKey(Enchantment.FORTUNE);

        int fortuneLevel = 1;

        if (hasFortune) {
            fortuneLevel = tool.getItemMeta().getEnchants()
                    .get(Enchantment.FORTUNE);
        }

        while (recipes.hasNext() && !dontSmelt(b.getType().name())) {
            Recipe recipe = recipes.next();

            if (!(recipe instanceof FurnaceRecipe furnaceRecipe)) {
                continue;
            }

            if (furnaceRecipe.getInput().getType() != b.getType()) {
                continue;
            }

            if (hasFortune && doDouble(b.getType().name())) {
                int extraDrops = rnd.nextInt(fortuneLevel + 1);

                for (int i = 0; i <= extraDrops; i++) {
                    results.add(furnaceRecipe.getResult());
                }
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

        if (InventoryUtils.hasEnchant(tool, EEnchant.TELEPATHY)) {
            Bukkit.getPluginManager().callEvent(new TelepathyEvent(p, e.getBlockBreakEvent(), results));
        } else {
            for (ItemStack drop : results) {
                if (drop == null || drop.getType().isAir()) {
                    continue;
                }
                b.getWorld().dropItemNaturally(b.getLocation(), drop);
            }
        }
    }

    private boolean doDouble(String name) {
        return Stream.of("IRON_ORE", "GOLD_ORE", "COAL_ORE")
                .anyMatch(name::equalsIgnoreCase);
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
