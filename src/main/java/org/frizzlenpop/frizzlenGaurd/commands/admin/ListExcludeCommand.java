package org.frizzlenpop.frizzlenGaurd.commands.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;

import java.util.Arrays;
import java.util.List;

public class ListExcludeCommand extends AbstractCommand {
    
    public ListExcludeCommand(FrizzlenGaurd plugin) {
        super(plugin, "listexclude", "List all worlds excluded from claiming", 
              "/fg listexclude", "frizzlengaurd.admin.exclude");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("excludelist", "listexcluded");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        // Get the excluded worlds list
        List<String> excludedWorlds = plugin.getConfigManager().getMainConfig().getStringList("worlds.excluded");
        
        if (excludedWorlds.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "There are no worlds excluded from claiming.");
            return true;
        }
        
        // Build a comma-separated list of excluded worlds
        String worldsStr = String.join(", ", excludedWorlds);
        
        // Send message to sender
        String message = getMessage("admin.excluded-worlds", "&aExcluded worlds: &f%worlds%");
        message = message.replace("%worlds%", worldsStr);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        
        return true;
    }
} 