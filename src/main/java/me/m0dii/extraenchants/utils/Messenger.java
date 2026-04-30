package me.m0dii.extraenchants.utils;

import me.m0dii.extraenchants.ExtraEnchants;

public class Messenger {
    public static void debug(String msg) {
        ExtraEnchants plugin = ExtraEnchants.getInstance();
        if (plugin == null) {
            return;
        }

        plugin.debug(msg);
    }
}
