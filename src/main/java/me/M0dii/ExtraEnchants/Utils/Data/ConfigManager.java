package me.M0dii.ExtraEnchants.Utils.Data;

import me.M0dii.ExtraEnchants.ExtraEnchants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager
{
    private final ExtraEnchants plugin;
    private FileConfiguration dataConfig = null;
    private File dataConfigFile = null;
    
    String configFile = "config.yml";
    
    public ConfigManager(ExtraEnchants plugin)
    {
        this.plugin = plugin;
        
        this.saveDefaultConfig();
    }
    
    public void reloadConfig()
    {
        if(this.dataConfigFile == null)
            this.dataConfigFile = new File(this.plugin.getDataFolder(), this.configFile);
        
        this.dataConfig = YamlConfiguration.loadConfiguration(this.dataConfigFile);
        
        InputStream defConfigStream = this.plugin.getResource(this.configFile);
        
        if(defConfigStream != null)
        {
            YamlConfiguration defConfig =
                    YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            
            this.dataConfig.setDefaults(defConfig);
        }
    }
    
    public FileConfiguration getConfig()
    {
        if(this.dataConfig == null)
            reloadConfig();

        return this.dataConfig;
    }
    
    public void saveConfig()
    {
        if(this.dataConfig == null || this.dataConfigFile == null)
            return;
        
        try
        {
            this.getConfig().save(this.dataConfigFile);
        }
        catch(IOException ex)
        {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.dataConfigFile, ex);
        }
    }
    
    public void saveDefaultConfig()
    {
        if(this.dataConfigFile == null)
            this.dataConfigFile = new File(this.plugin.getDataFolder(), this.configFile);

        if(!this.dataConfigFile.exists())
            this.plugin.saveResource(this.configFile, false);
    }
}