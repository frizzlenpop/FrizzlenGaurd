package org.frizzlenpop.frizzlenGaurd.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LimitManager {
    private final FrizzlenGaurd plugin;
    private final Map<String, Integer> groupLimits;
    private final Map<UUID, Integer> playerLimits;
    private final Map<UUID, Integer> usedBlocks;
    private final int defaultLimit;
    
    public LimitManager(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        this.groupLimits = new HashMap<>();
        this.playerLimits = new ConcurrentHashMap<>();
        this.usedBlocks = new ConcurrentHashMap<>();
        this.defaultLimit = plugin.getConfig().getInt("limits.default", 10000);
        
        loadLimits();
        calculateUsedBlocks();
    }
    
    private void loadLimits() {
        // Load group limits
        ConfigurationSection groupSection = plugin.getConfig().getConfigurationSection("limits.groups");
        if (groupSection != null) {
            for (String group : groupSection.getKeys(false)) {
                groupLimits.put(group, groupSection.getInt(group));
            }
        }
        
        // Load player limits
        ConfigurationSection playerSection = plugin.getConfig().getConfigurationSection("limits.players");
        if (playerSection != null) {
            for (String uuidStr : playerSection.getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(uuidStr);
                    playerLimits.put(playerId, playerSection.getInt(uuidStr));
                } catch (IllegalArgumentException ignored) {
                    // Invalid UUID, skip it
                }
            }
        }
    }
    
    private void calculateUsedBlocks() {
        usedBlocks.clear();
        
        for (Region region : plugin.getRegionManager().getAllRegions()) {
            for (UUID ownerId : region.getOwners().keySet()) {
                int volume = calculateRegionVolume(region);
                usedBlocks.merge(ownerId, volume, Integer::sum);
            }
        }
    }
    
    public int getLimit(Player player) {
        // Check for player-specific limit
        if (playerLimits.containsKey(player.getUniqueId())) {
            return playerLimits.get(player.getUniqueId());
        }
        
        // Check for group limits (highest takes precedence)
        int highestLimit = defaultLimit;
        for (Map.Entry<String, Integer> entry : groupLimits.entrySet()) {
            String permission = "frizzlengaurd.limit." + entry.getKey();
            if (player.hasPermission(permission) && entry.getValue() > highestLimit) {
                highestLimit = entry.getValue();
            }
        }
        
        return highestLimit;
    }
    
    public int getUsedBlocks(UUID playerId) {
        return usedBlocks.getOrDefault(playerId, 0);
    }
    
    public int getRemainingBlocks(Player player) {
        return getLimit(player) - getUsedBlocks(player.getUniqueId());
    }
    
    public boolean canClaim(Player player, Region region) {
        int volume = calculateRegionVolume(region);
        return getRemainingBlocks(player) >= volume;
    }
    
    public void updateUsedBlocks(UUID playerId) {
        int total = 0;
        for (Region region : plugin.getRegionManager().getAllRegions()) {
            if (region.getOwners().containsKey(playerId)) {
                total += calculateRegionVolume(region);
            }
        }
        usedBlocks.put(playerId, total);
    }
    
    public void setPlayerLimit(UUID playerId, int limit) {
        if (limit > 0) {
            playerLimits.put(playerId, limit);
        } else {
            playerLimits.remove(playerId);
        }
        savePlayerLimits();
    }
    
    private void savePlayerLimits() {
        ConfigurationSection playerSection = plugin.getConfig().createSection("limits.players");
        for (Map.Entry<UUID, Integer> entry : playerLimits.entrySet()) {
            playerSection.set(entry.getKey().toString(), entry.getValue());
        }
        plugin.saveConfig();
    }
    
    private int calculateRegionVolume(Region region) {
        int width = Math.abs(region.getMaxPoint().getBlockX() - region.getMinPoint().getBlockX()) + 1;
        int height = Math.abs(region.getMaxPoint().getBlockY() - region.getMinPoint().getBlockY()) + 1;
        int depth = Math.abs(region.getMaxPoint().getBlockZ() - region.getMinPoint().getBlockZ()) + 1;
        return width * height * depth;
    }
    
    public void reload() {
        loadLimits();
        calculateUsedBlocks();
    }
} 