package me.m0dii.extraenchants.utils.pipeline;

import lombok.Getter;
import lombok.Setter;
import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
public class BlockBreakContext {

    private final ExtraEnchants plugin;
    private final BlockBreakEvent event;
    private final ItemStack toolUsed;

    private List<ItemStack> drops;

    // NEW: when true the pipeline should spawn ctx.getDrops() even if event.setDropItems(false)
    private boolean spawnDrops = false;

    private boolean excavatorSecondary;

    public BlockBreakContext(ExtraEnchants plugin, BlockBreakEvent event) {
        this.plugin = plugin;
        this.event = event;
        this.toolUsed = event.getPlayer().getInventory().getItemInMainHand();
        this.drops = new ArrayList<>(event.getBlock().getDrops(event.getPlayer().getInventory().getItemInMainHand()));
    }

    public Player player() {
        return event.getPlayer();
    }

    public Block block() {
        return event.getBlock();
    }

    public ItemStack toolUsed() {
        return toolUsed;
    }

    public void addDrops(@NotNull Collection<ItemStack> additionalDrops) {
        this.drops.addAll(additionalDrops);
    }
}