package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.events.TurtleShellEvent;
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

@EnchantWrapper(name = "Turtle Shell", maxLevel = 1)
public class TurtleShellWrapper extends CustomEnchantment {

    public TurtleShellWrapper(final String name, final int lvl, EEnchant enchant) {
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
                || enchantment.equals(EEnchant.ANTI_THORNS.getEnchantment());
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
    public void onColdSteel(final TurtleShellEvent e) {
        Messenger.debug("TurtleShellEvent called.");

        if (!Utils.shouldTrigger(EEnchant.TURTLE_SHELL)) {
            return;
        }

        if (!(e.getEntityDamageByEntityEvent().getEntity() instanceof Player receiver)) {
            return;
        }

        if (!(e.getEntityDamageByEntityEvent().getDamager() instanceof Player attacker)) {
            return;
        }

        double dot = attacker.getLocation().getDirection().dot(receiver.getLocation().getDirection());

        Messenger.debug("Dot: " + dot);

        if (dot < 0.8) {
            Messenger.debug("Dot is less than 0.75.");
            return;
        }

        double percentage = 0;

        ItemStack helmet = receiver.getInventory().getHelmet();
        ItemStack chestplate = receiver.getInventory().getChestplate();
        ItemStack leggings = receiver.getInventory().getLeggings();
        ItemStack boots = receiver.getInventory().getBoots();

        if (InventoryUtils.hasEnchant(helmet, EEnchant.TURTLE_SHELL)) {
            if (Utils.shouldTrigger(EEnchant.TURTLE_SHELL)) {
                InventoryUtils.applyDurability(receiver, helmet);

                percentage += 0.025;
            }
        }

        if (InventoryUtils.hasEnchant(chestplate, EEnchant.TURTLE_SHELL)) {
            if (Utils.shouldTrigger(EEnchant.TURTLE_SHELL)) {
                InventoryUtils.applyDurability(receiver, chestplate);

                percentage += 0.025;
            }
        }

        if (InventoryUtils.hasEnchant(leggings, EEnchant.TURTLE_SHELL)) {
            if (Utils.shouldTrigger(EEnchant.TURTLE_SHELL)) {
                InventoryUtils.applyDurability(receiver, leggings);

                percentage += 0.025;
            }
        }

        if (InventoryUtils.hasEnchant(boots, EEnchant.TURTLE_SHELL)) {
            if (Utils.shouldTrigger(EEnchant.TURTLE_SHELL)) {
                InventoryUtils.applyDurability(receiver, boots);

                percentage += 0.025;
            }
        }

        if (percentage == 0) {
            return;
        }

        double damage = e.getEntityDamageByEntityEvent().getDamage();

        e.getEntityDamageByEntityEvent().setDamage(damage * (1 - percentage));
    }
}
