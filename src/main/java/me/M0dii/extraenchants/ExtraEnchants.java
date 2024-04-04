package me.m0dii.extraenchants;

import me.m0dii.extraenchants.commands.DisenchantCommand;
import me.m0dii.extraenchants.commands.EnchantCommand;
import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.utils.Placeholders;
import me.m0dii.extraenchants.utils.Utils;
import me.m0dii.extraenchants.utils.data.ConfigManager;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.MultiLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ExtraEnchants extends JavaPlugin {
    private static ExtraEnchants instance;
    private PluginManager pm;
    private ConfigManager configManager;
    private Economy economy = null;

    public static ExtraEnchants getInstance() {
        return instance;
    }

    public FileConfiguration getCfg() {
        return this.configManager.getConfig();
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    private Placeholders placeholders;

    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);

        this.pm = this.getServer().getPluginManager();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.placeholders = new Placeholders();
            this.placeholders.register();
        }

        getLogger().info("EnhancedEnchantments has been enabled.");

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        registerEvents();
        registerCommands();

        setupMetrics();
        checkForUpdates();

        Utils.copy(getResource("config.yml"), new File(getDataFolder(), "config_default.yml"));

        if (!setupEconomy()) {
            getLogger().severe("Vault not found, disabling economy features.");
        }

        CustomEnchants.register();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();

        return economy != null;
    }

    private void checkForUpdates() {
        if (!configManager.getConfig().getBoolean("notify-update")) {
            return;
        }

        new UpdateChecker(this, 88737).getVersion(ver ->
        {
            String curr = this.getDescription().getVersion();

            if (!curr.equalsIgnoreCase(
                    ver.replace("v", ""))) {
                getLogger().info("You are running an outdated version of M0-ExtraEnchants.");
                getLogger().info("Latest version: " + ver + ", you are using: " + curr);
                getLogger().info("You can download the latest version on Spigot:");
                getLogger().info("https://www.spigotmc.org/resources/88737/");
            }
        });
    }

    private void setupMetrics() {
        Metrics metrics = new Metrics(this, 12049);

        CustomChart c = new MultiLineChart("players_and_servers", () ->
        {
            Map<String, Integer> valueMap = new HashMap<>();

            valueMap.put("servers", 1);
            valueMap.put("players", Bukkit.getOnlinePlayers().size());

            return valueMap;
        });

        metrics.addCustomChart(c);
    }

    public Economy getEconomy() {
        return economy;
    }

    public void onDisable() {
        this.getLogger().info("EnhancedEnchantments has been disabled.");
    }

    private void registerEvents() {
        new Reflections("me.m0dii.extraenchants.listeners")
                .getSubTypesOf(Listener.class)
                .forEach(clazz -> {
                    try {
                        Constructor<? extends Listener> constructor = clazz.getConstructor(ExtraEnchants.class);

                        Listener listener = constructor.newInstance(this);

                        this.pm.registerEvents(listener, this);

                        Bukkit.getLogger().info("Registered listener: " + listener.getClass().getSimpleName());
                    } catch (IllegalAccessException | InstantiationException |
                             NoSuchMethodException | InvocationTargetException ex) {
                        ex.printStackTrace();
                    }
                });
    }

    private void registerCommands() {
        PluginCommand ee = getCommand("extraenchant");

        if (ee != null) {
            ee.setExecutor(new EnchantCommand(this));
        }

        PluginCommand ue = getCommand("unenchant");

        if (ue != null) {
            ue.setExecutor(new DisenchantCommand(this));
        }
    }
}