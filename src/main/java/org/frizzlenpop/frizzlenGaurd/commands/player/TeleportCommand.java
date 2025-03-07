package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeleportCommand extends AbstractCommand {
    
    public TeleportCommand(FrizzlenGaurd plugin) {
        super(plugin, "tp", "Teleport to a region", 
              "/fg tp <region>", "frizzlengaurd.teleport");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        Player player = getPlayer(sender);
        String regionName = args[0];
        
        // Find the region
        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found.");
            return true;
        }
        
        // Check if player can teleport to the region
        if (!canTeleportTo(player, region)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to teleport to this region.");
            return true;
        }
        
        // Get safe teleport location
        Location teleportLocation = findSafeLocation(region);
        if (teleportLocation == null) {
            sender.sendMessage(ChatColor.RED + "Could not find a safe location to teleport to in this region.");
            return true;
        }
        
        // Teleport the player
        player.teleport(teleportLocation);
        player.sendMessage(ChatColor.GREEN + "Teleported to region '" + regionName + "'.");
        
        return true;
    }
    
    private boolean canTeleportTo(Player player, Region region) {
        // Admin can teleport anywhere
        if (player.hasPermission("frizzlengaurd.admin")) {
            return true;
        }
        
        // Owner can always teleport
        if (region.isOwner(player)) {
            return true;
        }
        
        // Member can teleport if they have the permission
        if (region.getMembers().containsKey(player.getUniqueId())) {
            return player.hasPermission("frizzlengaurd.teleport.member");
        }
        
        // Others can teleport if they have the permission and the region allows it
        return player.hasPermission("frizzlengaurd.teleport.others") && 
               region.getFlags().getOrDefault("allow-teleport", false);
    }
    
    private Location findSafeLocation(Region region) {
        Location min = region.getMinPoint();
        Location max = region.getMaxPoint();
        
        // Try to find the highest safe block near the center of the region
        int centerX = (min.getBlockX() + max.getBlockX()) / 2;
        int centerZ = (min.getBlockZ() + max.getBlockZ()) / 2;
        
        Location center = new Location(region.getWorld(), centerX, max.getBlockY(), centerZ);
        
        // Start from the top and work down
        for (int y = max.getBlockY(); y >= min.getBlockY(); y--) {
            center.setY(y);
            
            if (isSafeLocation(center)) {
                // Move to center of block and add 0.5 to Y for smooth landing
                return center.clone().add(0.5, 0.5, 0.5);
            }
        }
        
        return null;
    }
    
    private boolean isSafeLocation(Location location) {
        // Check if the two blocks for the player are safe (feet and head level)
        return !location.getBlock().getType().isSolid() &&
               !location.clone().add(0, 1, 0).getBlock().getType().isSolid() &&
               location.clone().subtract(0, 1, 0).getBlock().getType().isSolid();
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            
            return plugin.getRegionManager().getAllRegions().stream()
                    .filter(region -> canTeleportTo(player, region))
                    .map(Region::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 