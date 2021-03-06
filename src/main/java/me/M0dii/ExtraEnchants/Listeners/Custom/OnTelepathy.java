package me.M0dii.ExtraEnchants.Listeners.Custom;

import me.M0dii.ExtraEnchants.ExtraEnchants;
import me.M0dii.ExtraEnchants.Events.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class OnTelepathy implements Listener
{
    private static final Random r = new Random();
    
    private final ExtraEnchants plugin;
    
    public OnTelepathy(ExtraEnchants plugin)
    {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onTelepathy(TelepathyEvent e)
    {
        Player player = e.getPlayer();
        Block b = e.getBlock();
        PlayerInventory inv = player.getInventory();
        ItemStack hand = inv.getItemInMainHand();
        Damageable itemDam = (Damageable)hand.getItemMeta();
        
        Collection<ItemStack> drops = e.getDrops();
        
        boolean hasSilk = hand.getItemMeta()
                .getEnchants().containsKey(Enchantment.SILK_TOUCH);
    
        boolean fits = doesFit(inv, drops);
    
        if(b.getType().equals(Material.SPAWNER)
        || b.getType().name().toUpperCase().contains("SPAWNER"))
            return;
    
        if(!fits)
        {
            for(ItemStack i : drops)
                b.getWorld().dropItemNaturally(
                        b.getLocation(), i);
    
            applyDurability(hand, itemDam);
            
            return;
        }
        
        if(hasSilk)
        {
            ItemStack itm = new ItemStack(b.getType());
            
            String name = itm.getType().name();
            
            if(name.contains("WALL_") || name.contains("BANNER"))
            {
                if(name.contains("BANNER"))
                {
                    for(ItemStack i : drops)
                        inv.addItem(i);
                }
                else
                {
                    Material m = Material.getMaterial(name.replace("WALL_", ""));
    
                    if(m != null)
                        inv.addItem(new ItemStack(m));
                }
            }
            else
                inv.addItem(itm);
        }
        else if(inv.firstEmpty() == -1)
        {
            ItemStack item = drops.iterator().next();
            
            if(inv.contains(item))
                addToStack(player, drops);
        }
        else
        {
            for(ItemStack i : drops)
                inv.addItem(i);
    
            if(hand.getType().getMaxDurability() <= itemDam.getDamage())
            {
                inv.removeItem(hand);
        
                return;
            }
        }
    
        applyDurability(hand, itemDam);
    }
    
    public boolean doesFit(Inventory inv, Collection<ItemStack> drops)
    {
        for (ItemStack i : inv.getStorageContents())
            if(i == null)
                return true;
    
        return hasSpaceForItem(drops, inv);
    }
    
    private void applyDurability(ItemStack hand, Damageable itemDam)
    {
        boolean contains = hand.getItemMeta().getEnchants().containsKey(Enchantment.DURABILITY);
        
        int unb = 0;
        
        if(contains)
            unb = hand.getItemMeta().getEnchants().get(Enchantment.DURABILITY);
        
        int chance = (100)/(1 + unb);
        
        int res = r.nextInt(100 - 1) + 1;
        
        if(res < chance)
            itemDam.setDamage(itemDam.getDamage() + 1);
        
        hand.setItemMeta((ItemMeta)itemDam);
    }
    
    private void addToStack(Player p, Collection<ItemStack> drops)
    {
        ItemStack item = drops.iterator().next();
        ItemStack[] arrayOfItemStack;
        
        int j = (arrayOfItemStack = p.getInventory().getContents()).length;
        
        for(int i = 0; i < j; i++)
        {
            ItemStack it = arrayOfItemStack[i];
            
            if((it.equals(item)) && (it.getAmount() < 64))
            {
                for(ItemStack itm : drops)
                {
                    p.getInventory().addItem(itm);
                }
                
                break;
            }
        }
    }
    
    private boolean hasSpaceForItem(Collection<ItemStack> drops, Inventory inv)
    {
        Iterator<ItemStack> iter = drops.iterator();
        
        if(iter != null && iter.hasNext())
        {
            ItemStack item = iter.next();
    
            for(ItemStack it : inv.getStorageContents())
            {
                if(it != null && it.equals(item) && (it.getAmount() < 64))
                    return true;
            }
            
        }
        
        return false;
    }
}