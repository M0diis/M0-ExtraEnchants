package me.m0dii.extraenchants.enchants.wrappers;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.events.TimberEvent;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import me.m0dii.extraenchants.utils.pipeline.BlockBreakContext;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("removal")
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
                || EEnchant.SMELT.equals(enchantment);
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

    record BlockLocation(Block block, Material originalType, EquipmentSlot toolSlot, Player player, long time,
                         BlockBreakContext ctx) {
    }

    // Per-player queues for better performance
    private static final Map<Player, List<BlockLocation>> playerQueues = Collections.synchronizedMap(new HashMap<>());
    // Track blocks being broken by TIMBER to prevent re-triggering (with timestamps)
    private static final Map<Block, Long> timberBreaking = Collections.synchronizedMap(new HashMap<>());
    private WrappedTask cleanupTask;
    private WrappedTask processingTask;

    private void init() {
        // Only initialize tasks once globally
        if (processingTask == null) {
            // Block-breaking scheduler - now processes per-player queues
            processingTask = ExtraEnchants.getInstance().getScheduler().runTimer(() -> {
                boolean debug = ExtraEnchants.getInstance().getConfig().getBoolean("debug-enchants.timber", false);
                long currentTime = System.currentTimeMillis();

                // Process each player's queue independently for better performance
                playerQueues.forEach((player, queue) -> {
                    if (queue.isEmpty() || !player.isOnline()) {
                        return;
                    }

                    // Check if the tool is still in the correct slot - if not, clear the queue
                    if (!queue.isEmpty()) {
                        BlockLocation firstBlock = queue.getFirst();
                        ItemStack currentTool = firstBlock.toolSlot() == EquipmentSlot.HAND
                                ? player.getInventory().getItemInMainHand()
                                : player.getInventory().getItemInOffHand();

                        // If tool is gone or doesn't have TIMBER enchant, clear the queue and stop processing
                        if (currentTool.getType().isAir() || !InventoryUtils.hasEnchant(currentTool, EEnchant.TIMBER)) {
                            if (debug) {
                                ExtraEnchants.getInstance().getLogger().info("[TIMBER] Tool no longer in " + firstBlock.toolSlot() + " for " + player.getName() + ", clearing queue");
                            }
                            queue.clear();
                            return;
                        }
                    }

                    // Only process blocks that are ready (100ms delay passed)
                    List<BlockLocation> toBreak = queue.stream()
                            .filter(block -> currentTime - block.time() >= 100)
                            .filter(block -> !block.block().getType().isAir())
                            .filter(block -> block.block().getType() == block.originalType())
                            .filter(block -> Utils.allowedAt(block.player(), block.block().getLocation()))
                            .filter(block -> block.block().getType() != Material.WATER
                                    && block.block().getType() != Material.BUBBLE_COLUMN)
                            .limit(5) // Process max 5 blocks per player per tick for performance
                            .toList();

                    if (debug && !toBreak.isEmpty()) {
                        ExtraEnchants.getInstance().getLogger().info("[TIMBER] Breaking " + toBreak.size() + " blocks for " + player.getName());
                    }

                    toBreak.forEach(block -> {
                        // Get the tool from the slot it was originally in (we already verified it exists above)
                        ItemStack currentTool = block.toolSlot() == EquipmentSlot.HAND
                                ? player.getInventory().getItemInMainHand()
                                : player.getInventory().getItemInOffHand();

                        if (debug) {
                            ExtraEnchants.getInstance().getLogger().info("[TIMBER] Breaking: " + block.block().getType()
                                    + " at " + block.block().getLocation() + " (firing BlockBreakEvent)");
                            ExtraEnchants.getInstance().getLogger().info("[TIMBER]   Player: " + player.getName());
                            ExtraEnchants.getInstance().getLogger().info("[TIMBER]   Tool in slot " + block.toolSlot() + ": " + currentTool.getType());
                        }

                        // Mark with timestamp to prevent re-triggering
                        timberBreaking.put(block.block(), currentTime);

                        // Create and fire a BlockBreakEvent so it goes through the pipeline
                        BlockBreakEvent breakEvent = new BlockBreakEvent(block.block(), player);

                        if (debug) {
                            ExtraEnchants.getInstance().getLogger().info("[TIMBER]   Firing event...");
                        }

                        // Fire the event - this will go through the pipeline and TELEPATHY will process it
                        Bukkit.getPluginManager().callEvent(breakEvent);

                        if (debug) {
                            ExtraEnchants.getInstance().getLogger().info("[TIMBER]   Event cancelled: " + breakEvent.isCancelled());
                            ExtraEnchants.getInstance().getLogger().info("[TIMBER]   Event dropItems: " + breakEvent.isDropItems());
                        }

                        // If not cancelled by another plugin, break the block
                        if (!breakEvent.isCancelled()) {
                            // If drops are disabled by event (TELEPATHY sets this), don't spawn items
                            if (breakEvent.isDropItems()) {
                                if (debug) {
                                    ExtraEnchants.getInstance().getLogger().info("[TIMBER]   Spawning drops naturally");
                                }
                                // Spawn drops naturally using the current tool
                                block.block().getDrops(currentTool).forEach(drop ->
                                        block.block().getWorld().dropItemNaturally(block.block().getLocation(), drop)
                                );
                            } else {
                                if (debug) {
                                    ExtraEnchants.getInstance().getLogger().info("[TIMBER]   Not spawning drops (TELEPATHY handled them)");
                                }
                            }
                            block.block().setType(Material.AIR);

                            // Apply durability to the CURRENT tool in that slot (fixes swap exploit)
                            if (currentTool != null && !currentTool.getType().isAir()) {
                                InventoryUtils.applyDurabilityChanced(player, currentTool, 50);
                            }
                        }
                    });

                    queue.removeAll(toBreak);
                });

                // Clean up empty queues
                playerQueues.entrySet().removeIf(entry -> entry.getValue().isEmpty() || !entry.getKey().isOnline());
            }, 0L, 1L);

            // Global cleanup task - runs every second
            cleanupTask = ExtraEnchants.getInstance().getScheduler().runTimer(
                    () -> {
                        long now = System.currentTimeMillis();
                        // Remove blocks marked more than 250ms ago
                        timberBreaking.entrySet().removeIf(entry -> now - entry.getValue() > 250);
                    },
                    20L, // Start after 1 second
                    20L  // Run every 1 second
            );
        }
    }

    @EventHandler
    public void onTimber(final TimberEvent e) {
        boolean debug = ExtraEnchants.getInstance().getConfig().getBoolean("debug-enchants.timber", false);

        if (!Utils.shouldTrigger(EEnchant.TIMBER)) {
            return;
        }

        BlockBreakContext ctx = e.getContext();
        Block block = ctx.block();

        // Skip if this block is being broken by TIMBER itself (secondary break)
        if (timberBreaking.containsKey(block)) {
            if (debug) {
                ExtraEnchants.getInstance().getLogger().info("[TIMBER] Skipping secondary break for: " + block.getType() + " at " + block.getLocation());
            }
            return;
        }

        Player player = ctx.player();

        if (!Utils.allowedAt(player, block.getLocation())) {
            return;
        }

        if (!isLog(block)) {
            return;
        }

        Set<Block> treeBlocks = getTree(block, Set.of(block.getType()))
                .stream()
                .sorted(Comparator.comparingInt(b -> b.getLocation().getBlockY()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (debug) {
            ExtraEnchants.getInstance().getLogger().info("[TIMBER] Tree has " + treeBlocks.size() + " logs");
            ExtraEnchants.getInstance().getLogger().info("[TIMBER] Tool has TELEPATHY: " + InventoryUtils.hasEnchant(ctx.toolUsed(), EEnchant.TELEPATHY));
        }

        // Determine which hand is being used
        ItemStack offHand = player.getInventory().getItemInOffHand();
        EquipmentSlot toolSlot = EquipmentSlot.HAND;

        // Check if the tool matches the context tool (could be in either hand)
        if (ctx.toolUsed().equals(offHand)) {
            toolSlot = EquipmentSlot.OFF_HAND;
        }

        long time = System.currentTimeMillis();

        // Remove the original block from the list since it's already being broken by the event
        treeBlocks.remove(block);

        if (debug) {
            ExtraEnchants.getInstance().getLogger().info("[TIMBER] Scheduling " + treeBlocks.size() + " additional blocks for breaking");
        }

        // Get or create player queue
        List<BlockLocation> playerQueue = playerQueues.computeIfAbsent(player, k -> Collections.synchronizedList(new ArrayList<>()));

        for (Block log : treeBlocks) {
            if (debug) {
                ExtraEnchants.getInstance().getLogger().info("[TIMBER] Scheduling block: " + log.getType() + " at " + log.getLocation());
            }

            // Schedule the block to be broken naturally (will go through pipeline)
            // Store the slot instead of the ItemStack reference to prevent swap exploit
            playerQueue.add(new BlockLocation(log, log.getType(), toolSlot, player, time, ctx));
            time += (long) (Math.random() * 2 * 100);

            // Don't apply durability here - it will be applied when the block is actually broken
            // This prevents the swap exploit
        }

        if (debug) {
            ExtraEnchants.getInstance().getLogger().info("[TIMBER] Scheduled " + treeBlocks.size() + " blocks for natural breaking");
        }
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