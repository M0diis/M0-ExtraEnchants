package me.m0dii.extraenchants.framework.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface RegionHook {
    boolean isAllowed(Player player, Location location);

    String getRegionName(Location location);
}

