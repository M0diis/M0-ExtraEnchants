package me.M0dii.ExtraEnchants.Enchants;

import java.lang.reflect.Field;
import org.bukkit.Bukkit;

import java.util.stream.Collectors;
import java.util.Arrays;
import org.bukkit.enchantments.Enchantment;
import java.util.List;

public class RegisterEnchants
{
    public static void register()
    {
        List<Enchantment> list = Arrays.stream(Enchantment.values())
                .collect(Collectors.toList());
        
        if (!list.contains(CustomEnchants.TELEPATHY))
            registerEnchantment(CustomEnchants.TELEPATHY);
            
        if (!list.contains(CustomEnchants.PLOW))
            registerEnchantment(CustomEnchants.PLOW);
    
        if (!list.contains(CustomEnchants.SMELT))
            registerEnchantment(CustomEnchants.SMELT);
    
        if (!list.contains(CustomEnchants.BEHEADING))
            registerEnchantment(CustomEnchants.BEHEADING);
    }
    
    private static void registerEnchantment(final Enchantment enchantment)
    {
        try
        {
            final Field f = Enchantment.class.getDeclaredField("acceptingNew");
            
            f.setAccessible(true);
            f.set(null, true);
            
            Enchantment.registerEnchantment(enchantment);
        }
        catch (Exception e)
        {
            Bukkit.getLogger().severe("[AdvancedEnchantments] Could not register enchantment: " + enchantment.getKey());
        }
    }
}
