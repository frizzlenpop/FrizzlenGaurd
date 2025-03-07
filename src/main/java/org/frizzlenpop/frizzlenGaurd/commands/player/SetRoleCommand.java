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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SetRoleCommand extends AbstractCommand {
    
    public SetRoleCommand(FrizzlenGaurd plugin) {
        super(plugin, "setrole", "Changes a player's role in your region", 
              "/fg setrole <regionName> <player> <role>", "frizzlengaurd.modify");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("role", "changerole");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        Player player = getPlayer(sender);
        UUID playerId = player.getUniqueId();
        
        String regionName = args[0];
        String friendName = args[1];
        String roleName = args[2];
        
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
        
        // Check if player has permission to modify roles in this region
        Region.Role playerRole = targetRegion.getMemberRole(playerId);
        if (!playerRole.canModifyFlags() && !player.hasPermission("frizzlengaurd.admin.*")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to change roles in this region.");
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
            for (Map.Entry<UUID, Region.Role> entry : targetRegion.getMembers().entrySet()) {
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
        
        // Check if the player is the owner (can't change owner's role)
        if (targetUUID.equals(targetRegion.getOwner()) && !player.hasPermission("frizzlengaurd.admin.*")) {
            sender.sendMessage(ChatColor.RED + "You cannot change the role of the owner.");
            return true;
        }
        
        // Check if the player is even in the region
        if (!targetRegion.isMember(targetUUID)) {
            String message = getMessage("friend.not-friend", "&f%player% &cis not added to this claim.");
            message = message.replace("%player%", targetPlayer.getName());
            sender.sendMessage(message);
            return true;
        }
        
        // Parse the role
        Region.Role newRole = Region.Role.valueOf(roleName.toUpperCase());
        
        // Don't allow setting as OWNER
        if (newRole == Region.Role.OWNER && !player.hasPermission("frizzlengaurd.admin.*")) {
            sender.sendMessage(ChatColor.RED + "You cannot set someone as OWNER. Use MEMBER instead.");
            return true;
        }
        
        // Get the current role
        Region.Role currentRole = targetRegion.getMemberRole(targetUUID);
        
        // Check if the role is already set
        if (currentRole == newRole) {
            sender.sendMessage(ChatColor.RED + targetPlayer.getName() + " already has the role " + newRole.name() + ".");
            return true;
        }
        
        // Set the new role
        targetRegion.addMember(targetUUID, newRole);
        
        // Log the action
        targetRegion.addLogEntry(new LogEntry(player, LogEntry.LogAction.MEMBER_ADD, 
                "Changed role of " + targetPlayer.getName() + " from " + currentRole.name() + " to " + newRole.name(), null));
        
        // Send success message
        String message = getMessage("friend.role-changed", "&aChanged &f%player%'s &arole to &f%role%&a.");
        message = message.replace("%player%", targetPlayer.getName())
                       .replace("%role%", newRole.name());
        sender.sendMessage(message);
        
        // Notify the target player if they are online
        if (targetPlayer.isOnline()) {
            Player onlineTarget = targetPlayer.getPlayer();
            onlineTarget.sendMessage(ChatColor.GREEN + "Your role in " + player.getName() + 
                    "'s region '" + regionName + "' has been changed to " + newRole.name() + ".");
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
        } else if (args.length == 3) {
            // Tab complete role names
            String partialRole = args[2].toLowerCase();
            return Arrays.stream(Region.Role.values())
                    .filter(role -> role != Region.Role.OWNER || player.hasPermission("frizzlengaurd.admin.*"))
                    .map(Region.Role::name)
                    .filter(name -> name.toLowerCase().startsWith(partialRole))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 