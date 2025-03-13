package org.frizzlenpop.frizzlenGaurd.commands.admin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.commands.player.ClaimCommand;
import org.frizzlenpop.frizzlenGaurd.models.LogEntry;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AdminClaimCommand extends AbstractCommand {
    
    public AdminClaimCommand(FrizzlenGaurd plugin) {
        super(plugin, "adminclaim", "Creates an admin region without restrictions", 
              "/fg adminclaim <name>", "frizzlengaurd.admin.claim");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("aclaim", "ac");
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
        
        // Get selection points from ClaimCommand
        ClaimCommand claimCommand = ((org.frizzlenpop.frizzlenGaurd.commands.CommandHandler) plugin.getCommand("fg").getExecutor()).getClaimCommand();
        
        UUID playerId = player.getUniqueId();
        Location pos1 = claimCommand.getFirstPoint(playerId);
        Location pos2 = claimCommand.getSecondPoint(playerId);
        
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        String name = args[0];
        
        // Check if both points are set
        if (pos1 == null || pos2 == null) {
            sender.sendMessage(ChatColor.RED + "You must select two points first using a selection stick or /fg claim pos1/pos2.");
            return true;
        }
        
        // Check if the points are in the same world
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            sender.sendMessage(ChatColor.RED + "Both selection points must be in the same world.");
            return true;
        }
        
        // Create the region without normal restrictions
        String regionId = java.util.UUID.randomUUID().toString();
        Region region = new Region(regionId, name, playerId, pos1, pos2);
        
        // Add to server regions
        plugin.getRegionManager().addRegion(region);
        
        // Log the creation
        region.addLogEntry(new LogEntry(player, LogEntry.LogAction.CLAIM_CREATE, 
                "Created admin claim " + name, null));
        
        // Send success message
        String message = getMessage("claim.claim-created", "&aSuccessfully created admin claim &f%name%&a.");
        message = message.replace("%name%", name);
        sender.sendMessage(message);
        
        // Visualize the region boundaries
        plugin.getVisualsManager().showRegionBoundaries(player, region);
        
        // Clear selection points
        claimCommand.clearPoints(playerId);
        
        return true;
    }
} 