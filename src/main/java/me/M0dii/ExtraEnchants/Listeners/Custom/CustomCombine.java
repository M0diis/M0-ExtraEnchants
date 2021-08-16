package me.M0dii.ExtraEnchants.Listeners.Custom;

import me.M0dii.ExtraEnchants.ExtraEnchants;
import me.M0dii.ExtraEnchants.Enchants.CustomEnchants;
import me.M0dii.ExtraEnchants.Events.CombineEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomCombine implements org.bukkit.event.Listener
{
    private final ExtraEnchants plugin;
    
    public CustomCombine(ExtraEnchants plugin)
    {
        this.plugin = plugin;
    }
    
    private final List<String> tools = Arrays.asList(
            "NETHERITE_AXE", "NETHERITE_SHOVEL", "NETHERITE_PICKAXE", "DIAMOND_AXE",
            "NETHERITE_HOE", "DIAMOND_SHOVEL", "DIAMOND_PICKAXE", "IRON_AXE", "IRON_SHOVEL",
            "IRON_PICKAXE", "GOLDEN_AXE", "GOLDEN_SHOVEL", "GOLDEN_PICKAXE", "STONE_AXE",
            "STONE_SHOVEL", "STONE_PICKAXE", "WOODEN_AXE", "WOODEN_SHOVEL", "WOODEN_PICKAXE",
            "DIAMOND_HOE", "IRON_HOE", "GOLDEN_HOE", "STONE_HOE", "WOODEN_HOE"
    );
    
    private final List<String> boots = Arrays.asList(
            "NETHERITE_BOOTS", "DIAMOND_BOOTS", "GOLD_BOOTS",
            "IRON_BOOTS", "LEATHER_BOOTS");
    
    private final List<String> hoes = Arrays.asList(
            "NETHERITE_HOE", "DIAMOND_HOE", "IRON_HOE",
            "GOLDEN_HOE", "STONE_HOE", "WOODEN_HOE");
    
    @EventHandler
    public void onCustomCombine(CombineEvent event)
    {
        ItemStack curr = event.clickEvent().getCurrentItem();
        
        if(curr == null)
            return;
    
        String enchName = event.getEnchantString();
        ItemMeta meta = curr.getItemMeta();
        
        if(enchName.equalsIgnoreCase("bonded"))
        {
            if((curr.hasItemMeta()) && (meta.hasEnchant(CustomEnchants.BONDED)))
                return;
        
            event.clickEvent().setCancelled(true);
        
            ItemStack tool = new ItemStack(curr.getType());
            List<String> lore = new ArrayList<>();
        
            lore.add(ChatColor.GRAY + "Bonded I");
        
            if(combine(event, curr, CustomEnchants.BONDED, tool, lore))
                return;
        }
    
        if(enchName.equalsIgnoreCase("lava_walker")
        || enchName.equalsIgnoreCase("lavawalker"))
        {
            if(!boots.contains(curr.getType().toString()))
                return;
        
            if((curr.hasItemMeta()) && (meta.hasEnchant(CustomEnchants.LAVA_WALKER)))
                return;
        
            event.clickEvent().setCancelled(true);
        
            ItemStack tool = new ItemStack(curr.getType());
        
            List<String> lore = new ArrayList<>();
        
            lore.add(ChatColor.GRAY + "Lava Walker I");
        
            if(combine(event, curr, CustomEnchants.LAVA_WALKER, tool, lore))
                return;
        }
    
        if(enchName.equalsIgnoreCase("smelt"))
        {
            if(!tools.contains(curr.getType().toString()))
                return;
        
            if((curr.hasItemMeta()) && (meta.hasEnchant(CustomEnchants.SMELT)))
                return;
        
            event.clickEvent().setCancelled(true);
        
            ItemStack tool = new ItemStack(curr.getType());
        
            List<String> lore = new ArrayList<>();
        
            lore.add(ChatColor.GRAY + "Smelt I");
        
            if(combine(event, curr, CustomEnchants.SMELT, tool, lore))
                return;
        }
        
        if(enchName.equalsIgnoreCase("telepathy"))
        {
            if(!tools.contains(curr.getType().toString()))
                return;
            
            if((curr.hasItemMeta()) && (meta.hasEnchant(CustomEnchants.TELEPATHY)))
                return;

            event.clickEvent().setCancelled(true);
            
            ItemStack tool = new ItemStack(curr.getType());
            
            List<String> lore = new ArrayList<>();
            
            lore.add(ChatColor.GRAY + "Telepathy I");
    
            if(combine(event, curr, CustomEnchants.TELEPATHY, tool, lore))
                return;
        }
    
        if(enchName.equalsIgnoreCase("plow"))
        {
            if(!hoes.contains(curr.getType().toString()))
                return;
            
            if((curr.hasItemMeta()) && (meta.hasEnchant(CustomEnchants.PLOW)))
                return;
            
            event.clickEvent().setCancelled(true);
            
            ItemStack tool = new ItemStack(curr.getType());
            List<String> lore = new ArrayList<>();
            
            lore.add(ChatColor.GRAY + "Plow I");
            
            if(combine(event, curr, CustomEnchants.PLOW, tool, lore))
                return;
        }
        
        event.clickEvent().setCursor(null);
    }
    
    private boolean combine(CombineEvent event, ItemStack curr,
                            Enchantment ench, ItemStack tool, List<String> lore)
    {
        ItemMeta meta = curr.getItemMeta();
        
        if(meta.getLore() != null)
            for(String l : meta.getLore())
                lore.add(this.plugin.format(l));
        
        meta.setLore(lore);
        tool.setItemMeta(meta);
        
        tool.addUnsafeEnchantment(ench, 1);
        
        if(curr.getAmount() > 1)
        {
            curr.setAmount(curr.getAmount() - 1);
            event.clickEvent().setCursor(tool);
    
            return true;
        }
        
        event.clickEvent().setCurrentItem(tool);
        
        return false;
    }
}
