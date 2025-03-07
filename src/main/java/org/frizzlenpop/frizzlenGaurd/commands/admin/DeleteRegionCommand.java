package org.frizzlenpop.frizzlenGaurd.commands.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DeleteRegionCommand extends AbstractCommand {
    
    public DeleteRegionCommand(FrizzlenGaurd plugin) {
        super(plugin, "delregion", "Delete a region", 
              "/fg delregion <regionName> [confirm]", "frizzlengaurd.admin.delete");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("deleteregion", "delete");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        String regionName = args[0];
        boolean confirm = args.length == 2 && args[1].equalsIgnoreCase("confirm");
        
        // Find the region
        Region targetRegion = null;
        for (Region region : plugin.getRegionManager().getAllRegions()) {
            if (region.getName().equalsIgnoreCase(regionName)) {
                targetRegion = region;
                break;
            }
        }
        
        if (targetRegion == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found.");
            return true;
        }
        
        // If player is not an admin, check if they are the owner
        if (sender instanceof Player && !sender.hasPermission("frizzlengaurd.admin.*")) {
            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();
            
            if (!targetRegion.getOwner().equals(playerId)) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to delete this region.");
                return true;
            }
        }
        
        // Check if the player has confirmed the deletion
        if (!confirm) {
            sender.sendMessage(ChatColor.RED + "Are you sure you want to delete region '" + regionName + "'?");
            sender.sendMessage(ChatColor.RED + "This action cannot be undone! Type " + 
                    ChatColor.YELLOW + "/fg delregion " + regionName + " confirm" + 
                    ChatColor.RED + " to confirm.");
            return true;
        }
        
        // Delete the region
        plugin.getRegionManager().removeRegion(targetRegion.getId());
        
        // Send success message
        String message = getMessage("claim.claim-deleted", "&aSuccessfully deleted claim &f%name%&a.");
        message = message.replace("%name%", targetRegion.getName());
        sender.sendMessage(message);
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            
            if (sender instanceof Player && !sender.hasPermission("frizzlengaurd.admin.*")) {
                // Non-admins can only see their own regions
                Player player = (Player) sender;
                UUID playerId = player.getUniqueId();
                
                completions = plugin.getRegionManager().getPlayerRegions(playerId).stream()
                        .map(Region::getName)
                        .filter(name -> name.toLowerCase().startsWith(partialName))
                        .collect(Collectors.toList());
            } else {
                // Admins can see all regions
                completions = plugin.getRegionManager().getAllRegions().stream()
                        .map(Region::getName)
                        .filter(name -> name.toLowerCase().startsWith(partialName))
                        .collect(Collectors.toList());
            }
            
            return completions;
        } else if (args.length == 2) {
            // Tab complete "confirm"
            String partialConfirm = args[1].toLowerCase();
            if ("confirm".startsWith(partialConfirm)) {
                return Collections.singletonList("confirm");
            }
        }
        
        return new ArrayList<>();
    }
} 