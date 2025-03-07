package org.frizzlenpop.frizzlenGaurd.commands.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.managers.BackupManager;
import org.frizzlenpop.frizzlenGaurd.managers.BackupManager.BackupInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BackupCommand extends AbstractCommand {
    
    public BackupCommand(FrizzlenGaurd plugin) {
        super(plugin, "backup", "Manage region backups", 
              "/fg backup <create|restore|list> [name] [description]", "frizzlengaurd.admin.backup");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "create":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /fg backup create <name> [description]");
                    return true;
                }
                
                String name = args[1];
                String description = args.length > 2 ? 
                        String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : 
                        "Manual backup";
                
                plugin.getBackupManager().createBackup(name, description);
                sender.sendMessage(ChatColor.GREEN + "Created backup '" + name + "'.");
                break;
                
            case "restore":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /fg backup restore <filename>");
                    return true;
                }
                
                String fileName = args[1];
                if (!fileName.endsWith(".zip")) {
                    fileName += ".zip";
                }
                
                if (plugin.getBackupManager().restoreBackup(fileName)) {
                    sender.sendMessage(ChatColor.GREEN + "Successfully restored backup '" + fileName + "'.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to restore backup '" + fileName + "'.");
                }
                break;
                
            case "list":
                List<BackupInfo> backups = plugin.getBackupManager().getBackups();
                
                if (backups.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "No backups found.");
                    return true;
                }
                
                sender.sendMessage(ChatColor.GREEN + "Available backups:");
                for (BackupInfo backup : backups) {
                    sender.sendMessage(ChatColor.YELLOW + "- " + backup.getFileName() + 
                            ChatColor.GRAY + " (" + backup.getFormattedDate() + ")");
                    if (backup.getDescription() != null && !backup.getDescription().isEmpty()) {
                        sender.sendMessage(ChatColor.GRAY + "  Description: " + backup.getDescription());
                    }
                }
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action. Use: create, restore, list");
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> actions = Arrays.asList("create", "restore", "list");
            
            return actions.stream()
                    .filter(action -> action.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("restore")) {
            String partial = args[1].toLowerCase();
            
            return plugin.getBackupManager().getBackups().stream()
                    .map(BackupInfo::getFileName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 