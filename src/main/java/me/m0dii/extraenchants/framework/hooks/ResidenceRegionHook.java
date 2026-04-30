package me.m0dii.extraenchants.framework.hooks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class ResidenceRegionHook implements RegionHook {
    @Override
    public boolean isAllowed(Player player, Location location) {
        Object residence = getResidenceAt(location);
        if (residence == null || player == null) {
            return true;
        }

        try {
            Object permissions = residence.getClass().getMethod("getPermissions").invoke(residence);

            Method withBool = findMethod(permissions.getClass(), "playerHas", Player.class, String.class, boolean.class);
            if (withBool != null) {
                boolean use = (boolean) withBool.invoke(permissions, player, "use", false);
                boolean build = (boolean) withBool.invoke(permissions, player, "build", false);
                return use || build;
            }

            Method noBool = findMethod(permissions.getClass(), "playerHas", Player.class, String.class);
            if (noBool != null) {
                boolean use = (boolean) noBool.invoke(permissions, player, "use");
                boolean build = (boolean) noBool.invoke(permissions, player, "build");
                return use || build;
            }
        } catch (Exception ignored) {
            return true;
        }

        return true;
    }

    @Override
    public String getRegionName(Location location) {
        Object residence = getResidenceAt(location);
        if (residence == null) {
            return "";
        }

        try {
            return String.valueOf(residence.getClass().getMethod("getName").invoke(residence));
        } catch (Exception ignored) {
            return "";
        }
    }

    private Object getResidenceAt(Location location) {
        if (location == null) {
            return null;
        }

        try {
            Class<?> residenceClass = Class.forName("com.bekvon.bukkit.residence.Residence");
            Object residenceApi = residenceClass.getMethod("getInstance").invoke(null);
            Object manager = residenceApi.getClass().getMethod("getResidenceManager").invoke(residenceApi);
            return manager.getClass().getMethod("getByLoc", Location.class).invoke(manager, location);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        try {
            return type.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}

