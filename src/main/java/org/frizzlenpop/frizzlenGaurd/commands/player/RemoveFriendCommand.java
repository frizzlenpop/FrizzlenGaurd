package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.models.LogEntry;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.models.Region.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RemoveFriendCommand extends AbstractCommand {
    
    public RemoveFriendCommand(FrizzlenGaurd plugin) {
        super(plugin, "removefriend", "Removes a player from your region", 
              "/fg removefriend <regionName> <player>", "frizzlengaurd.removefriend");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("remove", "kick");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        Player player = getPlayer(sender);
        UUID playerId = player.getUniqueId();
        
        String regionName = args[0];
        String friendName = args[1];
        
        // Get the region
        List<Region> playerRegions = plugin.getRegionManager().getPlayerRegions(playerId);
        Region targetRegion = null;
        
        for (Region region : playerRegions) {
            if (region.getName().equalsIgnoreCase(regionName)) {
                targetRegion = region;
                break;
            }
        }
        
        if (targetRegion == null) {
            sender.sendMessage(ChatColor.RED + "You don't have a region named '" + regionName + "'.");
            return true;
        }
        
        // Check if player has permission to remove friends from this region
        Role playerRole = targetRegion.getMemberRole(playerId);
        if (!playerRole.canBuild() && !player.hasPermission("frizzlengaurd.admin.*")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to remove friends from this region.");
            return true;
        }
        
        // Look up the target player
        OfflinePlayer targetPlayer = null;
        UUID targetUUID = null;
        
        // First try to find by exact username
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(friendName)) {
                targetPlayer = offlinePlayer;
                targetUUID = offlinePlayer.getUniqueId();
                break;
            }
        }
        
        // If not found by name, check if any members match the name
        if (targetPlayer == null) {
            for (Map.Entry<UUID, Role> entry : targetRegion.getMembers().entrySet()) {
                OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(entry.getKey());
                if (memberPlayer.getName() != null && memberPlayer.getName().equalsIgnoreCase(friendName)) {
                    targetPlayer = memberPlayer;
                    targetUUID = memberPlayer.getUniqueId();
                    break;
                }
            }
        }
        
        if (targetPlayer == null) {
            String message = getMessage("friend.player-not-found", "&cPlayer &f%player% &cnot found.");
            message = message.replace("%player%", friendName);
            sender.sendMessage(message);
            return true;
        }
        
        // Check if the player is the owner (can't remove the owner)
        if (targetUUID.equals(targetRegion.getOwner()) && !player.hasPermission("frizzlengaurd.admin.*")) {
            sender.sendMessage(ChatColor.RED + "You cannot remove the owner of the region.");
            return true;
        }
        
        // Check if the player is even in the region
        if (!targetRegion.isMember(targetUUID)) {
            String message = getMessage("friend.not-friend", "&f%player% &cis not added to this claim.");
            message = message.replace("%player%", targetPlayer.getName());
            sender.sendMessage(message);
            return true;
        }
        
        // Check if player's role is high enough to remove the target
        Role targetRole = targetRegion.getMemberRole(targetUUID);
        if (targetRole == Role.OWNER && playerRole != Role.OWNER && !player.hasPermission("frizzlengaurd.admin.*")) {
            sender.sendMessage(ChatColor.RED + "You cannot remove an Owner. Only admins can do that.");
            return true;
        }
        
        // Remove friend from region
        targetRegion.removeMember(targetUUID);
        
        // Log the action
        targetRegion.addLogEntry(new LogEntry(player, LogEntry.LogAction.MEMBER_REMOVE, 
                "Removed " + targetPlayer.getName(), null));
        
        // Send success message
        String message = getMessage("friend.friend-removed", "&aRemoved &f%player% &afrom your claim.");
        message = message.replace("%player%", targetPlayer.getName());
        sender.sendMessage(message);
        
        // Notify the target player if they are online
        if (targetPlayer.isOnline()) {
            Player onlineTarget = targetPlayer.getPlayer();
            onlineTarget.sendMessage(ChatColor.YELLOW + "You have been removed from " + player.getName() + 
                    "'s region '" + regionName + "'.");
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
            // Tab complete the region name
            List<Region> playerRegions = plugin.getRegionManager().getPlayerRegions(player.getUniqueId());
            return playerRegions.stream()
                    .map(Region::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Tab complete members of the region
            String regionName = args[0];
            String partialName = args[1].toLowerCase();
            
            // Find the region
            Region targetRegion = null;
            for (Region region : plugin.getRegionManager().getPlayerRegions(player.getUniqueId())) {
                if (region.getName().equalsIgnoreCase(regionName)) {
                    targetRegion = region;
                    break;
                }
            }
            
            if (targetRegion != null) {
                List<String> members = new ArrayList<>();
                
                // Check each member
                for (UUID memberId : targetRegion.getMembers().keySet()) {
                    OfflinePlayer memberPlayer = Bukkit.getOfflinePlayer(memberId);
                    if (memberPlayer.getName() != null && 
                            memberPlayer.getName().toLowerCase().startsWith(partialName)) {
                        members.add(memberPlayer.getName());
                    }
                }
                
                return members;
            }
        }
        
        return new ArrayList<>();
    }
} 