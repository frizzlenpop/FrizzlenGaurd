package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ResizeCommand extends AbstractCommand {
    
    public ResizeCommand(FrizzlenGaurd plugin) {
        super(plugin, "resize", "Resize a region in a specific direction", 
              "/fg resize <region> <direction> <amount>", "frizzlengaurd.resize");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            sender.sendMessage(ChatColor.RED + "Directions: north, south, east, west, up, down");
            return true;
        }
        
        Player player = getPlayer(sender);
        String regionName = args[0];
        String direction = args[1].toLowerCase();
        int amount;
        
        try {
            amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Amount must be a positive number.");
            return true;
        }
        
        // Find the region
        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found.");
            return true;
        }
        
        // Check if player owns the region or has admin permission
        if (!region.isOwner(player) && !player.hasPermission("frizzlengaurd.admin")) {
            sender.sendMessage(ChatColor.RED + "You must own this region to resize it.");
            return true;
        }
        
        // Get current bounds
        Location min = region.getMinPoint();
        Location max = region.getMaxPoint();
        Location newMin = min.clone();
        Location newMax = max.clone();
        
        // Apply resize based on direction
        switch (direction) {
            case "north":
                newMax.add(0, 0, amount);
                break;
            case "south":
                newMin.subtract(0, 0, amount);
                break;
            case "east":
                newMax.add(amount, 0, 0);
                break;
            case "west":
                newMin.subtract(amount, 0, 0);
                break;
            case "up":
                newMax.add(0, amount, 0);
                break;
            case "down":
                newMin.subtract(0, amount, 0);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid direction. Use: north, south, east, west, up, down");
                return true;
        }
        
        // Check if new size is within limits
        int newVolume = (newMax.getBlockX() - newMin.getBlockX() + 1) *
                       (newMax.getBlockY() - newMin.getBlockY() + 1) *
                       (newMax.getBlockZ() - newMin.getBlockZ() + 1);
        
        int maxVolume = player.hasPermission("frizzlengaurd.admin") ? 
                Integer.MAX_VALUE : plugin.getConfig().getInt("max-region-volume", 1000000);
        
        if (newVolume > maxVolume) {
            sender.sendMessage(ChatColor.RED + "The resized region would be too large. Maximum volume: " + maxVolume);
            return true;
        }
        
        try {
            // Create new region with same properties but new bounds
            Region newRegion = new Region(region.getName(), newMin, newMax);
            newRegion.setOwners(region.getOwners());
            newRegion.setMembers(region.getMembers());
            newRegion.setFlags(region.getFlags());
            
            // Remove old region and add new one
            plugin.getRegionManager().removeRegion(region.getName());
            plugin.getRegionManager().addRegion(newRegion);
            
            // Save changes
            plugin.getDataManager().saveData();
            
            sender.sendMessage(ChatColor.GREEN + "Successfully resized region '" + regionName + 
                    "' " + direction + " by " + amount + " blocks.");
            
            // Show preview of resized region
            plugin.getVisualsManager().showRegionBoundaries(player, newRegion, 200);
            
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Failed to resize region: " + e.getMessage());
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
            // Tab complete region names that the player owns
            String partial = args[0].toLowerCase();
            
            return plugin.getRegionManager().getAllRegions().stream()
                    .filter(region -> region.isOwner(player) || 
                                    player.hasPermission("frizzlengaurd.admin"))
                    .map(Region::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Tab complete directions
            String partial = args[1].toLowerCase();
            List<String> directions = Arrays.asList("north", "south", "east", "west", "up", "down");
            
            return directions.stream()
                    .filter(dir -> dir.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            // Suggest some common amounts
            String partial = args[2];
            List<String> amounts = Arrays.asList("1", "5", "10", "25", "50", "100");
            
            return amounts.stream()
                    .filter(amt -> amt.startsWith(partial))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 