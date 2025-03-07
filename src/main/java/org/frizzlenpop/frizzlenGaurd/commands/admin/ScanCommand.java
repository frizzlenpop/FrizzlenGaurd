package org.frizzlenpop.frizzlenGaurd.commands.admin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.utils.RegionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ScanCommand extends AbstractCommand {
    
    public ScanCommand(FrizzlenGaurd plugin) {
        super(plugin, "scan", "Scan for problematic regions", 
              "/fg scan [type] [radius]", "frizzlengaurd.admin.scan");
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
        String type = args.length > 0 ? args[0].toLowerCase() : "all";
        int radius = args.length > 1 ? Math.max(1, Math.min(1000, Integer.parseInt(args[1]))) : 100;
        
        Location playerLoc = player.getLocation();
        Collection<Region> allRegions = plugin.getRegionManager().getAllRegions();
        List<Region> nearbyRegions = allRegions.stream()
                .filter(region -> region.getWorld().equals(playerLoc.getWorld()))
                .filter(region -> isWithinRadius(region, playerLoc, radius))
                .collect(Collectors.toList());
        
        if (nearbyRegions.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No regions found within " + radius + " blocks.");
            return true;
        }
        
        List<String> issues = new ArrayList<>();
        
        switch (type) {
            case "overlap":
                findOverlappingRegions(nearbyRegions, issues);
                break;
            case "orphaned":
                findOrphanedRegions(nearbyRegions, issues);
                break;
            case "empty":
                findEmptyRegions(nearbyRegions, issues);
                break;
            case "all":
                findOverlappingRegions(nearbyRegions, issues);
                findOrphanedRegions(nearbyRegions, issues);
                findEmptyRegions(nearbyRegions, issues);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid scan type. Use: overlap, orphaned, empty, or all");
                return true;
        }
        
        if (issues.isEmpty()) {
            sender.sendMessage(ChatColor.GREEN + "No issues found in " + nearbyRegions.size() + 
                    " regions within " + radius + " blocks.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Found " + issues.size() + " issues in " + 
                    nearbyRegions.size() + " regions within " + radius + " blocks:");
            issues.forEach(sender::sendMessage);
        }
        
        return true;
    }
    
    private boolean isWithinRadius(Region region, Location center, int radius) {
        Location min = region.getMinPoint();
        Location max = region.getMaxPoint();
        
        // Check if any corner of the region is within radius
        return isPointWithinRadius(min, center, radius) ||
               isPointWithinRadius(max, center, radius) ||
               isPointWithinRadius(new Location(min.getWorld(), min.getX(), min.getY(), max.getZ()), center, radius) ||
               isPointWithinRadius(new Location(min.getWorld(), min.getX(), max.getY(), min.getZ()), center, radius) ||
               isPointWithinRadius(new Location(min.getWorld(), max.getX(), min.getY(), min.getZ()), center, radius) ||
               isPointWithinRadius(new Location(min.getWorld(), min.getX(), max.getY(), max.getZ()), center, radius) ||
               isPointWithinRadius(new Location(min.getWorld(), max.getX(), min.getY(), max.getZ()), center, radius) ||
               isPointWithinRadius(new Location(min.getWorld(), max.getX(), max.getY(), min.getZ()), center, radius);
    }
    
    private boolean isPointWithinRadius(Location point, Location center, int radius) {
        return point.distanceSquared(center) <= radius * radius;
    }
    
    private void findOverlappingRegions(List<Region> regions, List<String> issues) {
        for (int i = 0; i < regions.size(); i++) {
            Region region1 = regions.get(i);
            for (int j = i + 1; j < regions.size(); j++) {
                Region region2 = regions.get(j);
                if (RegionUtils.doRegionsOverlap(region1, region2)) {
                    issues.add(ChatColor.RED + "Overlap detected between regions '" + 
                            region1.getName() + "' and '" + region2.getName() + "'");
                }
            }
        }
    }
    
    private void findOrphanedRegions(List<Region> regions, List<String> issues) {
        for (Region region : regions) {
            boolean hasValidOwner = false;
            for (UUID ownerId : region.getOwners().keySet()) {
                if (plugin.getServer().getOfflinePlayer(ownerId).hasPlayedBefore()) {
                    hasValidOwner = true;
                    break;
                }
            }
            if (!hasValidOwner) {
                issues.add(ChatColor.YELLOW + "Orphaned region '" + region.getName() + 
                        "' has no valid owners");
            }
        }
    }
    
    private void findEmptyRegions(List<Region> regions, List<String> issues) {
        for (Region region : regions) {
            if (region.getOwners().isEmpty() && region.getMembers().isEmpty()) {
                issues.add(ChatColor.YELLOW + "Empty region '" + region.getName() + 
                        "' has no owners or members");
            }
        }
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> types = Arrays.asList("all", "overlap", "orphaned", "empty");
            
            return types.stream()
                    .filter(type -> type.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            String partial = args[1];
            List<String> radii = Arrays.asList("50", "100", "250", "500", "1000");
            
            return radii.stream()
                    .filter(r -> r.startsWith(partial))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 