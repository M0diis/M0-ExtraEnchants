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

public class BlockBreakPipeline {

    private static final Set<EEnchant> orderedSteps = new LinkedHashSet<>(
            List.of(
                    EEnchant.SMELT,
                    EEnchant.REPLANTER,
                    EEnchant.TELEPATHY,
                    EEnchant.STAT_TRACK,
                    EEnchant.HASTE_MINER,
                    EEnchant.EXPERIENCE_MINER,
                    EEnchant.VEIN_MINER,
                    EEnchant.TUNNEL,
                    EEnchant.EXCAVATOR,
                    EEnchant.DISPOSER,
                    EEnchant.TIMBER
            )
    );

    private final ExtraEnchants plugin;
    private final Map<EEnchant, BlockBreakAction> registry = new LinkedHashMap<>();

    public BlockBreakPipeline(ExtraEnchants plugin) {
        this.plugin = plugin;
        registerDefaults();
    }

    public void run(@NotNull BlockBreakContext ctx) {
        for (EEnchant enchant : orderedSteps) {
            if (ctx.getEvent().isCancelled()) {
                break;
            }
            BlockBreakAction action = registry.get(enchant);
            try {
                if (action != null && action.shouldRun().test(ctx)) {
                    action.run().accept(ctx);
                }
            } catch (Exception t) {
                plugin.getLogger().warning("[BlockBreakPipeline] Step '" + enchant + "' failed: " + t.getMessage());
            }
        }
    }

    private void registerDefaults() {
        // SMELT
        register(
                EEnchant.SMELT,
                hasEnchant(EEnchant.SMELT)
                        .and(ctx -> !SmeltWrapper.dontSmelt(ctx.block().getType())),
                ctx -> {
                    ctx.getEvent().setDropItems(false);

                    if (InventoryUtils.hasEnchant(ctx.toolUsed(), EEnchant.TELEPATHY)) {
                        SmeltWrapper.smeltInPlace(ctx);
                    } else {
                        Bukkit.getPluginManager().callEvent(new SmeltEvent(ctx));
                    }
                }
        );

        // TELEPATHY
        register(
                EEnchant.TELEPATHY,
                hasEnchant(EEnchant.TELEPATHY)
                        .and(ctx -> !ctx.getDrops().isEmpty()),
                ctx -> {
                    ctx.getEvent().setDropItems(false);
                    Bukkit.getPluginManager().callEvent(new TelepathyEvent(ctx));
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
                ctx -> Bukkit.getPluginManager().callEvent(new TunnelEvent(ctx))
        );

        // EXCAVATOR
        register(
                EEnchant.EXCAVATOR,
                hasEnchant(EEnchant.EXCAVATOR)
                        .and(ctx -> !ctx.isExcavatorSecondary()),
                ctx -> Bukkit.getPluginManager().callEvent(new ExcavatorEvent(ctx))
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
    }

    private void register(EEnchant enchant, Predicate<BlockBreakContext> shouldRun, Consumer<BlockBreakContext> run) {
        registry.put(enchant, new BlockBreakAction(enchant, shouldRun, run));
    }

    private static Predicate<BlockBreakContext> hasEnchant(EEnchant enchant) {
        return ctx -> InventoryUtils.hasEnchant(ctx.toolUsed(), enchant);
    }
}