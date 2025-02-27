package me.m0dii.extraenchants.enchants.wrappers;

import lombok.Getter;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.TimberEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@EnchantWrapper(name = "Timber", maxLevel = 1)
public class TimberWrapper extends CustomEnchantment {

    public TimberWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);

        init();
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isAxe(item) || enchant.canEnchantItemCustom(item);
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
                || EEnchant.SMELT.equals(enchantment)
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

    @Override
    public @NotNull String translationKey() {
        return name.toLowerCase();
    }

    @Getter
    static class BlockLocation {
        private final Block block;
        private final ItemStack tool;
        private final long time;

        public BlockLocation(Block block, ItemStack tool, long time) {
            this.block = block;
            this.tool = tool;
            this.time = time;
        }
    }

    private final List<BlockLocation> pendingToBreak = new ArrayList<>();

    private static BukkitTask breakTask;

    private void init() {
        breakTask = Bukkit.getServer().getScheduler().runTaskTimer(ExtraEnchants.getInstance(), () -> {
            List<BlockLocation> toBreak = pendingToBreak.stream()
                    .filter(block -> System.currentTimeMillis() - block.getTime() >= 100)
                    .sorted(Comparator.comparingLong(BlockLocation::getTime))
                    .toList();

            for (BlockLocation block : toBreak) {
                block.getBlock().breakNaturally(block.getTool());
            }

            pendingToBreak.removeAll(toBreak);
        }, 0L, 1L);
    }

    @EventHandler
    public void onTimber(final TimberEvent e) {
        if (!Utils.shouldTrigger(EEnchant.TIMBER)) {
            return;
        }

        if (!Utils.allowedAt(e.getPlayer(), e.getBlock().getLocation())) {
            return;
        }

        Block block = e.getBlock();

        if (!isLog(block)) {
            return;
        }

        Set<Block> treeBlocks = getTree(block, Set.of(block.getType()))
                .stream()
                .sorted(Comparator.comparingInt(b -> b.getLocation().getBlockY()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        e.getBlockBreakEvent().setCancelled(true);

        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();

        block.breakNaturally(hand);

        long time = System.currentTimeMillis();

        for (Block log : treeBlocks) {
            pendingToBreak.add(new BlockLocation(log, hand, time));

            time += Math.random() * 2 * 100;

            InventoryUtils.applyDurabilityChanced(e.getPlayer(), hand, 50);
        }

        pendingToBreak.sort(Comparator.comparingLong(BlockLocation::getTime));
    }

    private static boolean isLog(Block block) {
        String name = block.getType().name();
        return name.contains("LOG") && !name.contains("STRIPPED");
    }

    private static Set<Block> getTree(Block start, Set<Material> allowedMaterials) {
        return getNearbyBlocks(start, allowedMaterials, new HashSet<>());
    }

    private static Set<Block> getNearbyBlocks(@NotNull Block start,
                                              @NotNull Set<Material> allowedMaterials,
                                              @NotNull Set<Block> blocks) {
        if (blocks.size() >= 64) {
            return blocks;
        }

        List<Block> candidates = new ArrayList<>();
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    Block block = start.getLocation().clone().add(x, y, z).getBlock();
                    if (block.getType().isAir() || blocks.contains(block) || !allowedMaterials.contains(block.getType())) {
                        continue;
                    }
                    candidates.add(block);
                }
            }
        }

        Collections.shuffle(candidates);

        for (Block block : candidates) {
            blocks.add(block);
            getNearbyBlocks(block, allowedMaterials, blocks);
        }

        return blocks;
    }
}