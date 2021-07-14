package me.M0dii.ExtraEnchants.Enchants;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

public class EnchantmentWrapper extends Enchantment
{
    private final String name;
    private final int maxLvl;
    
    public EnchantmentWrapper(final String namespace, final String name, final int lvl)
    {
        super(NamespacedKey.minecraft(namespace));
        this.name = name;
        this.maxLvl = lvl;
    }
    
    public boolean canEnchantItem(final ItemStack item)
    {
        return true;
    }
    
    public boolean conflictsWith(final Enchantment arg0)
    {
        return false;
    }
    
    public EnchantmentTarget getItemTarget()
    {
        return null;
    }
    
    public int getMaxLevel()
    {
        return this.maxLvl;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public int getStartLevel()
    {
        return 0;
    }
    
    public boolean isCursed()
    {
        return false;
    }
    
    public boolean isTreasure()
    {
        return false;
    }
}
