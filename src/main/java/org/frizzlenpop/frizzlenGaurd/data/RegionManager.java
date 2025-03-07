package org.frizzlenpop.frizzlenGaurd.data;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.LogEntry;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.models.Role;
import org.frizzlenpop.frizzlenGaurd.utils.EconomyHandler;
import org.frizzlenpop.frizzlenGaurd.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RegionManager {
    private final FrizzlenGaurd plugin;
    private final Map<String, Region> regions;
    private final Map<UUID, Set<String>> playerRegions;
    private final Map<String, Set<String>> worldRegions;
    
    public RegionManager(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        this.regions = new ConcurrentHashMap<>();
        this.playerRegions = new ConcurrentHashMap<>();
        this.worldRegions = new ConcurrentHashMap<>();
    }
    
    public void addRegion(Region region) {
        String regionId = region.getId();
        regions.put(regionId, region);
        
        // Add to player index
        UUID ownerId = region.getOwner();
        playerRegions.computeIfAbsent(ownerId, k -> new HashSet<>()).add(regionId);
        
        // Add to world index
        String worldName = region.getWorldName();
        worldRegions.computeIfAbsent(worldName, k -> new HashSet<>()).add(regionId);
        
        Logger.debug("Added region " + region.getName() + " (ID: " + regionId + ")");
    }
    
    public void removeRegion(String regionId) {
        Region region = regions.get(regionId);
        if (region == null) {
            return;
        }
        
        // Remove from player index
        UUID ownerId = region.getOwner();
        if (playerRegions.containsKey(ownerId)) {
            playerRegions.get(ownerId).remove(regionId);
            if (playerRegions.get(ownerId).isEmpty()) {
                playerRegions.remove(ownerId);
            }
        }
        
        // Remove from world index
        String worldName = region.getWorldName();
        if (worldRegions.containsKey(worldName)) {
            worldRegions.get(worldName).remove(regionId);
            if (worldRegions.get(worldName).isEmpty()) {
                worldRegions.remove(worldName);
            }
        }
        
        // Remove all subregions
        for (Region subregion : new ArrayList<>(region.getSubregions())) {
            removeRegion(subregion.getId());
        }
        
        // Remove from parent if it's a subregion
        if (region.isSubregion() && region.getParent() != null) {
            region.getParent().removeSubregion(region);
        }
        
        // Finally remove the region itself
        regions.remove(regionId);
        
        Logger.debug("Removed region " + region.getName() + " (ID: " + regionId + ")");
    }
    
    public Region getRegion(String regionId) {
        return regions.get(regionId);
    }
    
    public Collection<Region> getAllRegions() {
        return regions.values();
    }
    
    public List<Region> getPlayerRegions(UUID playerId) {
        if (!playerRegions.containsKey(playerId)) {
            return new ArrayList<>();
        }
        
        return playerRegions.get(playerId).stream()
                .map(regions::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    public List<Region> getWorldRegions(String worldName) {
        if (!worldRegions.containsKey(worldName)) {
            return new ArrayList<>();
        }
        
        return worldRegions.get(worldName).stream()
                .map(regions::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    public Region getRegionAt(Location location) {
        String worldName = location.getWorld().getName();
        
        // Check if world is excluded
        if (isWorldExcluded(worldName)) {
            return null;
        }
        
        // First check for subregions (they have priority)
        for (Region region : getWorldRegions(worldName)) {
            if (region.contains(location)) {
                // Check subregions
                for (Region subregion : region.getSubregions()) {
                    if (subregion.contains(location)) {
                        return subregion;
                    }
                }
                return region;
            }
        }
        
        return null;
    }
    
    public boolean isWorldExcluded(String worldName) {
        List<String> excludedWorlds = plugin.getConfigManager().getMainConfig()
                .getStringList("worlds.excluded");
        return excludedWorlds.contains(worldName);
    }
    
    public boolean canCreateRegion(Player player, Location pos1, Location pos2) {
        // Check if world is excluded
        if (isWorldExcluded(pos1.getWorld().getName())) {
            return false;
        }
        
        // Check if player has permission
        if (!player.hasPermission("frizzlengaurd.claim")) {
            return false;
        }
        
        // Check if player has reached their claim limit
        int maxClaims = plugin.getConfigManager().getMainConfig().getInt("claims.max-claims-per-player", 3);
        if (getPlayerRegions(player.getUniqueId()).size() >= maxClaims && !player.hasPermission("frizzlengaurd.admin.*")) {
            return false;
        }
        
        // Calculate region size
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
        int area = (maxX - minX + 1) * (maxZ - minZ + 1);
        
        // Check minimum size
        int minSize = plugin.getConfigManager().getMainConfig().getInt("claims.min-claim-size", 25);
        if (area < minSize) {
            return false;
        }
        
        // Check maximum size
        int maxSize = plugin.getConfigManager().getMainConfig().getInt("claims.max-claim-size", 10000);
        if (area > maxSize && !player.hasPermission("frizzlengaurd.admin.*")) {
            return false;
        }
        
        // Check if player has enough claim blocks
        if (!player.hasPermission("frizzlengaurd.admin.*")) {
            int claimBlocks = plugin.getDataManager().getPlayerClaimBlocks(player.getUniqueId());
            if (area > claimBlocks) {
                return false;
            }
        }
        
        // Check if player can afford the claim
        if (!player.hasPermission("frizzlengaurd.admin.*") && 
                !EconomyHandler.canAffordClaim(player.getUniqueId(), area)) {
            return false;
        }
        
        // Check for overlapping regions
        World world = pos1.getWorld();
        for (Region region : getWorldRegions(world.getName())) {
            // Create a temporary region to check for overlap
            Region tempRegion = new Region(
                    UUID.randomUUID().toString(),
                    "temp",
                    player.getUniqueId(),
                    new Location(world, minX, minY, minZ),
                    new Location(world, maxX, maxY, maxZ)
            );
            
            if (tempRegion.overlaps(region)) {
                return false;
            }
        }
        
        return true;
    }
    
    public Region createRegion(Player player, String name, Location pos1, Location pos2) {
        if (!canCreateRegion(player, pos1, pos2)) {
            return null;
        }
        
        String regionId = UUID.randomUUID().toString();
        Region region = new Region(regionId, name, player.getUniqueId(), pos1, pos2);
        
        // Add the region
        addRegion(region);
        
        // Calculate area
        int area = (region.getMaxX() - region.getMinX() + 1) * (region.getMaxZ() - region.getMinZ() + 1);
        
        // Charge the player if economy is enabled
        if (!player.hasPermission("frizzlengaurd.admin.*")) {
            // Deduct claim blocks
            plugin.getDataManager().addPlayerClaimBlocks(player.getUniqueId(), -area);
            
            // Charge money if economy is enabled
            if (plugin.isVaultEnabled()) {
                EconomyHandler.chargeClaim(player.getUniqueId(), area);
            }
        }
        
        // Log the creation
        region.addLogEntry(new LogEntry(player, LogEntry.LogAction.CLAIM_CREATE, 
                "Created claim " + name, null));
        
        return region;
    }
    
    public boolean canCreateSubregion(Player player, Region parentRegion, Location pos1, Location pos2) {
        // Check if player has permission
        if (!player.hasPermission("frizzlengaurd.subclaim")) {
            return false;
        }
        
        // Check if player can create subregions in this region
        Region.Role role = parentRegion.getMemberRole(player.getUniqueId());
        if (role != Region.Role.OWNER && !player.hasPermission("frizzlengaurd.admin.*")) {
            return false;
        }
        
        // Check if the parent region has reached its subregion limit
        int maxSubregions = plugin.getConfigManager().getMainConfig().getInt("claims.max-subregions-per-claim", 5);
        if (parentRegion.getSubregions().size() >= maxSubregions && !player.hasPermission("frizzlengaurd.admin.*")) {
            return false;
        }
        
        // Calculate region size
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        // Check if the subregion is completely within the parent region
        if (minX < parentRegion.getMinX() || maxX > parentRegion.getMaxX() ||
            minY < parentRegion.getMinY() || maxY > parentRegion.getMaxY() ||
            minZ < parentRegion.getMinZ() || maxZ > parentRegion.getMaxZ()) {
            return false;
        }
        
        // Check for overlapping with other subregions
        for (Region subregion : parentRegion.getSubregions()) {
            if (minX <= subregion.getMaxX() && maxX >= subregion.getMinX() &&
                minY <= subregion.getMaxY() && maxY >= subregion.getMinY() &&
                minZ <= subregion.getMaxZ() && maxZ >= subregion.getMinZ()) {
                return false;
            }
        }
        
        return true;
    }
    
    public Region createSubregion(Player player, Region parentRegion, String name, Location pos1, Location pos2) {
        if (!canCreateSubregion(player, parentRegion, pos1, pos2)) {
            return null;
        }
        
        String regionId = UUID.randomUUID().toString();
        Region subregion = new Region(regionId, name, parentRegion.getOwner(), pos1, pos2);
        
        // Add the subregion to the parent
        parentRegion.addSubregion(subregion);
        
        // Add the subregion to the manager
        addRegion(subregion);
        
        // Log the creation
        subregion.addLogEntry(new LogEntry(player, LogEntry.LogAction.CLAIM_CREATE, 
                "Created subregion " + name + " in " + parentRegion.getName(), null));
        
        return subregion;
    }
    
    public void clear() {
        regions.clear();
        playerRegions.clear();
        worldRegions.clear();
    }
} 