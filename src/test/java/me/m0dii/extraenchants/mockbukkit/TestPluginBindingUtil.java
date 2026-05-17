package me.m0dii.extraenchants.mockbukkit;

import me.m0dii.extraenchants.ExtraEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;

import java.lang.reflect.Field;

final class TestPluginBindingUtil {

    private TestPluginBindingUtil() {
    }

    static void bindPlugin(ExtraEnchants plugin) throws Exception {
        Field instanceField = ExtraEnchants.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, plugin);

        // EEnchant caches plugin instance at enum-init time; keep it in sync for mixed test order.
        Field enchantInstanceField = EEnchant.class.getDeclaredField("instance");
        enchantInstanceField.setAccessible(true);
        for (EEnchant enchant : EEnchant.values()) {
            enchantInstanceField.set(enchant, plugin);
        }
    }
}

