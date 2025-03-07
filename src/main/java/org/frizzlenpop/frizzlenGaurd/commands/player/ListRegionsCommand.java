package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListRegionsCommand extends AbstractCommand {
    
    public ListRegionsCommand(FrizzlenGaurd plugin) {
        super(plugin, "listregions", "Lists regions you own or have access to", 
              "/fg listregions [player]", "frizzlengaurd.list");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("list", "regions", "ls");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        UUID targetUUID;
        String targetName;
        
        if (args.length == 0) {
            // If no arguments, show the sender's regions
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player name.");
                return true;
            }
            
            Player player = (Player) sender;
            targetUUID = player.getUniqueId();
            targetName = player.getName();
        } else {
            // If argument is provided, try to find that player
            String playerName = args[0];
            
            // Only allow targeting other players if admin
            if (!sender.hasPermission("frizzlengaurd.admin.*") && sender instanceof Player && 
                    !playerName.equalsIgnoreCase(((Player) sender).getName())) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to view other players' regions.");
                return true;
            }
            
            OfflinePlayer targetPlayer = null;
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(playerName)) {
                    targetPlayer = offlinePlayer;
                    break;
                }
            }
            
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found.");
                return true;
            }
            
            targetUUID = targetPlayer.getUniqueId();
            targetName = targetPlayer.getName();
        }
        
        // Get player's regions
        List<Region> ownedRegions = plugin.getRegionManager().getPlayerRegions(targetUUID);
        
        if (ownedRegions.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + targetName + " does not own any regions.");
        } else {
            sender.sendMessage(ChatColor.GOLD + "===== " + targetName + "'s Regions =====");
            for (Region region : ownedRegions) {
                displayRegionSummary(sender, region);
            }
        }
        
        // If looking at own regions, also show regions the player is a member of
        if (sender instanceof Player && ((Player) sender).getUniqueId().equals(targetUUID)) {
            List<Region> memberRegions = new ArrayList<>();
            
            for (Region region : plugin.getRegionManager().getAllRegions()) {
                if (!region.getOwner().equals(targetUUID) && region.isMember(targetUUID)) {
                    memberRegions.add(region);
                }
            }
            
            if (!memberRegions.isEmpty()) {
                sender.sendMessage(ChatColor.GOLD + "===== Regions You're a Member Of =====");
                for (Region region : memberRegions) {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(region.getOwner());
                    String ownerName = owner.getName() != null ? owner.getName() : "Unknown";
                    
                    sender.sendMessage(ChatColor.YELLOW + region.getName() + ChatColor.GRAY + 
                            " - Owner: " + ChatColor.WHITE + ownerName + ChatColor.GRAY + 
                            ", Role: " + ChatColor.YELLOW + 
                            region.getMemberRole(targetUUID).name());
                }
            }
        }
        
        return true;
    }
    
    private void displayRegionSummary(CommandSender sender, Region region) {
        int size = region.getVolume();
        int mainArea = (region.getMaxX() - region.getMinX() + 1) * (region.getMaxZ() - region.getMinZ() + 1);
        
        // Calculate member count excluding owner
        long memberCount = region.getMembers().size();
        if (region.getMembers().containsKey(region.getOwner())) {
            memberCount--;
        }
        
        sender.sendMessage(ChatColor.YELLOW + region.getName() + ChatColor.GRAY + 
                " - World: " + ChatColor.WHITE + region.getWorldName() + ChatColor.GRAY + 
                ", Size: " + ChatColor.WHITE + mainArea + ChatColor.GRAY + 
                ", Members: " + ChatColor.WHITE + memberCount + ChatColor.GRAY + 
                ", Subregions: " + ChatColor.WHITE + region.getSubregions().size());
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("frizzlengaurd.admin.*")) {
            // Tab complete player names for admins
            String partialName = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialName))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 