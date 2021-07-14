package me.M0dii.ExtraEnchants;

import me.M0dii.ExtraEnchants.Commands.Enchant;
import me.M0dii.ExtraEnchants.Commands.Unenchant;
import me.M0dii.ExtraEnchants.Enchants.RegisterEnchants;
import me.M0dii.ExtraEnchants.Listeners.*;
import me.M0dii.ExtraEnchants.Listeners.Custom.CustomCombine;
import me.M0dii.ExtraEnchants.Listeners.Custom.OnSmelt;
import me.M0dii.ExtraEnchants.Listeners.Custom.OnTelepathy;
import me.M0dii.ExtraEnchants.Listeners.Custom.OnTill;
import me.M0dii.ExtraEnchants.Utils.Data.ConfigManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.MultiLineChart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ExtraEnchants extends JavaPlugin
{
    public static ExtraEnchants instance;
    
    private PluginManager pm;
    private ConfigManager configManager;
    
    public FileConfiguration getCfg()
    {
        return this.configManager.getConfig();
    }
    
    public void onEnable()
    {
        RegisterEnchants.register();
        
        instance = this;
        
        this.configManager = new ConfigManager(this);
        
        this.pm = this.getServer().getPluginManager();
        
        this.getLogger().info("EnhancedEnchantments has been enabled.");
        
        registerEvents();
        registerCommands();
    
        setupMetrics();
        checkForUpdates();
    }
    
    private void checkForUpdates()
    {
        new UpdateChecker(this, 88737).getVersion(ver ->
        {
            String curr = this.getDescription().getVersion();
            
            if (!curr.equalsIgnoreCase(
                    ver.replace("v", "")))
            {
                getLogger().info("You are running an outdated version of M0-ExtraEnchants.");
                getLogger().info("Latest version: " + ver + ", you are using: " + curr);
                getLogger().info("You can download the latest version on Spigot:");
                getLogger().info("https://www.spigotmc.org/resources/m0-extraenchants-1-13-1-16.88737/");
            }
        });
    }
    
    private void setupMetrics()
    {
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
    
    public void onDisable()
    {
        this.getLogger().info("EnhancedEnchantments has been disabled.");
    }
    
    private void registerEvents()
    {
        this.pm.registerEvents(new PlayerInteract(this), this);
        this.pm.registerEvents(new BlockBreak(this), this);
        this.pm.registerEvents(new EnchantmentCombine(), this);
        this.pm.registerEvents(new CustomCombine(this), this);
        this.pm.registerEvents(new TillInteract(this), this);
        this.pm.registerEvents(new OnTelepathy(this), this);
        this.pm.registerEvents(new OnTill(), this);
        this.pm.registerEvents(new OnSmelt(this), this);
        this.pm.registerEvents(new AnvilCombine(this), this);
    }
    
    private void registerCommands()
    {
        PluginCommand ee = getCommand("extraenchant");
        
        if(ee != null)
            ee.setExecutor(new Enchant(this));
    
        PluginCommand ue = getCommand("unenchant");
    
        if(ue != null)
            ue.setExecutor(new Unenchant(this));
    }
    
    public String format(String msg)
    {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}