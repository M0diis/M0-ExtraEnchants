package me.M0dii.ExtraEnchants.Commands;

import me.M0dii.ExtraEnchants.ExtraEnchants;
import me.M0dii.ExtraEnchants.Utils.Enchanter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Enchant implements CommandExecutor, TabCompleter
{
    private final ExtraEnchants plugin;
    private final FileConfiguration cfg;
    
    public Enchant(ExtraEnchants plugin)
    {
        this.plugin = plugin;
        
        this.cfg = plugin.getCfg();
    }
    
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd,
                             @Nonnull String label, @Nonnull String[] args)
    {
        if(!sender.hasPermission("extraenchants.command.enchant"))
        {
            sender.sendMessage(this.format(this.cfg.getString("messages.no-permission")));
            
            return true;
        }
        
        if(args.length == 0)
        {
            sender.sendMessage(this.format(this.cfg.getString("messages.usage")));
            
            return true;
        }
        
        if(args.length == 1)
        {
            if(!(sender instanceof Player))
            {
                sender.sendMessage(this.format(this.cfg.getString("messages.usage")));
        
                return true;
            }
    
            Player player = (Player)sender;
    
            if(giveEnchantBook(args, player, "telepathy", "TELEPATHY"))
                return true;
    
            if(giveEnchantBook(args, player, "plow", "PLOW"))
                return true;
    
            if(giveEnchantBook(args, player, "smelt", "SMELT"))
                return true;
    
            sender.sendMessage(this.format(this.cfg.getString("messages.enchantment-list")));
            
            return true;
        }
        
        if(args.length >= 2)
        {
            if(giveEnchantBook(args, Bukkit.getPlayer(args[1]), "telepathy", "TELEPATHY"))
                return true;
    
            if(giveEnchantBook(args, Bukkit.getPlayer(args[1]), "plow", "PLOW"))
                return true;
    
            if(giveEnchantBook(args, Bukkit.getPlayer(args[1]), "smelt", "SMELT"))
                return true;
    
            sender.sendMessage(this.format(this.cfg.getString("messages.enchantment-list")));
            
            return true;
        }
        
        return false;
    }
    
    private boolean giveEnchantBook(@Nonnull String[] args, Player player, String enchant, String telepathy2)
    {
        if(args[0].equalsIgnoreCase(enchant) && player != null)
        {
            ItemStack item = Enchanter.getBook(telepathy2);
            
            if(player.getInventory().firstEmpty() == -1)
            {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                
                return true;
            }
            
            player.getInventory().addItem(item);
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command cmd,
                                      @Nonnull String alias, @Nonnull String[] args)
    {
        List<String> completes = new ArrayList<>();
        
        if(args.length == 1)
        {
            completes.add("plow");
            completes.add("telepathy");
            completes.add("smelt");
            completes.add("beheading");
        }
    
        if(args.length == 2)
        {
            for(Player p : Bukkit.getOnlinePlayers())
                completes.add(p.getName());
        }
        
        return completes;
    }
    
    public String format(String msg)
    {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}