package me.M0dii.ExtraEnchants.Enchants;

import org.bukkit.enchantments.Enchantment;

public class CustomEnchants
{
    public static final Enchantment TELEPATHY =
            new EnchantmentWrapper("telepathy", "Telepathy", 1);
    
    public static final Enchantment PLOW =
            new EnchantmentWrapper("plow", "Plow", 1);
    
    public static final Enchantment SMELT =
            new EnchantmentWrapper("smelt", "Smelt", 1);
    
    public static Enchantment parse(String en)
    {
        if(en.equalsIgnoreCase("telepathy"))
            return TELEPATHY;
        
        if(en.equalsIgnoreCase("smelt"))
            return SMELT;
    
        if(en.equalsIgnoreCase("plow"))
            return PLOW;
        
        return null;
    }
}