package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.utils.RegionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MergeCommand extends AbstractCommand {
    
    public MergeCommand(FrizzlenGaurd plugin) {
        super(plugin, "merge", "Merge two regions into one", 
              "/fg merge <region1> <region2> [newName]", "frizzlengaurd.merge");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        Player player = getPlayer(sender);
        String region1Name = args[0];
        String region2Name = args[1];
        String newName = args.length > 2 ? args[2] : region1Name;
        
        // Find both regions
        Region region1 = null;
        Region region2 = null;
        
        for (Region region : plugin.getRegionManager().getAllRegions()) {
            if (region.getName().equalsIgnoreCase(region1Name)) {
                region1 = region;
            } else if (region.getName().equalsIgnoreCase(region2Name)) {
                region2 = region;
            }
        }
        
        if (region1 == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + region1Name + "' not found.");
            return true;
        }
        
        if (region2 == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + region2Name + "' not found.");
            return true;
        }
        
        // Check if player owns both regions or has admin permission
        if (!player.hasPermission("frizzlengaurd.admin") && 
            (!region1.isOwner(player) || !region2.isOwner(player))) {
            sender.sendMessage(ChatColor.RED + "You must own both regions to merge them.");
            return true;
        }
        
        // Check if regions are adjacent or overlapping
        if (!RegionUtils.areRegionsAdjacent(region1, region2) && 
            !RegionUtils.doRegionsOverlap(region1, region2)) {
            sender.sendMessage(ChatColor.RED + "Regions must be adjacent or overlapping to merge them.");
            return true;
        }
        
        // Check if new name is already taken (if different from region1)
        if (!newName.equalsIgnoreCase(region1Name) && 
            plugin.getRegionManager().getRegion(newName) != null) {
            sender.sendMessage(ChatColor.RED + "A region with the name '" + newName + "' already exists.");
            return true;
        }
        
        try {
            // Create merged region
            Region mergedRegion = RegionUtils.mergeRegions(region1, region2, newName);
            
            // Copy owners and members from region1
            mergedRegion.setOwners(region1.getOwners());
            mergedRegion.setMembers(region1.getMembers());
            mergedRegion.setFlags(region1.getFlags());
            
            // Add any unique owners and members from region2
            region2.getOwners().forEach((uuid, role) -> mergedRegion.getOwners().put(uuid, role));
            region2.getMembers().forEach((uuid, role) -> mergedRegion.getMembers().put(uuid, role));
            
            // Remove old regions and add new one
            plugin.getRegionManager().removeRegion(region1.getName());
            plugin.getRegionManager().removeRegion(region2.getName());
            plugin.getRegionManager().addRegion(mergedRegion);
            
            // Save changes
            plugin.getDataManager().saveData();
            
            sender.sendMessage(ChatColor.GREEN + "Successfully merged regions '" + 
                    region1Name + "' and '" + region2Name + "' into '" + newName + "'.");
            
            // Show preview of merged region
            plugin.getVisualsManager().showRegionBoundaries(player, mergedRegion, 200);
            
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Failed to merge regions: " + e.getMessage());
        }
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1 || args.length == 2) {
            // Tab complete region names that the player owns
            String partial = args[args.length - 1].toLowerCase();
            
            return plugin.getRegionManager().getAllRegions().stream()
                    .filter(region -> region.isOwner(player) || 
                                    player.hasPermission("frizzlengaurd.admin"))
                    .map(Region::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 