package org.frizzlenpop.frizzlenGaurd.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetExcludeCommand extends AbstractCommand {
    
    public SetExcludeCommand(FrizzlenGaurd plugin) {
        super(plugin, "setexclude", "Exclude a world from land claiming", 
              "/fg setexclude <worldName>", "frizzlengaurd.admin.exclude");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("exclude", "excludeworld");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        String worldName = args[0];
        
        // Check if the world exists
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(ChatColor.RED + "World '" + worldName + "' does not exist.");
            return true;
        }
        
        // Get the configuration and excluded worlds list
        FileConfiguration config = plugin.getConfigManager().getMainConfig();
        List<String> excludedWorlds = config.getStringList("worlds.excluded");
        
        // Check if world is already excluded
        if (excludedWorlds.contains(worldName)) {
            sender.sendMessage(ChatColor.RED + "World '" + worldName + "' is already excluded from claiming.");
            return true;
        }
        
        // Add the world to the excluded list
        excludedWorlds.add(worldName);
        config.set("worlds.excluded", excludedWorlds);
        
        // Save the configuration
        plugin.getConfigManager().saveConfig("config");
        
        // Send success message
        String message = getMessage("admin.world-excluded", "&aWorld &f%world% &ahas been excluded from claiming.");
        message = message.replace("%world%", worldName);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Tab complete world names that aren't already excluded
            String partialName = args[0].toLowerCase();
            
            // Get the excluded worlds list
            List<String> excludedWorlds = plugin.getConfigManager().getMainConfig().getStringList("worlds.excluded");
            
            // Return worlds that match the partial name and aren't already excluded
            return Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .filter(name -> !excludedWorlds.contains(name))
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 