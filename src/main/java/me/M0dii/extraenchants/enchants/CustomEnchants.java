package me.m0dii.extraenchants.enchants;

import me.m0dii.extraenchants.utils.EnchantWrapper;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class CustomEnchants {

    public static void register() {
        new Reflections("me.m0dii.extraenchants.enchants.wrappers")
                .getSubTypesOf(Enchantment.class)
                .forEach(clazz -> {
                    try {
                        if (!clazz.isAnnotationPresent(EnchantWrapper.class)) {
                            return;
                        }

                        EnchantWrapper wrapper = clazz.getAnnotation(EnchantWrapper.class);

                        EEnchant eEnchant = EEnchant.parse(wrapper.name());

                        if (eEnchant == null) {
                            return;
                        }

                        Constructor<? extends Enchantment> constructor = clazz.getConstructor(String.class, int.class, EEnchant.class);

                        Enchantment enchantment = constructor.newInstance(wrapper.name(), wrapper.maxLevel(), eEnchant);

                        Bukkit.getLogger().info("Registering enchant wrapper: " + enchantment.getClass().getSimpleName());

                        eEnchant.setEnchantment(enchantment);
                    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                             InvocationTargetException ex) {
                        ex.printStackTrace();
                    }
                });
    }

    public static List<Enchantment> getAllEnchants() {
        return Arrays.stream(EEnchant.values())
                .map(EEnchant::getEnchantment)
                .toList();
    }

    public static List<EEnchant> getAllEEnchants() {
        return Arrays.stream(EEnchant.values()).toList();
    }
}