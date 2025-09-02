package me.m0dii.extraenchants.enchants.wrappers;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import me.m0dii.extraenchants.enchants.CustomEnchantment;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.BatVisionEvent;
import me.m0dii.extraenchants.enchants.EnchantWrapper;
import me.m0dii.extraenchants.utils.EnchantableItemTypeUtil;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@EnchantWrapper(name = "Bat Vision", maxLevel = 1)
public class BatVisionWrapper extends CustomEnchantment {

    public BatVisionWrapper(final String name, final int lvl, EEnchant enchant) {
        super(name, lvl, enchant);
    }

    @Override
    public boolean canEnchantItem(final @NotNull ItemStack item) {
        return EnchantableItemTypeUtil.isHelmet(item) || enchant.canEnchantItemCustom(item);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return Set.of(EquipmentSlot.HEAD);
    }


    @EventHandler
    public void onBatVision(final BatVisionEvent e) {
        if (!Utils.shouldTrigger(EEnchant.BAT_VISION)) {
            return;
        }

        Player p = e.getPlayer();

        PotionEffect current = p.getPotionEffect(PotionEffectType.NIGHT_VISION);

        if (current != null && current.getDuration() > 1200) {
            return;
        }

        InventoryUtils.applyDurabilityChanced(p, p.getInventory().getHelmet(), 50);

        p.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(3600, 0));
    }

    @EventHandler
    public void onPlayerMoveBatVision(final PlayerMoveEvent e) {
        Player p = e.getPlayer();

        ItemStack helmet = p.getInventory().getHelmet();

        if (helmet == null) {
            return;
        }

        if (!InventoryUtils.hasEnchant(helmet, EEnchant.BAT_VISION)) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new BatVisionEvent(p, e));
    }

    @EventHandler
    public void onArmorChangeBatVision(final PlayerArmorChangeEvent e) {
        Player p = e.getPlayer();

        if (e.getSlotType() != PlayerArmorChangeEvent.SlotType.HEAD) {
            return;
        }

        ItemStack previous = e.getOldItem();

        if (InventoryUtils.hasEnchant(previous, EEnchant.BAT_VISION)) {
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }
}
