package me.M0dii.ExtraEnchants.Listeners;

import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import me.M0dii.ExtraEnchants.Events.SmeltEvent;
import me.M0dii.ExtraEnchants.Events.TelepathyEvent;
import me.M0dii.ExtraEnchants.ExtraEnchants;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BlockBreak implements Listener
{
    private final List<String> heads = Arrays.asList("PLAYER_HEAD", "SKELETON_SKULL", "CREEPER_HEAD", "WITHER_SKELETON_SKULL",
            "ZOMBIE_HEAD", "CREEPER_WALL_HEAD", "PLAYER_WALL_HEAD", "DRAGON_HEAD", "DRAGON_WALL_HEAD", "ZOMBIE_WALL_HEAD",
            "SKELETON_WALL_SKULL", "WITHER_SKELETON_WALL_SKULL");
    
    private final List<String> hoes = Arrays.asList(
            "NETHERITE_HOE", "DIAMOND_HOE", "IRON_HOE",
            "GOLDEN_HOE", "STONE_HOE", "WOODEN_HOE");
    
    private final ExtraEnchants plugin;
    
    public BlockBreak(ExtraEnchants plugin)
    {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e)
    {
        if(!plugin.getCfg().getBoolean("enchants.telepathy.enabled"))
            return;
        
        if(e.isCancelled())
            return;
    
        Player p = e.getPlayer();
        Block b = e.getBlock();
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
    
        if(b.getType().equals(Material.SPAWNER) ||
            b.getType().name().toUpperCase().contains("SPAWNER"))
        return;
        
        ItemStack offhand = e.getPlayer().getInventory().getItemInOffHand();
        
        if(offhand != null)
        {
            if(offhand.hasItemMeta() &&
                    offhand.getItemMeta().hasEnchant(CustomEnchants.TELEPATHY))
                return;
        }
        
        if(hand == null)
            return;
        
        if(hand.getItemMeta() == null)
            return;
    
        if(!hand.getItemMeta().hasEnchant(CustomEnchants.SMELT)
        && !hand.getItemMeta().hasEnchant(CustomEnchants.TELEPATHY)
        && !hand.getItemMeta().hasEnchant(CustomEnchants.PLOW))
            return;
        
        if((p.getGameMode() == GameMode.CREATIVE)
        || (p.getGameMode() == GameMode.SPECTATOR))
            return;
    
        if((e.getBlock().getState() instanceof Container))
            return;
    
        if(this.heads.contains(b.getType().toString()))
            return;
    
        Collection<ItemStack> drops = b.getDrops(hand);
    
        e.setDropItems(false);
        
        if(hand.getItemMeta().hasEnchant(CustomEnchants.SMELT))
        {
            Bukkit.getPluginManager().callEvent(new SmeltEvent(p, e, drops));
        }
        else if(!hand.getItemMeta().hasEnchant(CustomEnchants.SMELT) &&
                hand.getItemMeta().hasEnchant(CustomEnchants.TELEPATHY))
        {
            if(hoes.contains(hand.getType().toString()))
            {
                for(ItemStack drop : drops)
                    p.getInventory().addItem(drop);
        
                return;
            }
    
            if(b.getType().name().contains("BED"))
            {
                p.getInventory().addItem(new ItemStack(b.getType()));
        
                return;
            }
    
            if(drops.isEmpty())
                return;
            
            Bukkit.getPluginManager().callEvent(new TelepathyEvent(p, e, drops));
        }
    }
}
