package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.AntiThornsEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.Set;

@EnchantWrapper(name = "Anti Thorns", maxLevel = 1)
public class AntiThornsWrapper extends CustomEnchantment {

    public AntiThornsWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isArmor(item) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public int getAnvilCost() {
        return 0;
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
        return Set.of(
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        );
    }

    private static final Random random = new Random();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAntiThorns(final AntiThornsEvent e) {
        if (!Utils.shouldTrigger(EEnchant.ANTI_THORNS)) {
            return;
        }

        Player p = e.getPlayer();

        ItemStack helmet = e.getPlayer().getInventory().getHelmet();
        ItemStack chestplate = e.getPlayer().getInventory().getChestplate();
        ItemStack leggings = e.getPlayer().getInventory().getLeggings();
        ItemStack boots = e.getPlayer().getInventory().getBoots();

        int deflectPercentage = 0;

        if (helmet != null) {
            if (helmet.containsEnchantment(EEnchant.ANTI_THORNS.getEnchantment())) {
                InventoryUtils.applyDurability(p, helmet);

                deflectPercentage += 25;
            }
        }

        if (chestplate != null) {
            if (chestplate.containsEnchantment(EEnchant.ANTI_THORNS.getEnchantment())) {
                InventoryUtils.applyDurability(p, chestplate);

                deflectPercentage += 25;
            }
        }

        if (leggings != null) {
            if (leggings.containsEnchantment(EEnchant.ANTI_THORNS.getEnchantment())) {
                InventoryUtils.applyDurability(p, leggings);

                deflectPercentage += 25;
            }
        }

        if (boots != null) {
            if (boots.containsEnchantment(EEnchant.ANTI_THORNS.getEnchantment())) {
                InventoryUtils.applyDurability(p, boots);

                deflectPercentage += 25;
            }
        }

        if (deflectPercentage == 100) {
            e.getEntityDamageEvent().setCancelled(true);
        } else {
            if (random.nextInt(100) < deflectPercentage) {
                e.getEntityDamageEvent().setCancelled(true);
            }
        }
    }
}
