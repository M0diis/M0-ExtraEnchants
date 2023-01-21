package me.m0dii.extraenchants;

import me.m0dii.extraenchants.commands.DisenchantCommand;
import me.m0dii.extraenchants.commands.EnchantCommand;
import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.utils.data.ConfigManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.MultiLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ExtraEnchants extends JavaPlugin {
    private static ExtraEnchants instance;
    private PluginManager pm;
    private ConfigManager configManager;

    public static ExtraEnchants getInstance() {
        return instance;
    }

    public FileConfiguration getCfg() {
        return this.configManager.getConfig();
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);

        this.pm = this.getServer().getPluginManager();

        getLogger().info("EnhancedEnchantments has been enabled.");

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        registerEvents();
        registerCommands();

        setupMetrics();
        checkForUpdates();

        CustomEnchants.register();
    }

    private void checkForUpdates() {
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
                    }
                    catch (IllegalAccessException | InstantiationException |
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