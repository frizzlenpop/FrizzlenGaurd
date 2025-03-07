package org.frizzlenpop.frizzlenGaurd.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RegionManager {
    private final FrizzlenGaurd plugin;
    private final Map<String, Region> regions;

    public RegionManager(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        this.regions = new HashMap<>();
        loadRegions();
    }

    public void addRegion(Region region) {
        regions.put(region.getName().toLowerCase(), region);
    }

    public void removeRegion(String name) {
        regions.remove(name.toLowerCase());
    }

    public Region getRegion(String name) {
        return regions.get(name.toLowerCase());
    }

    public Collection<Region> getAllRegions() {
        return regions.values();
    }

    public void clearRegions() {
        regions.clear();
    }

    public void saveRegions() {
        saveData();
    }

    public List<Region> getPlayerRegions(UUID playerId) {
        return regions.values().stream()
                .filter(region -> region.getOwners().containsKey(playerId) || region.getMembers().containsKey(playerId))
                .collect(Collectors.toList());
    }

    public void saveData() {
        // Get the regions file
        File regionsFile = new File(plugin.getDataFolder(), "regions.yml");
        
        // Create parent directories if they don't exist
        regionsFile.getParentFile().mkdirs();
        
        // Create the configuration
        YamlConfiguration config = new YamlConfiguration();
        
        // Save each region
        for (Region region : regions.values()) {
            String path = "regions." + region.getId();
            config.set(path + ".name", region.getName());
            config.set(path + ".world", region.getWorld().getName());
            config.set(path + ".minX", region.getMinPoint().getBlockX());
            config.set(path + ".minY", region.getMinPoint().getBlockY());
            config.set(path + ".minZ", region.getMinPoint().getBlockZ());
            config.set(path + ".maxX", region.getMaxPoint().getBlockX());
            config.set(path + ".maxY", region.getMaxPoint().getBlockY());
            config.set(path + ".maxZ", region.getMaxPoint().getBlockZ());
            
            // Save owners
            List<String> owners = region.getOwners().keySet().stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
            config.set(path + ".owners", owners);
            
            // Save members
            List<String> members = region.getMembers().keySet().stream()
                    .map(UUID::toString)
                    .collect(Collectors.toList());
            config.set(path + ".members", members);
            
            // Save flags
            config.set(path + ".flags", new ArrayList<>(region.getFlags().entrySet()));
        }
        
        try {
            // Save the configuration to file
            config.save(regionsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save regions: " + e.getMessage());
        }
    }

    private void loadRegions() {
        File regionsFile = new File(plugin.getDataFolder(), "regions.yml");
        if (!regionsFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(regionsFile);
        if (!config.contains("regions")) {
            return;
        }

        for (String id : config.getConfigurationSection("regions").getKeys(false)) {
            try {
                String path = "regions." + id;
                String name = config.getString(path + ".name");
                String worldName = config.getString(path + ".world");
                
                // Load coordinates
                int minX = config.getInt(path + ".minX");
                int minY = config.getInt(path + ".minY");
                int minZ = config.getInt(path + ".minZ");
                int maxX = config.getInt(path + ".maxX");
                int maxY = config.getInt(path + ".maxY");
                int maxZ = config.getInt(path + ".maxZ");
                
                // Create region
                Region region = new Region(name, 
                        new Location(plugin.getServer().getWorld(worldName), minX, minY, minZ),
                        new Location(plugin.getServer().getWorld(worldName), maxX, maxY, maxZ));
                
                // Load owners
                List<String> owners = config.getStringList(path + ".owners");
                for (String ownerUUID : owners) {
                    region.addOwner(UUID.fromString(ownerUUID));
                }
                
                // Load members
                List<String> members = config.getStringList(path + ".members");
                for (String memberUUID : members) {
                    region.addMember(UUID.fromString(memberUUID));
                }
                
                // Load flags
                if (config.contains(path + ".flags")) {
                    @SuppressWarnings("unchecked")
                    List<Map<?, ?>> flagsList = config.getMapList(path + ".flags");
                    for (Map<?, ?> entry : flagsList) {
                        String flagName = entry.get("key").toString();
                        boolean flagValue = Boolean.parseBoolean(entry.get("value").toString());
                        region.getFlags().put(flagName, flagValue);
                    }
                }
                
                // Add region to manager
                regions.put(name.toLowerCase(), region);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load region " + id + ": " + e.getMessage());
            }
        }
    }
} 