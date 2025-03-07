package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.models.LogEntry;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SubclaimCommand extends AbstractCommand {
    private final Map<UUID, Location> firstPoints = new ConcurrentHashMap<>();
    private final Map<UUID, Location> secondPoints = new ConcurrentHashMap<>();
    
    public SubclaimCommand(FrizzlenGaurd plugin) {
        super(plugin, "subclaim", "Creates a new subregion within an existing claim", 
              "/fg subclaim <parentName> <name>", "frizzlengaurd.subclaim");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("sub", "createsubclaim");
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
        
        // Check if world is excluded
        if (plugin.getRegionManager().isWorldExcluded(player.getWorld().getName())) {
            sender.sendMessage(getMessage("general.world-excluded", "&cLand claiming is disabled in this world."));
            return true;
        }
        
        // Check the player's equipped item - empty hand in main hand or stick for selection
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null && mainHand.getType() != Material.AIR && mainHand.getType() != Material.STICK) {
            sender.sendMessage(ChatColor.RED + "You must have an empty hand or a stick to make a selection.");
            return true;
        }
        
        // If player has stick or empty hand and no arguments, select a point
        if (args.length == 0) {
            // This is a selection command
            if (!firstPoints.containsKey(playerId)) {
                // First point
                firstPoints.put(playerId, player.getLocation().getBlock().getLocation());
                String message = getMessage("claim.first-point-set", "&aFirst point set at &f%x%&a, &f%y%&a, &f%z%&a.");
                message = message.replace("%x%", String.valueOf(player.getLocation().getBlockX()))
                               .replace("%y%", String.valueOf(player.getLocation().getBlockY()))
                               .replace("%z%", String.valueOf(player.getLocation().getBlockZ()));
                sender.sendMessage(message);
                return true;
            } else if (!secondPoints.containsKey(playerId)) {
                // Second point
                secondPoints.put(playerId, player.getLocation().getBlock().getLocation());
                String message = getMessage("claim.second-point-set", "&aSecond point set at &f%x%&a, &f%y%&a, &f%z%&a.");
                message = message.replace("%x%", String.valueOf(player.getLocation().getBlockX()))
                               .replace("%y%", String.valueOf(player.getLocation().getBlockY()))
                               .replace("%z%", String.valueOf(player.getLocation().getBlockZ()));
                sender.sendMessage(message);
                sender.sendMessage(ChatColor.YELLOW + "Use /fg subclaim <parentName> <name> to create the subregion.");
                return true;
            } else {
                // Reset points
                firstPoints.remove(playerId);
                secondPoints.remove(playerId);
                sender.sendMessage(ChatColor.YELLOW + "Selection reset. Select two points and then use /fg subclaim <parentName> <name>.");
                return true;
            }
        }
        
        // Check if they provided the parent region and name
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /fg subclaim <parentName> <name>");
            return true;
        }
        
        String parentName = args[0];
        String name = args[1];
        
        // Check if player has selected two points
        if (!firstPoints.containsKey(playerId) || !secondPoints.containsKey(playerId)) {
            sender.sendMessage(ChatColor.RED + "You must select two points first using your empty hand or a stick.");
            return true;
        }
        
        // Check if the points are in the same world
        if (!firstPoints.get(playerId).getWorld().equals(secondPoints.get(playerId).getWorld())) {
            sender.sendMessage(ChatColor.RED + "Both selection points must be in the same world.");
            firstPoints.remove(playerId);
            secondPoints.remove(playerId);
            return true;
        }
        
        // Find the parent region
        List<Region> playerRegions = plugin.getRegionManager().getPlayerRegions(playerId);
        Region parentRegion = null;
        
        for (Region region : playerRegions) {
            if (region.getName().equalsIgnoreCase(parentName)) {
                parentRegion = region;
                break;
            }
        }
        
        if (parentRegion == null) {
            sender.sendMessage(ChatColor.RED + "You don't have a region named '" + parentName + "'.");
            return true;
        }
        
        // Try to create the subregion
        Location pos1 = firstPoints.get(playerId);
        Location pos2 = secondPoints.get(playerId);
        
        // Check if positions are within the parent region
        if (!parentRegion.contains(pos1) || !parentRegion.contains(pos2)) {
            sender.sendMessage(getMessage("subregion.not-in-parent", 
                   "&cYou must create a subregion within your main claim."));
            return true;
        }
        
        Region subregion = plugin.getRegionManager().createSubregion(player, parentRegion, name, pos1, pos2);
        
        if (subregion == null) {
            // Check specific reasons why the region couldn't be created
            if (!plugin.getRegionManager().canCreateSubregion(player, parentRegion, pos1, pos2)) {
                // Check subregion limit
                int maxSubregions = plugin.getConfigManager().getMainConfig().getInt("claims.max-subregions-per-claim", 5);
                if (parentRegion.getSubregions().size() >= maxSubregions) {
                    String message = getMessage("subregion.subregion-limit-reached", 
                            "&cYou have reached the maximum number of subregions for this claim (&f%limit%&c).");
                    message = message.replace("%limit%", String.valueOf(maxSubregions));
                    sender.sendMessage(message);
                    return true;
                }
                
                // Check for overlapping subregions
                sender.sendMessage(ChatColor.RED + "This subregion overlaps with an existing subregion.");
                return true;
            }
            
            // Generic error
            sender.sendMessage(ChatColor.RED + "Failed to create subregion. Please try again.");
            Logger.error("Failed to create subregion for player " + player.getName());
            return true;
        }
        
        // Subregion created successfully
        String message = getMessage("subregion.subregion-created", "&aSuccessfully created subregion &f%name%&a.");
        message = message.replace("%name%", name);
        sender.sendMessage(message);
        
        // Visualize the region boundaries
        plugin.getVisualsManager().showRegionBoundaries(player, subregion);
        
        // Clean up selection points
        firstPoints.remove(playerId);
        secondPoints.remove(playerId);
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            // Tab complete the parent region name
            List<Region> playerRegions = plugin.getRegionManager().getPlayerRegions(player.getUniqueId());
            return playerRegions.stream()
                    .map(Region::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Get the first selection point for a player
     * 
     * @param playerId The player's UUID
     * @return The first point or null if not set
     */
    public Location getFirstPoint(UUID playerId) {
        return firstPoints.get(playerId);
    }
    
    /**
     * Get the second selection point for a player
     * 
     * @param playerId The player's UUID
     * @return The second point or null if not set
     */
    public Location getSecondPoint(UUID playerId) {
        return secondPoints.get(playerId);
    }
    
    /**
     * Clear the selection points for a player
     * 
     * @param playerId The player's UUID
     */
    public void clearPoints(UUID playerId) {
        firstPoints.remove(playerId);
        secondPoints.remove(playerId);
    }
} 