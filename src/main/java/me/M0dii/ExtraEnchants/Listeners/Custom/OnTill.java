package me.M0dii.ExtraEnchants.Listeners.Custom;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import me.M0dii.ExtraEnchants.Events.PlowEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class OnTill implements Listener
{
    private final Residence res;
    private final ResidenceManager rm;
    
    private static final List<String> SEEDS = Arrays.asList(
            "CARROT",
            "POTATO",
            "WHEAT_SEEDS",
            "PUMPKIN_SEEDS",
            "BEETROOT_SEEDS",
            "MELON_SEEDS"
    );
    
    private final Random r = new Random();
    
    public OnTill()
    {
        this.res = Residence.getInstance();
        this.rm = res.getResidenceManager();
    }
    
    @EventHandler
    public void onCustomTill(final PlowEvent event)
    {
        final Player p = event.getPlayer();
        final Block block = event.breakEvent().getClickedBlock();
        String type = "";
        
        ItemStack hand = p.getInventory().getItemInMainHand();
    
        Damageable itemDam = (Damageable)hand.getItemMeta();
    
        if(hand.getType().getMaxDurability() <= itemDam.getDamage())
        {
            p.getInventory().removeItem(hand);
        
            return;
        }
    
        boolean contains = hand.getItemMeta().getEnchants().containsKey(Enchantment.DURABILITY);
    
        int unb = 0;
    
        if(contains)
            unb = hand.getItemMeta().getEnchants().get(Enchantment.DURABILITY);
    
        int chance = (100)/(1 + unb);
    
        int res = r.nextInt(100 - 1) + 1;
    
        if(res < chance)
            itemDam.setDamage(itemDam.getDamage() + 1);
    
        hand.setItemMeta((ItemMeta)itemDam);
        
        if (block.getType().equals(Material.DIRT)
        || block.getType().equals(Material.GRASS_BLOCK))
        {
            final Block p1 = block.getLocation().add(0.0, 1.0, 0.0).getBlock();
            Block p2;
            Block b2;
            Block bl;
            Block p3;
            
            if (p.getFacing() == BlockFace.NORTH)
            {
                p2 = block.getLocation().add(-1.0, 1.0, 0.0).getBlock();
                b2 = block.getLocation().add(-1.0, 0.0, 0.0).getBlock();
                bl = block.getLocation().add(-2.0, 0.0, 0.0).getBlock();
                p3 = block.getLocation().add(-2.0, 1.0, 0.0).getBlock();
            }
            else if (p.getFacing() == BlockFace.SOUTH)
            {
                p2 = block.getLocation().add(1.0, 1.0, 0.0).getBlock();
                b2 = block.getLocation().add(1.0, 0.0, 0.0).getBlock();
                bl = block.getLocation().add(2.0, 0.0, 0.0).getBlock();
                p3 = block.getLocation().add(2.0, 1.0, 0.0).getBlock();
            }
            else if (p.getFacing() == BlockFace.EAST)
            {
                p2 = block.getLocation().add(0.0, 1.0, -1.0).getBlock();
                b2 = block.getLocation().add(0.0, 0.0, -1.0).getBlock();
                bl = block.getLocation().add(0.0, 0.0, -2.0).getBlock();
                p3 = block.getLocation().add(0.0, 1.0, -2.0).getBlock();
            }
            else
            {
                p2 = block.getLocation().add(0.0, 1.0, 1.0).getBlock();
                b2 = block.getLocation().add(0.0, 0.0, 1.0).getBlock();
                bl = block.getLocation().add(0.0, 0.0, 2.0).getBlock();
                p3 = block.getLocation().add(0.0, 1.0, 2.0).getBlock();
            }
            
            if (!p1.getType().equals(Material.AIR))
                return;
            
            int num = 0;
            int amount = 0;
            ItemStack[] contents;
    
            int length = (contents = p.getInventory().getContents()).length;
            
            for (int j = 0; j < length; ++j)
            {
                final ItemStack i = contents[j];
                
                if (i != null && OnTill.SEEDS.contains(i.getType().toString()))
                {
                    type = i.getType().toString();
                    amount = i.getAmount();
                    
                    break;
                }
                
                if (++num > 8)
                    return;
            }
            
            final ItemStack fee = new ItemStack(Material.matchMaterial(type));
            --amount;

            fee.setAmount(1);
            p.getInventory().removeItem(fee);
            p.updateInventory();
            block.setType(Material.FARMLAND);
            
            switch (type)
            {
                case "BEETROOT_SEEDS":
                {
                    setSeeds(p, p1, p2, b2, bl, p3, amount, fee, Material.BEETROOTS);
                    break;
                }
                
                case "POTATO":
                {
                    setSeeds(p, p1, p2, b2, bl, p3, amount, fee, Material.POTATOES);
    
                    break;
                }
                
                case "WHEAT_SEEDS":
                {
                    setSeeds(p, p1, p2, b2, bl, p3, amount, fee, Material.WHEAT);
    
                    break;
                }
                
                case "PUMPKIN_SEEDS":
                {
                    setSeeds(p, p1, p2, b2, bl, p3, amount, fee, Material.PUMPKIN_STEM);
    
                    break;
                }
                
                case "CARROT": {
                    setSeeds(p, p1, p2, b2, bl, p3, amount, fee, Material.CARROTS);
    
                    break;
                }
                
                case "MELON_SEEDS":
                {
                    setSeeds(p, p1, p2, b2, bl, p3, amount, fee, Material.MELON_STEM);
                    break;
                }
                
                default:
                    break;
            }
        }
    }
    
    private void setSeeds(Player p, Block p1, Block p2, Block b2, Block bl, Block p3,
                          int amount, ItemStack fee, Material seed)
    {
    
        ClaimedResidence res1 = rm.getByLoc(p1.getLocation());
        ClaimedResidence res2 = rm.getByLoc(p2.getLocation());
        ClaimedResidence res3 = rm.getByLoc(p3.getLocation());
        
        if(res1 != null)
        {
            ResidencePermissions res1perms = res1.getPermissions();

            if(res1perms.playerHas(p, "build", true) || res1.isOwner(p))
                p1.setType(seed);
        }
        else p1.setType(seed);
            
        if(res2 != null)
        {
            ResidencePermissions res2perms = res2.getPermissions();
            
            if(res2.isOwner(p) || res2perms.playerHas(p, "build", true))
                if(p2.getType().equals(Material.AIR) && amount >= 1 &&
                        (b2.getType().equals(Material.DIRT) ||
                                b2.getType().equals(Material.GRASS_BLOCK)))
                {
                    b2.setType(Material.FARMLAND);
                    p2.setType(seed);
                    p.getInventory().removeItem(fee);
                    --amount;
                }
        }
        else
        {
            if(p2.getType().equals(Material.AIR) && amount >= 1
                    && (b2.getType().equals(Material.DIRT)
                    || b2.getType().equals(Material.GRASS_BLOCK)))
            {
                b2.setType(Material.FARMLAND);
                p2.setType(seed);
                p.getInventory().removeItem(fee);
                --amount;
            }
        }
       
        if(res3 != null)
        {
            ResidencePermissions res3perms = res3.getPermissions();
    
            if(res3.isOwner(p) || res3perms.playerHas(p, "build", true))
                if(p3.getType().equals(Material.AIR) && amount >= 1
                        && (bl.getType().equals(Material.DIRT)
                        || bl.getType().equals(Material.GRASS_BLOCK)))
                {
                    bl.setType(Material.FARMLAND);
                    p3.setType(seed);
                    p.getInventory().removeItem(fee);
                    --amount;
                }
        }
        else
        {
            if(p3.getType().equals(Material.AIR) && amount >= 1
                    && (bl.getType().equals(Material.DIRT)
                    || bl.getType().equals(Material.GRASS_BLOCK)))
            {
                bl.setType(Material.FARMLAND);
                p3.setType(seed);
                p.getInventory().removeItem(fee);
                --amount;
            }
        }
    }
}