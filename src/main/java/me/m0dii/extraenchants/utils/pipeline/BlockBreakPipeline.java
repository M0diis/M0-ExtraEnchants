package me.m0dii.extraenchants.utils.pipeline;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.wrappers.SmeltWrapper;
import me.m0dii.extraenchants.events.*;
import me.m0dii.extraenchants.listeners.BlockBreak;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;

public class BlockBreakPipeline {

    private static final Set<EEnchant> orderedSteps = new LinkedHashSet<>(
            List.of(
                    EEnchant.EXCAVATOR,
                    EEnchant.TUNNEL,
                    EEnchant.VEIN_MINER,
                    EEnchant.SMELT,
                    EEnchant.REPLANTER,
                    EEnchant.TIMBER,
                    EEnchant.TELEPATHY,
                    EEnchant.STAT_TRACK,
                    EEnchant.HASTE_MINER,
                    EEnchant.EXPERIENCE_MINER,
                    EEnchant.DISPOSER
            )
    );

    private final ExtraEnchants plugin;
    private final Map<EEnchant, BlockBreakAction> registry = new LinkedHashMap<>();

    public BlockBreakPipeline(ExtraEnchants plugin) {
        this.plugin = plugin;
        registerDefaults();
    }

    public void run(@NotNull BlockBreakContext ctx) {
        boolean hasAnyEnchant = false;
        boolean debug = plugin.getConfig().getBoolean("debug-enchants.pipeline", false);

        if (debug) {
            plugin.getLogger().info("=== BlockBreakPipeline Debug ===");
            plugin.getLogger().info("Block: " + ctx.block().getType() + " at " + ctx.block().getLocation());
            plugin.getLogger().info("Tool: " + ctx.toolUsed().getType());
            plugin.getLogger().info("Initial drops count: " + ctx.getDrops().size());
            plugin.getLogger().info("Initial spawnDrops: " + ctx.isSpawnDrops());
            plugin.getLogger().info("Event dropItems: " + ctx.getEvent().isDropItems());
        }

        for (EEnchant enchant : orderedSteps) {
            if (ctx.getEvent().isCancelled()) {
                if (debug) {
                    plugin.getLogger().info("Pipeline stopped - event cancelled at: " + enchant);
                }
                break;
            }
            BlockBreakAction action = registry.get(enchant);
            try {
                if (action != null && action.shouldRun().test(ctx)) {
                    if (debug) {
                        plugin.getLogger().info(">>> Running: " + enchant);
                        plugin.getLogger().info("    Before - Drops: " + ctx.getDrops().size()
                            + ", SpawnDrops: " + ctx.isSpawnDrops()
                            + ", EventDropItems: " + ctx.getEvent().isDropItems()
                            + ", EventCancelled: " + ctx.getEvent().isCancelled());
                    }

                    action.run().accept(ctx);
                    hasAnyEnchant = true;

                    if (debug) {
                        plugin.getLogger().info("    After  - Drops: " + ctx.getDrops().size()
                            + ", SpawnDrops: " + ctx.isSpawnDrops()
                            + ", EventDropItems: " + ctx.getEvent().isDropItems()
                            + ", EventCancelled: " + ctx.getEvent().isCancelled());
                    }
                }
            } catch (Exception t) {
                plugin.getLogger().warning("[BlockBreakPipeline] Step '" + enchant + "' failed: " + t.getMessage());
                if (debug) {
                    plugin.getLogger().log(Level.WARNING, "[BlockBreakPipeline] Stacktrace for failed step '" + enchant + "'", t);
                }
            }
        }

        if(!hasAnyEnchant) {
            if (debug) {
                plugin.getLogger().info("No custom enchantments triggered");
            }
            return; // No custom enchantments triggered, do nothing
        }

        if (debug) {
            plugin.getLogger().info("Final - SpawnDrops: " + ctx.isSpawnDrops() + ", Drops count: " + ctx.getDrops().size());
        }

        // Spawn items if vanilla allowed OR a plugin step explicitly requested spawning plugin-controlled drops
        if (ctx.isSpawnDrops()) {
            if (debug) {
                plugin.getLogger().info("Spawning " + ctx.getDrops().size() + " drops from pipeline");
            }
            dropItems(ctx);
        } else {
            if (debug) {
                plugin.getLogger().info("Not spawning drops (spawnDrops=false)");
            }
        }

        if (debug) {
            plugin.getLogger().info("=== End Pipeline Debug ===");
        }
    }

    public void dropItems(@NotNull BlockBreakContext ctx) {
        for (var itemStack : ctx.getDrops()) {
            ctx.block().getWorld().dropItemNaturally(ctx.block().getLocation(), itemStack);
        }
    }

    private void registerDefaults() {
        // SMELT
        register(
                EEnchant.SMELT,
                hasEnchant(EEnchant.SMELT)
                        .and(ctx -> !SmeltWrapper.dontSmelt(ctx.block().getType())),
                ctx -> {
                    // Prevent vanilla drops - we will spawn ctx.getDrops() ourselves:
                    ctx.getEvent().setDropItems(false);
                    ctx.setSpawnDrops(true); // plugin will spawn the smelted drops later
                    Bukkit.getPluginManager().callEvent(new SmeltEvent(ctx));
                }
        );

        // STAT_TRACK
        register(
                EEnchant.STAT_TRACK,
                hasEnchant(EEnchant.STAT_TRACK),
                ctx -> Bukkit.getPluginManager().callEvent(new StatTrackEvent(ctx))
        );

        // HASTE_MINER
        register(
                EEnchant.HASTE_MINER,
                hasEnchant(EEnchant.HASTE_MINER),
                ctx -> Bukkit.getPluginManager().callEvent(new HasteMinerEvent(ctx))
        );

        // EXPERIENCE_MINER
        register(
                EEnchant.EXPERIENCE_MINER,
                hasEnchant(EEnchant.EXPERIENCE_MINER),
                ctx -> Bukkit.getPluginManager().callEvent(new ExperienceMinerEvent(ctx))
        );

        // VEIN_MINER
        register(
                EEnchant.VEIN_MINER,
                hasEnchant(EEnchant.VEIN_MINER),
                ctx -> Bukkit.getPluginManager().callEvent(new VeinMinerEvent(ctx))
        );

        // TUNNEL
        register(
                EEnchant.TUNNEL,
                hasEnchant(EEnchant.TUNNEL),
                ctx -> {
                    // Prevent vanilla drops - we will spawn ctx.getDrops() ourselves:
                    ctx.getEvent().setDropItems(false);
                    ctx.setSpawnDrops(true); // plugin will spawn the smelted drops later
                    Bukkit.getPluginManager().callEvent(new TunnelEvent(ctx));
                }
        );

        // EXCAVATOR
        register(
                EEnchant.EXCAVATOR,
                hasEnchant(EEnchant.EXCAVATOR)
                        .and(ctx -> !ctx.isExcavatorSecondary())
                        .and(ctx -> !EEnchant.EXCAVATOR.ignoresBlock(ctx.block().getType())),
                ctx -> {
                    ctx.setSpawnDrops(true);
                    Bukkit.getPluginManager().callEvent(new ExcavatorEvent(ctx));
                }
        );

        // DISPOSER
        register(
                EEnchant.DISPOSER,
                hasEnchant(EEnchant.DISPOSER),
                ctx -> Bukkit.getPluginManager().callEvent(new DisposerEvent(ctx))
        );

        // REPLANTER (requires hoe)
        register(
                EEnchant.REPLANTER,
                hasEnchant(EEnchant.REPLANTER)
                        .and(ctx -> EnchantableItemTypeUtil.isHoe(ctx.toolUsed())),
                ctx -> Bukkit.getPluginManager().callEvent(new ReplanterBreakEvent(ctx))
        );

        // TIMBER (requires axe + a log)
        register(
                EEnchant.TIMBER,
                hasEnchant(EEnchant.TIMBER)
                        .and(ctx -> EnchantableItemTypeUtil.isAxe(ctx.toolUsed()))
                        .and(ctx -> BlockBreak.isLog(ctx.block())),
                ctx -> Bukkit.getPluginManager().callEvent(new TimberEvent(ctx))
        );

        // TELEPATHY
        register(
                EEnchant.TELEPATHY,
                hasEnchant(EEnchant.TELEPATHY)
                        .and(ctx -> !ctx.getDrops().isEmpty()),
                ctx -> {
                    // Telepathy will handle items itself (inventory or world), prevent vanilla and ensure pipeline does not spawn them
                    ctx.getEvent().setDropItems(false);
                    ctx.setSpawnDrops(false);
                    Bukkit.getPluginManager().callEvent(new TelepathyEvent(ctx));
                }
        );
    }

    private void register(EEnchant enchant, Predicate<BlockBreakContext> shouldRun, Consumer<BlockBreakContext> run) {
        registry.put(enchant, new BlockBreakAction(enchant, shouldRun, run));
    }

    private static Predicate<BlockBreakContext> hasEnchant(EEnchant enchant) {
        return ctx -> InventoryUtils.hasEnchant(ctx.toolUsed(), enchant);
    }
}