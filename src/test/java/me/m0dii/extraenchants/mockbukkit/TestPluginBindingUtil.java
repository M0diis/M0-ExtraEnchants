package me.m0dii.extraenchants.mockbukkit;

import me.m0dii.extraenchants.ExtraEnchants;
import java.lang.reflect.Field;

final class TestPluginBindingUtil {

    private TestPluginBindingUtil() {
    }

    static void bindPlugin(ExtraEnchants plugin) throws Exception {
        Field instanceField = ExtraEnchants.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, plugin);

    }
}

