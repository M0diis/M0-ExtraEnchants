package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.events.SmeltEvent;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import me.m0dii.extraenchants.utils.pipeline.BlockBreakContext;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("removal")
@EnchantWrapper(name = "Smelt", maxLevel = 1)
public class SmeltWrapper extends CustomEnchantment {

    public SmeltWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    public static void smeltInPlace(BlockBreakContext ctx) {
        if (!Utils.shouldTrigger(EEnchant.SMELT)) {
            return;
        }

        ItemStack tool = ctx.toolUsed();

        Collection<ItemStack> drops = ctx.getDrops();
        List<ItemStack> results = new ArrayList<>();

        boolean hasFortune = InventoryUtils.hasEnchant(tool, Enchantment.FORTUNE);

        int fortuneLevel = 1;

        if (hasFortune) {
            fortuneLevel = InventoryUtils.getEnchantLevel(tool, Enchantment.FORTUNE);
        }
        // Build a map from input material -> furnace result once
        Map<Material, ItemStack> smeltMap = new HashMap<>();
        Iterator<Recipe> recipesIter = Bukkit.recipeIterator();
        while (recipesIter.hasNext()) {
            Recipe recipe = recipesIter.next();
            if (!(recipe instanceof FurnaceRecipe furnaceRecipe)) {
                continue;
            }
            Material input = furnaceRecipe.getInput().getType();
            // Keep the first found mapping for this input
            smeltMap.putIfAbsent(input, furnaceRecipe.getResult());
        }

        // Process each drop using the prebuilt map
        for (ItemStack drop : drops) {
            Material dropType = drop.getType();

            if (dontSmelt(dropType)) {
                continue;
            }

            ItemStack resultTemplate = smeltMap.get(dropType);
            if (resultTemplate == null) {
                continue;
            }

            // Account for stack size and fortune-based extra outputs
            int totalAmount = 0;
            int dropAmount = Math.max(1, drop.getAmount());
            for (int i = 0; i < dropAmount; i++) {
                if (hasFortune && doDouble(dropType)) {
                    int extraDrops = rnd.nextInt(fortuneLevel + 1);
                    totalAmount += 1 + extraDrops;
                } else {
                    totalAmount += 1;
                }
            }

            ItemStack res = resultTemplate.clone();
            res.setAmount(totalAmount);
            results.add(res);
        }

        if (results.isEmpty()) {
            results = new ArrayList<>(drops);
        }

        if (InventoryUtils.hasEnchant(tool, Enchantment.SILK_TOUCH)) {
            results = new ArrayList<>(drops);
        }

        ctx.setDrops(results);
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

        BlockBreakContext ctx = e.getContext();

        smeltInPlace(ctx);
    }

    private static final Set<Material> DOUBLE_SMELT_MATERIALS = EnumSet.of(
            Material.IRON_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.GOLD_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.COAL_ORE,
            Material.DEEPSLATE_COAL_ORE
    );

    private static boolean doDouble(@NotNull Material material) {
        return DOUBLE_SMELT_MATERIALS.contains(material);
    }

    private static final Set<Material> UNSMELTABLE_MATERIALS = EnumSet.of(
            Material.REDSTONE_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.DIAMOND_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.NETHER_GOLD_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.LAPIS_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.EMERALD_ORE,
            Material.DEEPSLATE_EMERALD_ORE,
            Material.COAL_ORE,
            Material.DEEPSLATE_COAL_ORE,
            Material.ANCIENT_DEBRIS
    );

    public static boolean dontSmelt(@NotNull Material material) {
        return UNSMELTABLE_MATERIALS.contains(material);
    }
}
