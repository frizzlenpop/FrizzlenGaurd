package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.models.LogEntry;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.models.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LogsCommand extends AbstractCommand {
    
    public LogsCommand(FrizzlenGaurd plugin) {
        super(plugin, "logs", "View logs for a region", 
              "/fg logs <regionName> [page]", "frizzlengaurd.info");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("log", "history");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        Player player = getPlayer(sender);
        UUID playerId = player.getUniqueId();
        
        String regionName = args[0];
        int page = 1;
        
        if (args.length == 2) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) {
                    page = 1;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Page must be a number.");
                return true;
            }
        }
        
        // Get the region
        Region targetRegion = null;
        boolean isAdmin = player.hasPermission("frizzlengaurd.admin.*");
        
        // Search all regions
        for (Region region : plugin.getRegionManager().getAllRegions()) {
            if (region.getName().equalsIgnoreCase(regionName)) {
                // Check if player has access to this region
                if (isAdmin || region.getOwner().equals(playerId) || region.isMember(playerId)) {
                    targetRegion = region;
                    break;
                }
            }
        }
        
        if (targetRegion == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found or you don't have access to it.");
            return true;
        }
        
        // Get the logs
        List<LogEntry> logs = targetRegion.getLogs();
        
        if (logs.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No logs found for region '" + regionName + "'.");
            return true;
        }
        
        // Sort logs by timestamp (newest first)
        Collections.sort(logs, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        
        // Paginate the logs
        int logsPerPage = 10;
        int totalPages = (int) Math.ceil((double) logs.size() / logsPerPage);
        
        if (page > totalPages) {
            page = totalPages;
        }
        
        int startIndex = (page - 1) * logsPerPage;
        int endIndex = Math.min(startIndex + logsPerPage, logs.size());
        
        // Display logs
        sender.sendMessage(ChatColor.GOLD + "===== Logs for '" + targetRegion.getName() + 
                "' (Page " + page + "/" + totalPages + ") =====");
        
        for (int i = startIndex; i < endIndex; i++) {
            LogEntry log = logs.get(i);
            
            ChatColor actionColor;
            switch (log.getAction()) {
                case CLAIM_CREATE:
                case CLAIM_DELETE:
                case CLAIM_MODIFY:
                    actionColor = ChatColor.RED;
                    break;
                case MEMBER_ADD:
                case MEMBER_REMOVE:
                    actionColor = ChatColor.YELLOW;
                    break;
                case FLAG_CHANGE:
                    actionColor = ChatColor.AQUA;
                    break;
                case BLOCK_BREAK:
                case BLOCK_PLACE:
                    actionColor = ChatColor.GREEN;
                    break;
                default:
                    actionColor = ChatColor.GRAY;
                    break;
            }
            
            sender.sendMessage(
                    ChatColor.GRAY + log.getFormattedTimestamp() + " " + 
                    ChatColor.WHITE + log.getPlayerName() + ": " + 
                    actionColor + log.getAction() + " " + 
                    ChatColor.WHITE + log.getDetails());
        }
        
        // Show pagination info if there are multiple pages
        if (totalPages > 1) {
            sender.sendMessage(ChatColor.GOLD + "===== Use " + ChatColor.YELLOW + 
                    "/fg logs " + regionName + " <page>" + ChatColor.GOLD + " to see more logs =====");
        }
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            List<String> regions = new ArrayList<>();
            String partialName = args[0].toLowerCase();
            UUID playerId = player.getUniqueId();
            boolean isAdmin = player.hasPermission("frizzlengaurd.admin.*");
            
            if (isAdmin) {
                // Admins can see all regions
                regions = plugin.getRegionManager().getAllRegions().stream()
                        .map(Region::getName)
                        .filter(name -> name.toLowerCase().startsWith(partialName))
                        .collect(Collectors.toList());
            } else {
                // Regular players can only see their own regions or regions they have access to
                regions = plugin.getRegionManager().getAllRegions().stream()
                        .filter(region -> region.getOwner().equals(playerId) || region.isMember(playerId))
                        .map(Region::getName)
                        .filter(name -> name.toLowerCase().startsWith(partialName))
                        .collect(Collectors.toList());
            }
            
            return regions;
        } else if (args.length == 2) {
            // Tab complete possible page numbers
            String regionName = args[0];
            Region targetRegion = null;
            UUID playerId = player.getUniqueId();
            boolean isAdmin = player.hasPermission("frizzlengaurd.admin.*");
            
            // Find the region
            for (Region region : plugin.getRegionManager().getAllRegions()) {
                if (region.getName().equalsIgnoreCase(regionName)) {
                    if (isAdmin || region.getOwner().equals(playerId) || region.isMember(playerId)) {
                        targetRegion = region;
                        break;
                    }
                }
            }
            
            if (targetRegion != null) {
                int logSize = targetRegion.getLogs().size();
                int totalPages = (int) Math.ceil((double) logSize / 10);
                
                if (totalPages <= 1) {
                    return new ArrayList<>();
                }
                
                List<String> pages = new ArrayList<>();
                for (int i = 1; i <= totalPages; i++) {
                    pages.add(String.valueOf(i));
                }
                
                return pages.stream()
                        .filter(page -> page.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
} 