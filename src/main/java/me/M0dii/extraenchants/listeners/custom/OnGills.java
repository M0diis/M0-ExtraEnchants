package me.m0dii.extraenchants.listeners.custom;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.events.BatVisionEvent;
import me.m0dii.extraenchants.events.GillsEvent;
import me.m0dii.extraenchants.utils.InventoryUtils;
import me.m0dii.extraenchants.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class OnGills implements Listener {
    private final ExtraEnchants plugin;

    public OnGills(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGills(final GillsEvent e) {
        if (!Utils.shouldTrigger(EEnchant.GILLS)) {
            return;
        }

        Player p = e.getPlayer();

        PotionEffect current = p.getPotionEffect(PotionEffectType.WATER_BREATHING);

        if(current != null && current.getDuration() > 1200) {
            return;
        }

        InventoryUtils.applyDurabilityChanced(p.getInventory().getHelmet(), 50);

        p.addPotionEffect(PotionEffectType.WATER_BREATHING.createEffect(3600, 0));
    }

    @EventHandler
    public void onPlayerMoveGills(final PlayerMoveEvent e) {
        Player p = e.getPlayer();

        ItemStack helmet = p.getInventory().getHelmet();

        Block block = p.getLocation().getBlock();

        if (!block.isLiquid() || !block.getType().equals(Material.WATER)) {
            p.removePotionEffect(PotionEffectType.WATER_BREATHING);
        }

        if(helmet == null) {
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

        if(e.getSlotType() != PlayerArmorChangeEvent.SlotType.HEAD) {
            return;
        }

        ItemStack previous = e.getOldItem();

        if(previous != null && previous.containsEnchantment(EEnchant.GILLS.getEnchantment())) {
            p.removePotionEffect(PotionEffectType.WATER_BREATHING);
        }
    }
}