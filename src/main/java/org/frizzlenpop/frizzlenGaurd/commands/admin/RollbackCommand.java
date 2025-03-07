package org.frizzlenpop.frizzlenGaurd.commands.admin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.models.LogEntry;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.utils.RollbackManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class RollbackCommand extends AbstractCommand {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public RollbackCommand(FrizzlenGaurd plugin) {
        super(plugin, "rollback", "Rollback changes in a region", 
              "/fg rollback <region> [time] [player] [action]", "frizzlengaurd.admin.rollback");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("rb", "restore");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        Player player = getPlayer(sender);
        
        // Handle cancel command
        if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
            // Attempt to cancel any active rollback session
            if (plugin.getRollbackManager().cancelRollback(player.getUniqueId())) {
                sender.sendMessage(ChatColor.YELLOW + "Rollback session cancelled.");
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have an active rollback session.");
            }
            return true;
        }
        
        // Require at least the region name
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            sender.sendMessage(ChatColor.RED + "Or use: /fg rollback cancel - to cancel an active rollback");
            return true;
        }
        
        String regionName = args[0];
        
        // Find the region
        Region region = null;
        for (Region r : plugin.getRegionManager().getAllRegions()) {
            if (r.getName().equalsIgnoreCase(regionName)) {
                region = r;
                break;
            }
        }
        
        if (region == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found.");
            return true;
        }
        
        // Parse optional parameters
        long timestamp = 0;
        UUID playerFilter = null;
        List<LogEntry.LogAction> actionFilter = null;
        
        // Parse timestamp (if provided)
        if (args.length >= 2 && !args[1].equalsIgnoreCase("*")) {
            try {
                if (args[1].matches("\\d+[smhd]")) {
                    // Format like "30m" (30 minutes), "2h" (2 hours), etc.
                    int value = Integer.parseInt(args[1].substring(0, args[1].length() - 1));
                    char unit = args[1].charAt(args[1].length() - 1);
                    
                    long now = System.currentTimeMillis();
                    switch (unit) {
                        case 's':
                            timestamp = now - (value * 1000L);
                            break;
                        case 'm':
                            timestamp = now - (value * 60 * 1000L);
                            break;
                        case 'h':
                            timestamp = now - (value * 60 * 60 * 1000L);
                            break;
                        case 'd':
                            timestamp = now - (value * 24 * 60 * 60 * 1000L);
                            break;
                    }
                } else {
                    // Try to parse as a date/time
                    Date date = DATE_FORMAT.parse(args[1]);
                    timestamp = date.getTime();
                }
            } catch (ParseException | NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid time format. Use format like '30m', '2h', or 'yyyy-MM-dd HH:mm:ss'.");
                return true;
            }
        }
        
        // Parse player filter (if provided)
        if (args.length >= 3 && !args[2].equalsIgnoreCase("*")) {
            String playerName = args[2];
            OfflinePlayer targetPlayer = null;
            
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(playerName)) {
                    targetPlayer = offlinePlayer;
                    break;
                }
            }
            
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found.");
                return true;
            }
            
            playerFilter = targetPlayer.getUniqueId();
        }
        
        // Parse action filter (if provided)
        if (args.length >= 4 && !args[3].equalsIgnoreCase("*")) {
            String actionName = args[3].toUpperCase();
            actionFilter = new ArrayList<>();
            
            try {
                LogEntry.LogAction action = LogEntry.LogAction.valueOf(actionName);
                actionFilter.add(action);
            } catch (IllegalArgumentException e) {
                // Try to match multiple actions
                if (actionName.equals("BLOCKS")) {
                    actionFilter.add(LogEntry.LogAction.BLOCK_BREAK);
                    actionFilter.add(LogEntry.LogAction.BLOCK_PLACE);
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid action type: " + actionName);
                    sender.sendMessage(ChatColor.RED + "Valid actions: " + 
                            Arrays.stream(LogEntry.LogAction.values())
                                  .map(LogEntry.LogAction::name)
                                  .collect(Collectors.joining(", ")));
                    return true;
                }
            }
        }
        
        // Start the rollback
        if (plugin.getRollbackManager().startRollback(player, region, timestamp, playerFilter, actionFilter)) {
            sender.sendMessage(ChatColor.GREEN + "Starting rollback for region '" + regionName + "'...");
            sender.sendMessage(ChatColor.GREEN + "Type '/fg rollback cancel' to cancel the rollback.");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to start rollback. You may already have an active rollback session.");
        }
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            // Tab complete region name or "cancel"
            List<String> completions = new ArrayList<>();
            String partialName = args[0].toLowerCase();
            
            // Add "cancel" option
            if ("cancel".startsWith(partialName)) {
                completions.add("cancel");
            }
            
            // Add region names
            completions.addAll(plugin.getRegionManager().getAllRegions().stream()
                    .map(Region::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList()));
            
            return completions;
        } else if (args.length == 2) {
            // Tab complete time options
            String partial = args[1].toLowerCase();
            List<String> completions = new ArrayList<>();
            
            // Add some common time options
            completions.add("*");  // All time
            completions.add("15m"); // 15 minutes
            completions.add("1h");  // 1 hour
            completions.add("6h");  // 6 hours
            completions.add("1d");  // 1 day
            completions.add(DATE_FORMAT.format(new Date())); // Current time
            
            return completions.stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            // Tab complete player names or "*" for all players
            String partial = args[2].toLowerCase();
            List<String> completions = new ArrayList<>();
            
            completions.add("*"); // All players
            
            // Add online player names
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
            
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 4) {
            // Tab complete action types
            String partial = args[3].toLowerCase();
            List<String> completions = new ArrayList<>();
            
            completions.add("*"); // All actions
            completions.add("BLOCKS"); // All block actions
            
            // Add all action types
            for (LogEntry.LogAction action : LogEntry.LogAction.values()) {
                completions.add(action.name());
            }
            
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 