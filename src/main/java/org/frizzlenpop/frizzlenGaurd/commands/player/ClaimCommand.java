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
import org.frizzlenpop.frizzlenGaurd.utils.EconomyHandler;
import org.frizzlenpop.frizzlenGaurd.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClaimCommand extends AbstractCommand {
    private final Map<UUID, Location> firstPoints = new ConcurrentHashMap<>();
    private final Map<UUID, Location> secondPoints = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> previewMode = new ConcurrentHashMap<>();
    
    public ClaimCommand(FrizzlenGaurd plugin) {
        super(plugin, "claim", "Creates a new region claim", "/fg claim <name>", "frizzlengaurd.claim");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("c", "create");
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
        
        // Toggle preview mode with "preview" argument
        if (args.length == 1 && args[0].equalsIgnoreCase("preview")) {
            boolean currentPreview = previewMode.getOrDefault(playerId, false);
            previewMode.put(playerId, !currentPreview);
            
            if (previewMode.get(playerId)) {
                sender.sendMessage(ChatColor.GREEN + "Preview mode enabled. Your selection will be visualized in real-time.");
                
                // If both points are set, show preview
                if (firstPoints.containsKey(playerId) && secondPoints.containsKey(playerId)) {
                    showPreview(player);
                }
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Preview mode disabled.");
                plugin.getVisualsManager().cancelPreview(player);
            }
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
                
                // Let them know about preview mode
                sender.sendMessage(ChatColor.YELLOW + "You can enable preview mode with " + ChatColor.WHITE + "/fg claim preview");
                
                return true;
            } else if (!secondPoints.containsKey(playerId)) {
                // Second point
                secondPoints.put(playerId, player.getLocation().getBlock().getLocation());
                String message = getMessage("claim.second-point-set", "&aSecond point set at &f%x%&a, &f%y%&a, &f%z%&a.");
                message = message.replace("%x%", String.valueOf(player.getLocation().getBlockX()))
                               .replace("%y%", String.valueOf(player.getLocation().getBlockY()))
                               .replace("%z%", String.valueOf(player.getLocation().getBlockZ()));
                sender.sendMessage(message);
                
                // Show claim information (size, cost, etc.)
                displayClaimInfo(player);
                
                // Show preview if enabled
                if (previewMode.getOrDefault(playerId, false)) {
                    showPreview(player);
                }
                
                sender.sendMessage(ChatColor.YELLOW + "Use /fg claim <name> to create the region.");
                return true;
            } else {
                // Reset points
                firstPoints.remove(playerId);
                secondPoints.remove(playerId);
                
                // Cancel any preview visualization
                plugin.getVisualsManager().cancelPreview(player);
                
                sender.sendMessage(ChatColor.YELLOW + "Selection reset. Select two points and then use /fg claim <name>.");
                return true;
            }
        }
        
        // Creating a claim with a name
        if (args.length == 1) {
            String name = args[0];
            
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
            
            // Try to create the region
            Location pos1 = firstPoints.get(playerId);
            Location pos2 = secondPoints.get(playerId);
            
            Region region = plugin.getRegionManager().createRegion(player, name, pos1, pos2);
            
            if (region == null) {
                // Check specific reasons why the region couldn't be created
                if (!plugin.getRegionManager().canCreateRegion(player, pos1, pos2)) {
                    // Check player claim limit
                    int maxClaims = plugin.getConfigManager().getMainConfig().getInt("claims.max-claims-per-player", 3);
                    if (plugin.getRegionManager().getPlayerRegions(playerId).size() >= maxClaims) {
                        String message = getMessage("claim.claim-limit-reached", 
                                "&cYou have reached your maximum number of claims (&f%limit%&c).");
                        message = message.replace("%limit%", String.valueOf(maxClaims));
                        sender.sendMessage(message);
                        return true;
                    }
                    
                    // Check minimum size
                    int minSize = plugin.getConfigManager().getMainConfig().getInt("claims.min-claim-size", 25);
                    int area = Math.abs((pos2.getBlockX() - pos1.getBlockX() + 1) * (pos2.getBlockZ() - pos1.getBlockZ() + 1));
                    if (area < minSize) {
                        String message = getMessage("claim.claim-too-small", 
                                "&cClaim is too small. Minimum size is &f%size% &cblocks.");
                        message = message.replace("%size%", String.valueOf(minSize));
                        sender.sendMessage(message);
                        return true;
                    }
                    
                    // Check maximum size
                    int maxSize = plugin.getConfigManager().getMainConfig().getInt("claims.max-claim-size", 10000);
                    if (area > maxSize && !player.hasPermission("frizzlengaurd.admin.*")) {
                        String message = getMessage("claim.claim-too-large", 
                                "&cClaim is too large. Maximum size is &f%size% &cblocks.");
                        message = message.replace("%size%", String.valueOf(maxSize));
                        sender.sendMessage(message);
                        return true;
                    }
                    
                    // Check claim blocks
                    int claimBlocks = plugin.getDataManager().getPlayerClaimBlocks(playerId);
                    if (area > claimBlocks && !player.hasPermission("frizzlengaurd.admin.*")) {
                        String message = getMessage("claim.not-enough-blocks", 
                                "&cYou don't have enough claim blocks. Need &f%needed%&c, have &f%have%&c.");
                        message = message.replace("%needed%", String.valueOf(area))
                                       .replace("%have%", String.valueOf(claimBlocks));
                        sender.sendMessage(message);
                        return true;
                    }
                    
                    // Check economy
                    if (plugin.isVaultEnabled() && EconomyHandler.isClaimCostEnabled() && 
                            !player.hasPermission("frizzlengaurd.admin.*")) {
                        double cost = EconomyHandler.calculateClaimCost(area);
                        if (!EconomyHandler.canAffordClaim(playerId, area)) {
                            String message = getMessage("claim.not-enough-money", 
                                    "&cYou don't have enough money. Need &f%needed%&c, have &f%have%&c.");
                            message = message.replace("%needed%", EconomyHandler.formatCost(cost))
                                           .replace("%have%", EconomyHandler.formatCost(0)); // Replace with actual balance
                            sender.sendMessage(message);
                            return true;
                        }
                    }
                    
                    // Check for overlapping claims
                    sender.sendMessage(getMessage("claim.claim-overlaps", 
                            "&cThis claim overlaps with an existing claim."));
                    return true;
                }
                
                // Generic error
                sender.sendMessage(ChatColor.RED + "Failed to create region. Please try again.");
                Logger.error("Failed to create region for player " + player.getName());
                return true;
            }
            
            // Region created successfully
            String message = getMessage("claim.claim-created", "&aSuccessfully created claim &f%name%&a.");
            message = message.replace("%name%", name);
            sender.sendMessage(message);
            
            // Show cost information if economy is enabled
            if (plugin.isVaultEnabled() && EconomyHandler.isClaimCostEnabled() && 
                    !player.hasPermission("frizzlengaurd.admin.*")) {
                int area = (region.getMaxX() - region.getMinX() + 1) * (region.getMaxZ() - region.getMinZ() + 1);
                double cost = EconomyHandler.calculateClaimCost(area);
                sender.sendMessage(ChatColor.GREEN + "Cost: " + ChatColor.WHITE + EconomyHandler.formatCost(cost));
            }
            
            // Show claim blocks information
            if (!player.hasPermission("frizzlengaurd.admin.*")) {
                int claimBlocks = plugin.getDataManager().getPlayerClaimBlocks(playerId);
                sender.sendMessage(ChatColor.GREEN + "Remaining claim blocks: " + ChatColor.WHITE + claimBlocks);
            }
            
            // Visualize the region boundaries
            plugin.getVisualsManager().showRegionBoundaries(player, region);
            
            // Clean up selection points and preview mode
            firstPoints.remove(playerId);
            secondPoints.remove(playerId);
            previewMode.remove(playerId);
            plugin.getVisualsManager().cancelPreview(player);
            
            return true;
        }
        
        // Invalid syntax
        sender.sendMessage(ChatColor.RED + "Usage: /fg claim - Select points with an empty hand or stick");
        sender.sendMessage(ChatColor.RED + "       /fg claim <name> - Create a claim with the selected points");
        sender.sendMessage(ChatColor.RED + "       /fg claim preview - Toggle preview mode for selection");
        return true;
    }
    
    /**
     * Show preview of the current selection
     * 
     * @param player The player to show the preview to
     */
    private void showPreview(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!firstPoints.containsKey(playerId) || !secondPoints.containsKey(playerId)) {
            return;
        }
        
        Location pos1 = firstPoints.get(playerId);
        Location pos2 = secondPoints.get(playerId);
        
        // Make sure they're in the same world
        if (pos1.getWorld().equals(pos2.getWorld())) {
            plugin.getVisualsManager().showPreview(player, pos1, pos2);
        }
    }
    
    /**
     * Display information about the selected area
     * 
     * @param player The player to show the information to
     */
    private void displayClaimInfo(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Make sure both points are selected
        if (!firstPoints.containsKey(playerId) || !secondPoints.containsKey(playerId)) {
            return;
        }
        
        Location pos1 = firstPoints.get(playerId);
        Location pos2 = secondPoints.get(playerId);
        
        // Calculate dimensions
        int width = Math.abs(pos2.getBlockX() - pos1.getBlockX()) + 1;
        int height = Math.abs(pos2.getBlockY() - pos1.getBlockY()) + 1;
        int depth = Math.abs(pos2.getBlockZ() - pos1.getBlockZ()) + 1;
        int area = width * depth;
        int volume = width * height * depth;
        
        // Show information
        player.sendMessage(ChatColor.GOLD + "=== Selection Information ===");
        player.sendMessage(ChatColor.YELLOW + "Dimensions: " + ChatColor.WHITE + 
                width + " x " + height + " x " + depth);
        player.sendMessage(ChatColor.YELLOW + "Area: " + ChatColor.WHITE + area + " blocks");
        player.sendMessage(ChatColor.YELLOW + "Volume: " + ChatColor.WHITE + volume + " blocks");
        
        // Check minimum and maximum size
        int minSize = plugin.getConfigManager().getMainConfig().getInt("claims.min-claim-size", 25);
        int maxSize = plugin.getConfigManager().getMainConfig().getInt("claims.max-claim-size", 10000);
        
        if (area < minSize) {
            player.sendMessage(ChatColor.RED + "This area is too small! Minimum size: " + minSize + " blocks.");
        } else if (area > maxSize && !player.hasPermission("frizzlengaurd.admin.*")) {
            player.sendMessage(ChatColor.RED + "This area is too large! Maximum size: " + maxSize + " blocks.");
        }
        
        // Show claim blocks information
        if (!player.hasPermission("frizzlengaurd.admin.*")) {
            int claimBlocks = plugin.getDataManager().getPlayerClaimBlocks(playerId);
            player.sendMessage(ChatColor.YELLOW + "Your claim blocks: " + ChatColor.WHITE + claimBlocks);
            
            if (area > claimBlocks) {
                player.sendMessage(ChatColor.RED + "You don't have enough claim blocks! Need " + area + " blocks.");
            }
        }
        
        // Show cost information if economy is enabled
        if (plugin.isVaultEnabled() && EconomyHandler.isClaimCostEnabled() &&
                !player.hasPermission("frizzlengaurd.admin.*")) {
            double cost = EconomyHandler.calculateClaimCost(area);
            player.sendMessage(ChatColor.YELLOW + "Cost: " + ChatColor.WHITE + EconomyHandler.formatCost(cost));
            
            if (!EconomyHandler.canAffordClaim(playerId, area)) {
                player.sendMessage(ChatColor.RED + "You cannot afford this claim!");
            }
        }
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
        previewMode.remove(playerId);
    }
} 