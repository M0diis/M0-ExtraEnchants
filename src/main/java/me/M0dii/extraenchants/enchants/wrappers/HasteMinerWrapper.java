package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.HasteMinerEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

@EnchantWrapper(name = "Haste Miner", maxLevel = 1)
public class HasteMinerWrapper extends CustomEnchantment {

    public HasteMinerWrapper(final String name, final int lvl, EEnchant enchant) {
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

    @EventHandler
    public void onHasteMine(final HasteMinerEvent e) {
        if (!Utils.shouldTrigger(EEnchant.HASTE_MINER)) {
            return;
        }

        Player p = e.getPlayer();

        Optional<PotionEffect> current = p.getActivePotionEffects()
                .stream()
                .filter(effect -> effect.getType().equals(PotionEffectType.HASTE))
                .findFirst();

        if (current.isPresent()) {
            return;
        }

        int level = Math.max(1, e.getEnchantLevel());

        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, (level * 100), level - 1));

        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_HIT, 0.1F, 0.1F);
    }
}
