package me.M0dii.ExtraEnchants;

import me.M0dii.ExtraEnchants.Commands.Enchant;
import me.M0dii.ExtraEnchants.Commands.Unenchant;
import me.M0dii.ExtraEnchants.Enchants.RegisterEnchants;
import me.M0dii.ExtraEnchants.Listeners.*;
import me.M0dii.ExtraEnchants.Listeners.Custom.*;
import me.M0dii.ExtraEnchants.Utils.Data.ConfigManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.MultiLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

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
        RegisterEnchants.register();

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
        this.pm.registerEvents(new CustomCombine(), this);
        this.pm.registerEvents(new TillInteract(this), this);
        this.pm.registerEvents(new OnTelepathy(), this);
        this.pm.registerEvents(new OnTill(), this);
        this.pm.registerEvents(new OnSmelt(), this);
        this.pm.registerEvents(new OnLavaWalk(this), this);
        this.pm.registerEvents(new OnHasteMine(), this);
        this.pm.registerEvents(new OnExperienceMine(this), this);
        this.pm.registerEvents(new OnLifesteal(), this);
        this.pm.registerEvents(new OnVeinMine(this), this);
        this.pm.registerEvents(new AnvilCombine(), this);
        this.pm.registerEvents(new ItemDrop(this), this);
        this.pm.registerEvents(new PlayerDamage(this), this);
        this.pm.registerEvents(new OnAntiThorns(this), this);
    }

    private void registerCommands() {
        PluginCommand ee = getCommand("extraenchant");

        if (ee != null)
            ee.setExecutor(new Enchant(this));

        PluginCommand ue = getCommand("unenchant");

        if (ue != null)
            ue.setExecutor(new Unenchant(this));
    }
}