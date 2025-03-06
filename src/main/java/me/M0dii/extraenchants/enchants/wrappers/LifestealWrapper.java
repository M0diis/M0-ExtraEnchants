package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.LifestealEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@EnchantWrapper(name = "Lifesteal", maxLevel = 1)
public class LifestealWrapper extends CustomEnchantment {

    public LifestealWrapper(final String name, final int lvl, EEnchant enchant) {
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

        return enchantment.equals(Enchantment.SILK_TOUCH);
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
    public void onLifesteal(final LifestealEvent e) {
        if (!Utils.shouldTrigger(EEnchant.LIFESTEAL)) {
            return;
        }

        Player damager = e.getPlayer();

        Entity target = e.getEntityDamageByEntityEvent().getEntity();

        if (EEnchant.WEBBING.isPlayerOnly() && !(target instanceof Player)) {
            return;
        }

        double damage = e.getEntityDamageByEntityEvent().getDamage();

        double heal = damage * 0.1;

        if (damager.getHealth() + heal > 20) {
            return;
        }

        damager.setHealth(damager.getHealth() + heal);

        damager.getWorld().playSound(damager.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.1F, 0.5F);

        Location locFrom = damager.getLocation();

        Location locTo = target.getLocation();

        Vector dir = locTo.toVector().subtract(locFrom.toVector());

        World world = locFrom.getWorld();

        for (double i = 1; i <= locFrom.distance(locTo); i += 0.25) {
            dir.multiply(i);

            locFrom.add(dir);

            world.spawnParticle(Particle.HEART, locFrom, 1, 0, 0, 0, 0);
            locFrom.subtract(dir);

            dir.normalize();
        }
    }
}
