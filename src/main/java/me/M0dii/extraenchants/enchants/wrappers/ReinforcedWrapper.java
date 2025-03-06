package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.events.ReinforcedEvent;
import me.m0dii.extraenchants.utils.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@EnchantWrapper(name = "Reinforced", maxLevel = 1)
public class ReinforcedWrapper extends CustomEnchantment {

    public ReinforcedWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isArmor(item) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public boolean conflictsWith(final @NotNull Enchantment enchantment) {
        if (enchant.getCustomConflicts().contains(enchantment)) {
            return true;
        }

        if (!enchant.defaultConflictsEnabled()) {
            return false;
        }

        return enchantment.equals(Enchantment.THORNS)
                || enchantment.equals(EEnchant.ANTI_THORNS.getEnchantment())
                || enchantment.equals(EEnchant.TURTLE_SHELL.getEnchantment());
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        );
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onColdSteel(final ReinforcedEvent e) {
        Messenger.debug("ReinforcedEvent called.");

        if (!Utils.shouldTrigger(EEnchant.REINFORCED)) {
            return;
        }

        if (!(e.getEntityDamageByEntityEvent().getEntity() instanceof Player receiver)) {
            return;
        }

        if (!(e.getEntityDamageByEntityEvent().getDamager() instanceof Player)) {
            return;
        }

        double percentage = 0.015;

        int maxCount = 1;

        ItemStack helmet = receiver.getInventory().getHelmet();
        ItemStack chestplate = receiver.getInventory().getChestplate();
        ItemStack leggings = receiver.getInventory().getLeggings();
        ItemStack boots = receiver.getInventory().getBoots();

        if (InventoryUtils.hasEnchant(helmet, EEnchant.REINFORCED)) {
            if (Utils.shouldTrigger(EEnchant.REINFORCED)) {
                InventoryUtils.applyDurability(receiver, helmet);

                maxCount++;
            }
        }

        if (InventoryUtils.hasEnchant(chestplate, EEnchant.REINFORCED)) {
            if (Utils.shouldTrigger(EEnchant.REINFORCED)) {
                InventoryUtils.applyDurability(receiver, chestplate);

                maxCount++;
            }
        }

        if (InventoryUtils.hasEnchant(leggings, EEnchant.REINFORCED)) {
            if (Utils.shouldTrigger(EEnchant.REINFORCED)) {
                InventoryUtils.applyDurability(receiver, leggings);

                maxCount++;
            }
        }

        if (InventoryUtils.hasEnchant(boots, EEnchant.REINFORCED)) {
            if (Utils.shouldTrigger(EEnchant.REINFORCED)) {
                InventoryUtils.applyDurability(receiver, boots);

                maxCount++;
            }
        }

        if (maxCount == 1) {
            return;
        }

        double damage = e.getEntityDamageByEntityEvent().getDamage();

        e.getEntityDamageByEntityEvent().setDamage(damage * (1 - (percentage * maxCount)));
    }
}
