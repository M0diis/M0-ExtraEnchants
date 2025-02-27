package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.ArmorBreakerEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@EnchantWrapper(name = "Armor Breaker", maxLevel = 1)
public class ArmorBreakerWrapper extends CustomEnchantment {

    public ArmorBreakerWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isAxe(item) || EnchantableItemTypeUtil.isSword(item) || enchant.canEnchantItemCustom(item);
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
                || EEnchant.WITHERING.equals(enchantment);
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
    public void onArmorBreaker(final ArmorBreakerEvent e) {
        if (!Utils.shouldTrigger(EEnchant.ARMOR_BREAKER)) {
            return;
        }

        Player damager = e.getPlayer();

        Entity target = e.getEntityDamageEvent().getEntity();

        if (!(target instanceof Player targetPlayer)) {
            return;
        }

        int level = e.getEnchantLevel();

        ItemStack helmet = targetPlayer.getInventory().getHelmet();
        ItemStack chestplate = targetPlayer.getInventory().getChestplate();
        ItemStack leggings = targetPlayer.getInventory().getLeggings();
        ItemStack boots = targetPlayer.getInventory().getBoots();

        int random = (int) (Math.random() * 4);

        switch (random) {
            case 0:
                if (helmet != null) {
                    InventoryUtils.applyDurability(damager, helmet);
                }
                break;
            case 1:
                if (chestplate != null) {
                    InventoryUtils.applyDurability(damager, chestplate);
                }
                break;
            case 2:
                if (leggings != null) {
                    InventoryUtils.applyDurability(damager, leggings);
                }
                break;
            case 3:
                if (boots != null) {
                    InventoryUtils.applyDurability(damager, boots);
                }
                break;
        }
    }
}
