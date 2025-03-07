package org.frizzlenpop.frizzlenGaurd.commands.admin;

import org.bukkit.command.CommandSender;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;

import java.util.Collections;
import java.util.List;

public class ReloadCommand extends AbstractCommand {
    
    public ReloadCommand(FrizzlenGaurd plugin) {
        super(plugin, "reload", "Reloads the plugin configuration", "/fg reload", "frizzlengaurd.admin.reload");
    }
    
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("rl");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        // Reload configurations
        plugin.getConfigManager().reloadConfigs();
        
        // Save current data before reloading
        plugin.getDataManager().saveData();
        
        // Reload data
        plugin.getDataManager().loadData();
        
        // Send success message
        sender.sendMessage(getMessage("general.plugin-reloaded", "&aPlugin reloaded successfully."));
        
        return true;
    }
} 