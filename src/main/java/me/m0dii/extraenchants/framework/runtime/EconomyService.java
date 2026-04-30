package me.m0dii.extraenchants.framework.runtime;

import me.m0dii.extraenchants.ExtraEnchants;
import org.bukkit.entity.Player;

public class EconomyService {
    private final ExtraEnchants plugin;

    public EconomyService(ExtraEnchants plugin) {
        this.plugin = plugin;
    }

    public boolean has(Player player, double amount) {
        return plugin.getEconomy() != null && player != null && plugin.getEconomy().has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (plugin.getEconomy() == null || player == null) {
            return false;
        }

        return plugin.getEconomy().withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(Player player, double amount) {
        if (plugin.getEconomy() == null || player == null) {
            return false;
        }

        return plugin.getEconomy().depositPlayer(player, amount).transactionSuccess();
    }

    public double balance(Player player) {
        if (plugin.getEconomy() == null || player == null) {
            return 0D;
        }

        return plugin.getEconomy().getBalance(player);
    }
}

