package me.M0dii.ExtraEnchants.Listeners.Custom;

import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import me.M0dii.ExtraEnchants.Events.AntiThornsEvent;
import me.M0dii.ExtraEnchants.ExtraEnchants;
import me.M0dii.ExtraEnchants.Utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OnAntiThorns implements Listener {
    private static final Random random = new Random();
    public static List<Block> lavaWalkerBlocks;
    private final ExtraEnchants plugin;

    public OnAntiThorns(ExtraEnchants plugin) {
        this.plugin = plugin;

        lavaWalkerBlocks = new ArrayList<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAntiThorns(AntiThornsEvent e) {
        ItemStack helmet = e.getPlayer().getInventory().getHelmet();
        ItemStack chestplate = e.getPlayer().getInventory().getChestplate();
        ItemStack leggings = e.getPlayer().getInventory().getLeggings();
        ItemStack boots = e.getPlayer().getInventory().getBoots();

        int deflectPercentage = 0;

        if(helmet != null) {
            if(helmet.containsEnchantment(CustomEnchants.ANTI_THORNS)) {
                ItemMeta meta = helmet.getItemMeta();
                InventoryUtils.applyDurability(helmet, (Damageable) meta);

                deflectPercentage += 25;
            }
        }

        if(chestplate != null) {
            if(chestplate.containsEnchantment(CustomEnchants.ANTI_THORNS)) {
                ItemMeta meta = chestplate.getItemMeta();
                InventoryUtils.applyDurability(chestplate, (Damageable) meta);

                deflectPercentage += 25;
            }
        }

        if(leggings != null) {
            if(leggings.containsEnchantment(CustomEnchants.ANTI_THORNS)) {
                ItemMeta meta = leggings.getItemMeta();
                InventoryUtils.applyDurability(leggings, (Damageable) meta);

                deflectPercentage += 25;
            }
        }

        if(boots != null) {
            if(boots.containsEnchantment(CustomEnchants.ANTI_THORNS)) {
                ItemMeta meta = boots.getItemMeta();
                InventoryUtils.applyDurability(boots, (Damageable) meta);

                deflectPercentage += 25;
            }
        }

        if(deflectPercentage == 100) {
            e.getEntityDamageEvent().setCancelled(true);
        } else {
            if(random.nextInt(100) < deflectPercentage) {
                e.getEntityDamageEvent().setCancelled(true);
            }
        }
    }
}
