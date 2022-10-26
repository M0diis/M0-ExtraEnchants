package me.m0dii.extraenchants;

import me.m0dii.extraenchants.commands.DisenchantCommand;
import me.m0dii.extraenchants.commands.EnchantCommand;
import me.m0dii.extraenchants.enchants.CustomEnchants;
import me.m0dii.extraenchants.enchants.EEnchant;
import me.m0dii.extraenchants.listeners.*;
import me.m0dii.extraenchants.listeners.custom.*;
import me.m0dii.extraenchants.utils.data.ConfigManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.MultiLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ExtraEnchants extends JavaPlugin {
    public static ExtraEnchants instance;
    private PluginManager pm;
    private ConfigManager configManager;
    private boolean residence = false;

    public FileConfiguration getCfg() {
        return this.configManager.getConfig();
    }

    public boolean hasResidence() {
        return residence;
    }

    public void onEnable() {
        CustomEnchants.register();

        instance = this;

        this.configManager = new ConfigManager(this);

        this.pm = this.getServer().getPluginManager();

        this.getLogger().info("EnhancedEnchantments has been enabled.");

        Plugin residence = this.pm.getPlugin("Residence");

        if (residence != null && residence.isEnabled()) {
            this.residence = true;
        }

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
        this.pm.registerEvents(new PlayerDeathRespawn(), this);
        this.pm.registerEvents(new PlayerInteract(this), this);
        this.pm.registerEvents(new BlockBreak(this), this);
        this.pm.registerEvents(new EnchantmentCombine(), this);
        this.pm.registerEvents(new TillInteract(this), this);
        this.pm.registerEvents(new ItemDrop(this), this);
        this.pm.registerEvents(new PlayerDamage(this), this);

        Reflections reflections = new Reflections("me.m0dii.extraenchants.listeners.custom");

        Set<Class<? extends Listener>> classes = reflections.getSubTypesOf(Listener.class);
        Iterator<Class<? extends Listener>> it = classes.iterator();

        try {
            while (it.hasNext()) {
                Class<? extends Listener> clazz = it.next();

                Constructor<? extends Listener> constructor = clazz.getConstructor(ExtraEnchants.class);

                Listener listener = constructor.newInstance(this);

                Bukkit.getLogger().info("Registering listener: " + listener.getClass().getSimpleName());

                this.pm.registerEvents(listener, this);
            }
        }
        catch (IllegalAccessException | InstantiationException |
               NoSuchMethodException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
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