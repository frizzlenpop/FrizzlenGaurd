package org.frizzlenpop.frizzlenGaurd.utils;

import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;

import java.util.UUID;

/**
 * Manages permission-based claim limits
 */
public class PermissionsManager {
    private final FrizzlenGaurd plugin;
    
    // Default values
    private final int defaultMaxClaims;
    private final int defaultClaimBlocks;
    
    public PermissionsManager(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        
        // Load defaults from config
        this.defaultMaxClaims = plugin.getConfigManager().getMainConfig().getInt("claims.default-max-claims", 1);
        this.defaultClaimBlocks = plugin.getConfigManager().getMainConfig().getInt("claims.default-claim-blocks", 250);
    }
    
    /**
     * Gets the maximum number of claims a player can have based on their permissions
     * 
     * @param player The player to check
     * @return The maximum number of claims
     */
    public int getMaxClaims(Player player) {
        if (player.hasPermission("frizzlengaurd.admin.bypass")) {
            return Integer.MAX_VALUE; // Admin bypass
        }
        
        // Start with default
        int maxClaims = defaultMaxClaims;
        
        // Check for permission-based limits
        for (int i = 50; i >= 1; i--) {
            if (player.hasPermission("frizzlengaurd.claims." + i)) {
                maxClaims = i;
                break;
            }
        }
        
        return maxClaims;
    }
    
    /**
     * Gets the number of claim blocks a player has based on their permissions
     * 
     * @param player The player to check
     * @return The number of claim blocks
     */
    public int getClaimBlocks(Player player) {
        if (player.hasPermission("frizzlengaurd.admin.bypass")) {
            return Integer.MAX_VALUE; // Admin bypass
        }
        
        // Start with default
        int claimBlocks = defaultClaimBlocks;
        
        // Check for permission-based blocks (check higher values first)
        for (int i = 10000; i >= 100; i -= 50) {
            if (player.hasPermission("frizzlengaurd.blocks." + i)) {
                claimBlocks = i;
                break;
            }
        }
        
        return claimBlocks;
    }
    
    /**
     * Checks if a player can create another claim
     * 
     * @param player The player to check
     * @return True if they can create another claim, false otherwise
     */
    public boolean canCreateClaim(Player player) {
        int currentClaims = plugin.getRegionManager().getRegionsByOwner(player.getUniqueId()).size();
        int maxClaims = getMaxClaims(player);
        
        return currentClaims < maxClaims;
    }
    
    /**
     * Gets the number of remaining claims a player can create
     * 
     * @param player The player to check
     * @return The number of remaining claims
     */
    public int getRemainingClaims(Player player) {
        int currentClaims = plugin.getRegionManager().getRegionsByOwner(player.getUniqueId()).size();
        int maxClaims = getMaxClaims(player);
        
        return Math.max(0, maxClaims - currentClaims);
    }
    
    /**
     * Checks if a player has enough claim blocks for a specific volume
     * 
     * @param player The player to check
     * @param volume The volume to check
     * @return True if they have enough blocks, false otherwise
     */
    public boolean hasEnoughClaimBlocks(Player player, int volume) {
        UUID playerId = player.getUniqueId();
        
        // Admin bypass check
        if (player.hasPermission("frizzlengaurd.admin.bypass")) {
            return true;
        }
        
        // Get used claim blocks
        int usedBlocks = plugin.getRegionManager().getUsedClaimBlocks(playerId);
        
        // Get max claim blocks from permissions
        int maxBlocks = getClaimBlocks(player);
        
        // Check if they have enough blocks remaining
        return (usedBlocks + volume) <= maxBlocks;
    }
    
    /**
     * Gets the number of claim blocks a player is using
     * 
     * @param playerId The player UUID
     * @return The number of claim blocks being used
     */
    public int getUsedClaimBlocks(UUID playerId) {
        return plugin.getRegionManager().getUsedClaimBlocks(playerId);
    }
    
    /**
     * Gets the number of claim blocks a player has remaining
     * 
     * @param player The player to check
     * @return The number of remaining claim blocks
     */
    public int getRemainingClaimBlocks(Player player) {
        UUID playerId = player.getUniqueId();
        int usedBlocks = getUsedClaimBlocks(playerId);
        int maxBlocks = getClaimBlocks(player);
        
        return Math.max(0, maxBlocks - usedBlocks);
    }
} 