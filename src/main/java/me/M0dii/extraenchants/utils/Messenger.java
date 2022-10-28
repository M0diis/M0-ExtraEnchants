package me.m0dii.extraenchants.utils;

import me.m0dii.extraenchants.ExtraEnchants;

public class Messenger {
    private static final ExtraEnchants plugin = ExtraEnchants.getInstance();

    public static void debug(String msg) {
        if (plugin.getCfg().getBoolean("debug")) {
            plugin.getLogger().info("[DEBUG] " + msg);
        }
    }
}
