package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.models.Role;

import java.util.*;
import java.util.stream.Collectors;

public class RegionInfoCommand extends AbstractCommand {
    
    public RegionInfoCommand(FrizzlenGaurd plugin) {
        super(plugin, "regioninfo", "Shows information about a region", 
              "/fg regioninfo [regionName]", "frizzlengaurd.info");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("info", "i");
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
        UUID playerId = player.getUniqueId();
        
        // If no region name specified, get info on the region the player is standing in
        if (args.length == 0) {
            Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
            
            if (region == null) {
                sender.sendMessage(getMessage("claim.no-claim-here", "&cThere is no claim here."));
                return true;
            }
            
            displayRegionInfo(player, region);
            return true;
        }
        
        // Get region by name
        String regionName = args[0];
        
        // If the player is admin, search all regions
        boolean isAdmin = player.hasPermission("frizzlengaurd.admin.*");
        Region targetRegion = null;
        
        if (isAdmin) {
            // Search all regions
            for (Region region : plugin.getRegionManager().getAllRegions()) {
                if (region.getName().equalsIgnoreCase(regionName)) {
                    targetRegion = region;
                    break;
                }
            }
        } else {
            // Search only player's regions and regions they have access to
            for (Region region : plugin.getRegionManager().getAllRegions()) {
                if (region.getName().equalsIgnoreCase(regionName) && 
                        (region.getOwner().equals(playerId) || region.isMember(playerId))) {
                    targetRegion = region;
                    break;
                }
            }
        }
        
        if (targetRegion == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found or you don't have access to it.");
            return true;
        }
        
        displayRegionInfo(player, targetRegion);
        return true;
    }
    
    private void displayRegionInfo(Player player, Region region) {
        player.sendMessage(ChatColor.GOLD + "===== Region: " + ChatColor.WHITE + region.getName() + ChatColor.GOLD + " =====");
        
        // Basic info
        OfflinePlayer owner = Bukkit.getOfflinePlayer(region.getOwner());
        String ownerName = owner.getName() != null ? owner.getName() : "Unknown";
        
        player.sendMessage(ChatColor.YELLOW + "Owner: " + ChatColor.WHITE + ownerName);
        player.sendMessage(ChatColor.YELLOW + "World: " + ChatColor.WHITE + region.getWorldName());
        player.sendMessage(ChatColor.YELLOW + "Corners: " + ChatColor.WHITE + 
                region.getMinX() + "," + region.getMinY() + "," + region.getMinZ() + " to " + 
                region.getMaxX() + "," + region.getMaxY() + "," + region.getMaxZ());
        player.sendMessage(ChatColor.YELLOW + "Size: " + ChatColor.WHITE + region.getVolume() + " blocks" + 
                " (" + (region.getMaxX() - region.getMinX() + 1) + "x" + 
                (region.getMaxY() - region.getMinY() + 1) + "x" + 
                (region.getMaxZ() - region.getMinZ() + 1) + ")");
        
        // Parent/child info
        if (region.isSubregion()) {
            player.sendMessage(ChatColor.YELLOW + "Parent: " + ChatColor.WHITE + region.getParent().getName());
        }
        if (!region.getSubregions().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Subregions: " + ChatColor.WHITE + 
                    region.getSubregions().stream()
                            .map(Region::getName)
                            .collect(Collectors.joining(", ")));
        }
        
        // Members
        if (!region.getMembers().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Members:");
            
            // Group by role
            Map<Region.Role, List<String>> membersByRole = new HashMap<>();
            
            for (Map.Entry<UUID, Region.Role> entry : region.getMembers().entrySet()) {
                UUID memberId = entry.getKey();
                Region.Role role = entry.getValue();
                
                // Skip owner as they're shown separately
                if (memberId.equals(region.getOwner())) {
                    continue;
                }
                
                OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);
                String memberName = member.getName() != null ? member.getName() : memberId.toString().substring(0, 8);
                
                membersByRole.computeIfAbsent(role, k -> new ArrayList<>()).add(memberName);
            }
            
            // Sort roles by hierarchy (highest first)
            List<Region.Role> sortedRoles = Arrays.asList(Region.Role.OWNER, Region.Role.MEMBER, Region.Role.VISITOR);
            
            for (Region.Role role : sortedRoles) {
                List<String> members = membersByRole.get(role);
                if (members != null && !members.isEmpty()) {
                    player.sendMessage("  " + ChatColor.YELLOW + role.name() + ": " + 
                            ChatColor.WHITE + String.join(", ", members));
                }
            }
        }
        
        // Flags
        if (!region.getFlags().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Flags:");
            
            // Sort flags alphabetically
            List<Map.Entry<String, Boolean>> sortedFlags = new ArrayList<>(region.getFlags().entrySet());
            sortedFlags.sort(Map.Entry.comparingByKey());
            
            for (Map.Entry<String, Boolean> entry : sortedFlags) {
                String flag = entry.getKey();
                boolean value = entry.getValue();
                
                player.sendMessage("  " + flag + ": " + 
                        (value ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            }
        }
        
        // Visualize the region
        plugin.getVisualsManager().showRegionBoundaries(player, region);
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
                // Regular players can only see their own regions or regions they're a member of
                regions = plugin.getRegionManager().getAllRegions().stream()
                        .filter(region -> region.getOwner().equals(playerId) || region.isMember(playerId))
                        .map(Region::getName)
                        .filter(name -> name.toLowerCase().startsWith(partialName))
                        .collect(Collectors.toList());
            }
            
            return regions;
        }
        
        return new ArrayList<>();
    }
} 