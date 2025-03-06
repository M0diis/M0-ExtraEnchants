package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.WitheringEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

@EnchantWrapper(name = "Withering", maxLevel = 3)
public class WitheringWrapper extends CustomEnchantment {

    public WitheringWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isSword(item) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        if (enchant.getCustomConflicts().contains(enchantment)) {
            return true;
        }

        if (!enchant.defaultConflictsEnabled()) {
            return false;
        }

        return EEnchant.LIFESTEAL.equals(enchantment)
                || EEnchant.BERSERK.equals(enchantment)
                || Enchantment.FIRE_ASPECT.equals(enchantment);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    @EventHandler
    public void onWithering(final WitheringEvent e) {
        if (!Utils.shouldTrigger(EEnchant.WITHERING)) {
            return;
        }

        Entity target = e.getEntityDamageByEntityEvent().getEntity();

        if (EEnchant.WEBBING.isPlayerOnly() && !(target instanceof Player)) {
            return;
        }

        Player targetPlayer = (Player) target;

        int level = e.getEnchantLevel();

        Optional<PotionEffect> current = targetPlayer.getActivePotionEffects()
                .stream()
                .filter(effect -> effect.getType().equals(PotionEffectType.WITHER))
                .findFirst();

        if (current.isPresent()) {
            return;
        }

        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (level * 60), level - 1));
    }
}
