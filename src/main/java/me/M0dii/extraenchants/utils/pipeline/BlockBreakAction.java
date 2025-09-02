package me.m0dii.extraenchants.utils.pipeline;

import me.m0dii.extraenchants.enchants.EEnchant;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class BlockBreakAction {
    private final EEnchant enchant;
    private final Predicate<BlockBreakContext> shouldRun;
    private final Consumer<BlockBreakContext> run;

    public BlockBreakAction(EEnchant enchant, Predicate<BlockBreakContext> shouldRun, Consumer<BlockBreakContext> run) {
        this.enchant = enchant;
        this.shouldRun = shouldRun;
        this.run = run;
    }

    public EEnchant enchant() { return enchant; }
    public Predicate<BlockBreakContext> shouldRun() { return shouldRun; }
    public Consumer<BlockBreakContext> run() { return run; }
}