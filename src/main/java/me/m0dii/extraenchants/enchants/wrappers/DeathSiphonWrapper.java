package me.m0dii.extraenchants.enchants.wrappers;

import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.DeathSiphonEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Messenger;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.Set;

@EnchantWrapper(name = "Death Siphon", maxLevel = 1)
public class DeathSiphonWrapper extends CustomEnchantment {

    public DeathSiphonWrapper(final String name, final int lvl, EEnchant enchant) {
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

    private static final Random random = new Random();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAntiThorns(final DeathSiphonEvent e) {
        Messenger.debug("DeathSiphonEvent called");

        if (EEnchant.DEATH_SIPHON.isDisabled()) {
            return;
        }

        Entity target = e.getEntityDeathEvent().getEntity();

        if (EEnchant.DEATH_SIPHON.isPlayerOnly() && !(target instanceof Player)) {
            return;
        }

        Player player = e.getPlayer();

        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        if (InventoryUtils.hasEnchant(helmet, EEnchant.DEATH_SIPHON)) {
            InventoryUtils.applyDurability(player, helmet);

            if (random.nextInt(100) < EEnchant.DEATH_SIPHON.getTriggerChance()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 4));
            }
        }

        if (InventoryUtils.hasEnchant(chestplate, EEnchant.DEATH_SIPHON)) {
            InventoryUtils.applyDurability(player, chestplate);

            if (random.nextInt(100) < EEnchant.DEATH_SIPHON.getTriggerChance()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 4));
            }
        }

        if (InventoryUtils.hasEnchant(leggings, EEnchant.DEATH_SIPHON)) {
            InventoryUtils.applyDurability(player, leggings);

            if (random.nextInt(100) < EEnchant.DEATH_SIPHON.getTriggerChance()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 4));
            }
        }

        if (InventoryUtils.hasEnchant(boots, EEnchant.DEATH_SIPHON)) {
            InventoryUtils.applyDurability(player, boots);

            if (random.nextInt(100) < EEnchant.DEATH_SIPHON.getTriggerChance()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 4));
            }
        }
    }
}
