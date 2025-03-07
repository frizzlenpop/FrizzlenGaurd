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
import java.util.UUID;
import java.util.stream.Collectors;

public class AddFriendCommand extends AbstractCommand {
    
    public AddFriendCommand(FrizzlenGaurd plugin) {
        super(plugin, "addfriend", "Adds a player to your region", 
              "/fg addfriend <regionName> <player> [role]", "frizzlengaurd.addfriend");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("add", "invite");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        Player player = getPlayer(sender);
        UUID playerId = player.getUniqueId();
        
        String regionName = args[0];
        String friendName = args[1];
        String roleName = args.length == 3 ? args[2] : "visitor";
        
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
        
        // Check if player has permission to add friends to this region
        Role playerRole = targetRegion.getMemberRole(playerId);
        if (!playerRole.canBuild() && !player.hasPermission("frizzlengaurd.admin.*")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to add friends to this region.");
            return true;
        }
        
        // Look up the target player
        OfflinePlayer targetPlayer = null;
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(friendName)) {
                targetPlayer = offlinePlayer;
                break;
            }
        }
        
        if (targetPlayer == null) {
            String message = getMessage("friend.player-not-found", "&cPlayer &f%player% &cnot found.");
            message = message.replace("%player%", friendName);
            sender.sendMessage(message);
            return true;
        }
        
        // Check if the player is already a member
        if (targetRegion.isMember(targetPlayer.getUniqueId())) {
            String message = getMessage("friend.already-friend", "&f%player% &cis already added to this claim.");
            message = message.replace("%player%", targetPlayer.getName());
            sender.sendMessage(message);
            return true;
        }
        
        // Parse the role
        Role role = Role.valueOf(roleName.toUpperCase());
        
        // Don't allow adding as OWNER
        if (role == Role.OWNER && !player.hasPermission("frizzlengaurd.admin.*")) {
            sender.sendMessage(ChatColor.RED + "You cannot add someone as OWNER. Use CO_OWNER instead.");
            return true;
        }
        
        // Add friend to region
        targetRegion.addMember(targetPlayer.getUniqueId(), role);
        
        // Log the action
        targetRegion.addLogEntry(new LogEntry(player, LogEntry.LogAction.MEMBER_ADD, 
                "Added " + targetPlayer.getName() + " as " + role.name(), null));
        
        // Send success message
        String message = getMessage("friend.friend-added", "&aAdded &f%player% &ato your claim with role &f%role%&a.");
        message = message.replace("%player%", targetPlayer.getName())
                       .replace("%role%", role.name());
        sender.sendMessage(message);
        
        // Notify the target player if they are online
        if (targetPlayer.isOnline()) {
            Player onlineTarget = targetPlayer.getPlayer();
            onlineTarget.sendMessage(ChatColor.GREEN + "You have been added to " + player.getName() + 
                    "'s region '" + regionName + "' with role " + role.name() + ".");
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
            // Tab complete online player names
            String partialName = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            // Tab complete role names
            String partialRole = args[2].toLowerCase();
            return Arrays.stream(Role.values())
                    .filter(role -> role != Role.OWNER || player.hasPermission("frizzlengaurd.admin.*"))
                    .map(Role::name)
                    .filter(name -> name.toLowerCase().startsWith(partialRole))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 