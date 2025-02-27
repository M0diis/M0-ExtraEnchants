package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.ExperienceMinerEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.Set;

@EnchantWrapper(name = "Experience Miner", maxLevel = 1)
public class ExperienceMinerWrapper extends CustomEnchantment {

    public ExperienceMinerWrapper(final String name, final int lvl, EEnchant enchant) {
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

        return enchantment.equals(Enchantment.SILK_TOUCH);
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
    public void onExperienceMine(final ExperienceMinerEvent e) {
        if (!Utils.shouldTrigger(EEnchant.EXPERIENCE_MINER)) {
            return;
        }

        Player p = e.getPlayer();

        Block source = e.getBlock();

        if (source.hasMetadata("EXPERIENCE_MINER")) {
            return;
        }

        source.setMetadata("EXPERIENCE_MINER", new FixedMetadataValue(ExtraEnchants.getInstance(), true));

        e.getBlockBreakEvent().setExpToDrop(rnd.nextInt(p.getLevel() + 1) + 1);
    }
}
