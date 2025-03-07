package me.m0dii.extraenchants.enchants;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import me.m0dii.extraenchants.ExtraEnchants;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.intellij.lang.annotations.Subst;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class CustomEnchants {
    public static void register(ExtraEnchants plugin) {
        new Reflections("me.m0dii.extraenchants.enchants.wrappers")
                .getSubTypesOf(CustomEnchantment.class)
                .forEach(clazz -> {
                    try {
                        if (!clazz.isAnnotationPresent(EnchantWrapper.class)) {
                            return;
                        }

                        @Subst("") EnchantWrapper wrapper = clazz.getAnnotation(EnchantWrapper.class);

                        EEnchant eEnchant = EEnchant.parse(wrapper.name());

                        if (eEnchant == null) {
                            return;
                        }

                        Constructor<? extends CustomEnchantment> constructor = clazz.getConstructor(String.class, int.class, EEnchant.class);

                        CustomEnchantment enchantment = constructor.newInstance(wrapper.name(), wrapper.maxLevel(), eEnchant);

                        Registry<Enchantment> enchantmentRegistry = RegistryAccess
                                .registryAccess()
                                .getRegistry(RegistryKey.ENCHANTMENT);

                        String name = wrapper.name();

                        Enchantment enchantmentB = enchantmentRegistry.getOrThrow(TypedKey.create(
                                RegistryKey.ENCHANTMENT, Key.key("custom:" + name.toLowerCase().replace(" ", "_")))
                        );

                        eEnchant.setEnchantment(enchantmentB);
                        eEnchant.setCustomEnchantment(enchantment);

                        plugin.getLogger().info("Registered enchant wrapper: " + enchantment.getClass().getSimpleName());
                        plugin.getServer().getPluginManager().registerEvents(enchantment, plugin);
                        plugin.getLogger().info("Registered listener for enchant: " + enchantment.getName());
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