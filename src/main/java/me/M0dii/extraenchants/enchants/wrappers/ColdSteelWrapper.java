package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.events.ColdSteelEvent;
import me.m0dii.extraenchants.utils.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@EnchantWrapper(name = "Cold Steel", maxLevel = 1)
public class ColdSteelWrapper extends CustomEnchantment {

    public ColdSteelWrapper(final String name, final int lvl, EEnchant enchant) {
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

        return enchantment.equals(Enchantment.THORNS);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onColdSteel(final ColdSteelEvent e) {
        Messenger.debug("ColdSteelEvent called.");

        if (!Utils.shouldTrigger(EEnchant.COLD_STEEL)) {
            return;
        }

        Player player = e.getPlayer();

        boolean apply = false;

        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        if (InventoryUtils.hasEnchant(helmet, EEnchant.COLD_STEEL)) {
            if (Utils.shouldTrigger(EEnchant.COLD_STEEL)) {
                InventoryUtils.applyDurability(player, helmet);

                apply = true;
            }
        }

        if (InventoryUtils.hasEnchant(chestplate, EEnchant.COLD_STEEL)) {
            if (Utils.shouldTrigger(EEnchant.COLD_STEEL)) {
                InventoryUtils.applyDurability(player, chestplate);

                apply = true;
            }
        }

        if (InventoryUtils.hasEnchant(leggings, EEnchant.COLD_STEEL)) {
            if (Utils.shouldTrigger(EEnchant.COLD_STEEL)) {
                InventoryUtils.applyDurability(player, leggings);

                apply = true;
            }
        }

        if (InventoryUtils.hasEnchant(boots, EEnchant.COLD_STEEL)) {
            if (Utils.shouldTrigger(EEnchant.COLD_STEEL)) {
                InventoryUtils.applyDurability(player, boots);

                apply = true;
            }
        }

        if (!apply) {
            return;
        }

        if (!(e.getEntityDamageByEntityEvent().getEntity() instanceof Player receiver)) {
            return;
        }

        receiver.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
        receiver.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 60, 0));
    }
}
