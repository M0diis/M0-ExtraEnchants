package me.m0dii.extraenchants.framework.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NoopRegionHook implements RegionHook {
    @Override
    public boolean isAllowed(Player player, Location location) {
        return true;
    }

    @Override
    public String getRegionName(Location location) {
        return "";
    }
}

