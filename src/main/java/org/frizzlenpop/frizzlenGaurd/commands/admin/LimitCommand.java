package org.frizzlenpop.frizzlenGaurd.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LimitCommand extends AbstractCommand {
    
    public LimitCommand(FrizzlenGaurd plugin) {
        super(plugin, "limit", "Manage claim block limits", 
              "/fg limit <set|info> <player> [limit]", "frizzlengaurd.admin.limit");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        String action = args[0].toLowerCase();
        String playerName = args[1];
        
        // Find the target player
        OfflinePlayer target = null;
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && 
                offlinePlayer.getName().equalsIgnoreCase(playerName)) {
                target = offlinePlayer;
                break;
            }
        }
        
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found.");
            return true;
        }
        
        UUID targetId = target.getUniqueId();
        
        switch (action) {
            case "set":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /fg limit set <player> <limit>");
                    return true;
                }
                
                int limit;
                try {
                    limit = Integer.parseInt(args[2]);
                    if (limit < 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Limit must be a positive number or 0 to remove.");
                    return true;
                }
                
                plugin.getLimitManager().setPlayerLimit(targetId, limit);
                
                if (limit > 0) {
                    sender.sendMessage(ChatColor.GREEN + "Set claim block limit for " + 
                            target.getName() + " to " + limit + " blocks.");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Removed custom claim block limit for " + 
                            target.getName() + ".");
                }
                break;
                
            case "info":
                if (target.isOnline()) {
                    Player onlineTarget = target.getPlayer();
                    int playerLimit = plugin.getLimitManager().getLimit(onlineTarget);
                    int usedBlocks = plugin.getLimitManager().getUsedBlocks(targetId);
                    int remainingBlocks = plugin.getLimitManager().getRemainingBlocks(onlineTarget);
                    
                    sender.sendMessage(ChatColor.GREEN + "Claim block information for " + target.getName() + ":");
                    sender.sendMessage(ChatColor.YELLOW + "Total limit: " + ChatColor.WHITE + playerLimit);
                    sender.sendMessage(ChatColor.YELLOW + "Used blocks: " + ChatColor.WHITE + usedBlocks);
                    sender.sendMessage(ChatColor.YELLOW + "Remaining: " + ChatColor.WHITE + remainingBlocks);
                } else {
                    int usedBlocks = plugin.getLimitManager().getUsedBlocks(targetId);
                    sender.sendMessage(ChatColor.GREEN + "Claim block information for " + target.getName() + ":");
                    sender.sendMessage(ChatColor.YELLOW + "Used blocks: " + ChatColor.WHITE + usedBlocks);
                    sender.sendMessage(ChatColor.GRAY + "Player is offline, cannot show limit information.");
                }
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action. Use: set, info");
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
            List<String> actions = Arrays.asList("set", "info");
            
            return actions.stream()
                    .filter(action -> action.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String partial = args[1].toLowerCase();
            
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            String partial = args[2];
            List<String> limits = Arrays.asList("0", "10000", "50000", "100000", "500000");
            
            return limits.stream()
                    .filter(limit -> limit.startsWith(partial))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 