package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.VeinMinerEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EnchantWrapper(name = "Vein Miner", maxLevel = 1)
public class VeinMinerWrapper extends CustomEnchantment {

    public VeinMinerWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isPickaxe(item) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        if (enchant.getCustomConflicts().contains(enchantment)) {
            return true;
        }

        if (!enchant.defaultConflictsEnabled()) {
            return false;
        }

        return Enchantment.SILK_TOUCH.equals(enchantment) || EEnchant.TELEPATHY.equals(enchantment);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    private static final BlockFace[] AREA = {
            BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH
    };
    private static final String META_BLOCK_VEINED = "veinminer_block_veined";

    @EventHandler
    public void onVeinMine(final VeinMinerEvent e) {
        if (!Utils.shouldTrigger(EEnchant.VEIN_MINER)) {
            return;
        }

        Player p = e.getPlayer();

        Block source = e.getBlock();

        if (source.hasMetadata(META_BLOCK_VEINED)) {
            return;
        }

        Set<Block> ores = new HashSet<>();
        Set<Block> prepare = getNearby(source, p);

        int limit = 15;

        if (prepare.isEmpty()) {
            return;
        }

        while (ores.addAll(prepare) && ores.size() < limit) {
            Set<Block> nearby = new HashSet<>();
            prepare.forEach(prepared -> nearby.addAll(getNearby(prepared, p)));
            prepare.clear();
            prepare.addAll(nearby);
        }

        ores.remove(source);

        ores.forEach(ore -> {
            ore.setMetadata(META_BLOCK_VEINED, new FixedMetadataValue(ExtraEnchants.getInstance(), true));
            ore.breakNaturally(p.getInventory().getItemInMainHand());
            ore.removeMetadata(META_BLOCK_VEINED, ExtraEnchants.getInstance());
        });

        p.getWorld().playSound(source.getLocation(), Sound.ITEM_TOTEM_USE, 0.03F, 0.5F);
    }

    @NotNull
    private Set<Block> getNearby(@NotNull Block block, Player player) {
        return Stream.of(AREA).map(block::getRelative)
                .filter(blockAdded -> blockAdded.getType() == block.getType())
                .filter(b -> b.getType().name().contains("ORE"))
                .filter(b -> Utils.allowedAt(player, b.getLocation()))
                .collect(Collectors.toSet());
    }
}
