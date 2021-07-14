package me.M0dii.ExtraEnchants.Listeners.Custom;

import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import me.M0dii.ExtraEnchants.Events.SmeltEvent;
import me.M0dii.ExtraEnchants.Events.TelepathyEvent;
import me.M0dii.ExtraEnchants.ExtraEnchants;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.*;

public class OnSmelt implements Listener
{
    private final ExtraEnchants plugin;
    
    public OnSmelt(ExtraEnchants plugin)
    {
        this.plugin = plugin;
    }
    
    private static final Random rnd = new Random();
    
    @EventHandler
    public void onSmelt(SmeltEvent e)
    {
        Block b = e.getBlock();
        Player p = e.getPlayer();
        ItemStack tool = p.getInventory().getItemInMainHand();
        
        Collection<ItemStack> drops = e.getDrops();
        List<ItemStack> results = new ArrayList<>();
    
        Iterator<Recipe> recipes = Bukkit.recipeIterator();
    
        boolean hasFortune = tool.getItemMeta().getEnchants()
                .containsKey(Enchantment.LOOT_BONUS_BLOCKS);
        
        int fortuneLevel = 1;
        
        if(hasFortune)
            fortuneLevel = tool.getItemMeta().getEnchants()
                .get(Enchantment.LOOT_BONUS_BLOCKS);
        
        while (recipes.hasNext() && !dontSmelt(b.getType().name()))
        {
            Recipe recipe = recipes.next();
            
            if (!(recipe instanceof FurnaceRecipe))
                continue;
            
            if (((FurnaceRecipe) recipe).getInput().getType() != b.getType())
                continue;
            
            if(hasFortune)
            {
                if(doDouble(b.getType().name()))
                {
                    int extraDrops = rnd.nextInt(fortuneLevel + 1);

                    for(int i = 0; i <= extraDrops; i++)
                        results.add(recipe.getResult());
                }
                else results.add(recipe.getResult());
            }
            else results.add(recipe.getResult());
    
            break;
        }
        
        if(results.size() == 0)
            results = new ArrayList<>(drops);
    
        boolean silk = tool.getItemMeta().getEnchants()
                .containsKey(Enchantment.SILK_TOUCH);
        
        if(silk)
            results = new ArrayList<>(drops);
        
        if(tool != null && tool.getItemMeta() != null &&
                tool.getItemMeta().hasEnchant(CustomEnchants.TELEPATHY))
        {
            Bukkit.getPluginManager().callEvent(new TelepathyEvent(p, e.breakEvent(), results));
        }
        else
        {
            for(ItemStack drop : results)
                b.getWorld().dropItemNaturally(b.getLocation(), drop);
        }
    }
    
    private boolean doDouble(String name)
    {
        return name.equalsIgnoreCase("IRON_ORE") || name.equalsIgnoreCase("GOLD_ORE") || name.equalsIgnoreCase("COAL_ORE");
    }
    
    private boolean dontSmelt(String name)
    {
        if(name.equalsIgnoreCase("REDSTONE_ORE")
        || name.equalsIgnoreCase("DIAMOND_ORE")
        || name.equalsIgnoreCase("NETHER_QUARTZ_ORE")
        || name.equalsIgnoreCase("LAPIS_ORE")
        || name.equalsIgnoreCase("NETHER_GOLD_ORE")
        || name.equalsIgnoreCase("EMERALD_ORE"))
            return true;
        
        return false;
    }
    
}
