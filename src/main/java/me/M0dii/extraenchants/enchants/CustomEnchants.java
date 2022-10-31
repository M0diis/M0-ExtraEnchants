package me.m0dii.extraenchants.enchants;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.utils.Wrapper;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CustomEnchants {

    private static final ExtraEnchants plugin = ExtraEnchants.getInstance();

    public static void register() {
        List<Enchantment> list = Arrays.stream(Enchantment.values()).toList();

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

                EEnchant eEnchant = EEnchant.parse(wrapper.name());

                if(eEnchant == null) {
                    continue;
                }

                Constructor<? extends Enchantment> constructor = clazz.getConstructor(String.class, int.class);

                Enchantment enchantment = constructor.newInstance(wrapper.name(), wrapper.maxLvl());

                registerEnchantment(enchantment, list);

                Bukkit.getLogger().info("Registering enchant wrapper: " + enchantment.getClass().getSimpleName());

                eEnchant.setEnchantment(enchantment);
            }
        }
        catch (IllegalAccessException | InstantiationException |
               NoSuchMethodException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static List<Enchantment> getAllEnchants() {
        return Arrays.stream(EEnchant.values()).map(EEnchant::getEnchantment).toList();
    }

    private static void registerEnchantment(final Enchantment enchantment, List<Enchantment> list) {
        if (list.contains(enchantment)) {
            return;
        }

        try {
            final Field f = Enchantment.class.getDeclaredField("acceptingNew");

            f.setAccessible(true);
            f.set(null, true);

            Enchantment.registerEnchantment(enchantment);
        } catch (Exception ex) {
            Bukkit.getLogger().severe("[M0-ExtraEnchants] Could not register enchantment: " + enchantment.getKey());
        }
    }
}