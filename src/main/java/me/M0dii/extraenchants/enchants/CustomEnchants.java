package me.m0dii.extraenchants.enchants;

import me.m0dii.extraenchants.utils.Wrapper;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CustomEnchants {
    public static final Map<EEnchant, Enchantment> ENCHANTMENTS = new HashMap<>();

    public static void register() {
        ENCHANTMENTS.clear();

        Reflections reflections = new Reflections("me.m0dii.extraenchants.enchants.wrappers");

        Set<Class<? extends Enchantment>> classes = reflections.getSubTypesOf(Enchantment.class);
        Iterator<Class<? extends Enchantment>> it = classes.iterator();

        try {
            while (it.hasNext()) {
                Class<? extends Enchantment> clazz = it.next();

                if (!clazz.isAnnotationPresent(Wrapper.class)) {
                    continue;
                }

                Wrapper wrapper = clazz.getAnnotation(Wrapper.class);

                EEnchant eEnchant = EEnchant.get(wrapper.name());

                if(eEnchant == null) {
                    continue;
                }

                Constructor<? extends Enchantment> constructor = clazz.getConstructor(String.class, int.class);

                Enchantment enchantment = constructor.newInstance(wrapper.name(), wrapper.maxLvl());

                Bukkit.getLogger().info("Registering enchant wrapper: " + enchantment.getClass().getSimpleName());

                eEnchant.setEnchant(enchantment);

                ENCHANTMENTS.put(eEnchant, enchantment);
            }
        }
        catch (IllegalAccessException | InstantiationException |
               NoSuchMethodException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static Enchantment parse(EEnchant key) {
        return ENCHANTMENTS.getOrDefault(key, null);
    }

    public static List<Enchantment> getAllEnchants() {
        return ENCHANTMENTS.values().stream().toList();
    }
}