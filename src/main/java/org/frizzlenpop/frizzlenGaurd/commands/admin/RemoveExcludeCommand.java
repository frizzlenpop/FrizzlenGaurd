package org.frizzlenpop.frizzlenGaurd.commands.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RemoveExcludeCommand extends AbstractCommand {
    
    public RemoveExcludeCommand(FrizzlenGaurd plugin) {
        super(plugin, "removeexclude", "Remove a world from the exclusion list", 
              "/fg removeexclude <worldName>", "frizzlengaurd.admin.exclude");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("includeworld", "include");
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
        
        // Get the configuration and excluded worlds list
        FileConfiguration config = plugin.getConfigManager().getMainConfig();
        List<String> excludedWorlds = config.getStringList("worlds.excluded");
        
        // Check if world is in the excluded list
        if (!excludedWorlds.contains(worldName)) {
            sender.sendMessage(ChatColor.RED + "World '" + worldName + "' is not in the exclusion list.");
            return true;
        }
        
        // Remove the world from the excluded list
        excludedWorlds.remove(worldName);
        config.set("worlds.excluded", excludedWorlds);
        
        // Save the configuration
        plugin.getConfigManager().saveConfig("config");
        
        // Send success message
        String message = getMessage("admin.world-included", "&aWorld &f%world% &ahas been included for claiming.");
        message = message.replace("%world%", worldName);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Tab complete world names that are already excluded
            String partialName = args[0].toLowerCase();
            
            // Get the excluded worlds list
            List<String> excludedWorlds = plugin.getConfigManager().getMainConfig().getStringList("worlds.excluded");
            
            // Return excluded worlds that match the partial name
            return excludedWorlds.stream()
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 