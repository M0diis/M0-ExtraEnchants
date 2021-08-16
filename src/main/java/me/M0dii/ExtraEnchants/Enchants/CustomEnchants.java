package me.M0dii.ExtraEnchants.Enchants;

import org.bukkit.enchantments.Enchantment;

public class CustomEnchants
{
    public static final Enchantment TELEPATHY =
            new EnchantmentWrapper("telepathy", "Telepathy", 1);
    
    public static final Enchantment PLOW =
            new EnchantmentWrapper("plow", "Plow", 1);
    
    public static final Enchantment SMELT =
            new SmeltWrapper("smelt", "Smelt", 1);
    
    public static final Enchantment BEHEADING =
            new EnchantmentWrapper("beheading", "Beheading", 1);
    
    public static final Enchantment BONDED =
            new EnchantmentWrapper("bonded", "Bonded", 1);
    
    public static final Enchantment LAVA_WALKER =
            new EnchantmentWrapper("lava_walker", "Lava Walker", 1);
    
    public static Enchantment parse(String en)
    {
        if(en.equalsIgnoreCase("telepathy"))
            return TELEPATHY;
        
        if(en.equalsIgnoreCase("smelt"))
            return SMELT;
    
        if(en.equalsIgnoreCase("plow"))
            return PLOW;
    
        if(en.equalsIgnoreCase("beheading"))
            return BEHEADING;
    
        if(en.equalsIgnoreCase("lava_walker")
        || en.equalsIgnoreCase("lavawalker"))
            return LAVA_WALKER;
        
        return null;
    }
}