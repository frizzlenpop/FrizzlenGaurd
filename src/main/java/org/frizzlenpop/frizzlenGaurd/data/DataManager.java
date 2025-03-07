package org.frizzlenpop.frizzlenGaurd.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {
    private final FrizzlenGaurd plugin;
    private final File dataFolder;
    private final File regionsFile;
    private final File backupFolder;
    private final Map<UUID, Integer> playerClaimBlocks;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    
    public DataManager(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.regionsFile = new File(dataFolder, "regions.yml");
        this.backupFolder = new File(dataFolder, "backups");
        this.playerClaimBlocks = new ConcurrentHashMap<>();
        
        // Create necessary directories
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
    }
    
    public void loadData() {
        loadRegions();
        loadPlayerData();
        
        Logger.info("Data loaded successfully.");
    }
    
    private void loadRegions() {
        if (!regionsFile.exists()) {
            Logger.info("No regions file found. Creating a new one.");
            return;
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(regionsFile);
            ConfigurationSection regionsSection = config.getConfigurationSection("regions");
            
            if (regionsSection == null) {
                Logger.warning("No regions found in regions.yml.");
                return;
            }
            
            // Clear existing regions
            plugin.getRegionManager().clear();
            
            // Load regions
            for (String regionId : regionsSection.getKeys(false)) {
                try {
                    ConfigurationSection regionSection = regionsSection.getConfigurationSection(regionId);
                    if (regionSection == null) continue;
                    
                    Map<String, Object> regionData = regionSection.getValues(true);
                    Region region = Region.deserialize(regionData);
                    
                    // Only add main regions (subregions are added by their parents)
                    if (!region.isSubregion()) {
                        plugin.getRegionManager().addRegion(region);
                    }
                } catch (Exception e) {
                    Logger.error("Failed to load region " + regionId + ": " + e.getMessage());
                }
            }
            
            Logger.info("Loaded " + plugin.getRegionManager().getAllRegions().size() + " regions.");
        } catch (Exception e) {
            Logger.error("Failed to load regions: " + e.getMessage());
        }
    }
    
    private void loadPlayerData() {
        File playerDataFile = new File(dataFolder, "playerdata.yml");
        
        if (!playerDataFile.exists()) {
            Logger.info("No player data file found. Creating a new one.");
            return;
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerDataFile);
            ConfigurationSection playersSection = config.getConfigurationSection("players");
            
            if (playersSection == null) {
                Logger.warning("No player data found in playerdata.yml.");
                return;
            }
            
            // Clear existing player data
            playerClaimBlocks.clear();
            
            // Load player data
            for (String playerIdStr : playersSection.getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(playerIdStr);
                    ConfigurationSection playerSection = playersSection.getConfigurationSection(playerIdStr);
                    if (playerSection == null) continue;
                    
                    int claimBlocks = playerSection.getInt("claim-blocks", 
                            plugin.getConfigManager().getMainConfig().getInt("claims.default-claim-blocks", 1000));
                    
                    playerClaimBlocks.put(playerId, claimBlocks);
                } catch (Exception e) {
                    Logger.error("Failed to load player data for " + playerIdStr + ": " + e.getMessage());
                }
            }
            
            Logger.info("Loaded data for " + playerClaimBlocks.size() + " players.");
        } catch (Exception e) {
            Logger.error("Failed to load player data: " + e.getMessage());
        }
    }
    
    public void saveData() {
        saveRegions();
        savePlayerData();
        
        Logger.info("Data saved successfully.");
    }
    
    private void saveRegions() {
        try {
            FileConfiguration config = new YamlConfiguration();
            ConfigurationSection regionsSection = config.createSection("regions");
            
            // Save all regions
            for (Region region : plugin.getRegionManager().getAllRegions()) {
                // Only save main regions (subregions are saved with their parents)
                if (!region.isSubregion()) {
                    ConfigurationSection regionSection = regionsSection.createSection(region.getId());
                    Map<String, Object> serialized = region.serialize();
                    
                    for (Map.Entry<String, Object> entry : serialized.entrySet()) {
                        regionSection.set(entry.getKey(), entry.getValue());
                    }
                }
            }
            
            config.save(regionsFile);
            Logger.debug("Saved " + plugin.getRegionManager().getAllRegions().size() + " regions.");
        } catch (IOException e) {
            Logger.error("Failed to save regions: " + e.getMessage());
        }
    }
    
    private void savePlayerData() {
        try {
            FileConfiguration config = new YamlConfiguration();
            ConfigurationSection playersSection = config.createSection("players");
            
            // Save player data
            for (Map.Entry<UUID, Integer> entry : playerClaimBlocks.entrySet()) {
                ConfigurationSection playerSection = playersSection.createSection(entry.getKey().toString());
                playerSection.set("claim-blocks", entry.getValue());
            }
            
            config.save(new File(dataFolder, "playerdata.yml"));
            Logger.debug("Saved data for " + playerClaimBlocks.size() + " players.");
        } catch (IOException e) {
            Logger.error("Failed to save player data: " + e.getMessage());
        }
    }
    
    public void createBackup() {
        try {
            // Generate backup filename with timestamp
            String timestamp = DATE_FORMAT.format(new Date());
            File backupFile = new File(backupFolder, "regions_" + timestamp + ".yml");
            
            // Save current regions to backup file
            FileConfiguration config = new YamlConfiguration();
            ConfigurationSection regionsSection = config.createSection("regions");
            
            for (Region region : plugin.getRegionManager().getAllRegions()) {
                if (!region.isSubregion()) {
                    ConfigurationSection regionSection = regionsSection.createSection(region.getId());
                    Map<String, Object> serialized = region.serialize();
                    
                    for (Map.Entry<String, Object> entry : serialized.entrySet()) {
                        regionSection.set(entry.getKey(), entry.getValue());
                    }
                }
            }
            
            config.save(backupFile);
            
            // Also backup player data
            File playerBackupFile = new File(backupFolder, "playerdata_" + timestamp + ".yml");
            FileConfiguration playerConfig = new YamlConfiguration();
            ConfigurationSection playersSection = playerConfig.createSection("players");
            
            for (Map.Entry<UUID, Integer> entry : playerClaimBlocks.entrySet()) {
                ConfigurationSection playerSection = playersSection.createSection(entry.getKey().toString());
                playerSection.set("claim-blocks", entry.getValue());
            }
            
            playerConfig.save(playerBackupFile);
            
            // Clean up old backups
            cleanupOldBackups();
            
            Logger.info("Created backup: " + backupFile.getName());
        } catch (IOException e) {
            Logger.error("Failed to create backup: " + e.getMessage());
        }
    }
    
    private void cleanupOldBackups() {
        int maxBackups = plugin.getConfigManager().getMainConfig().getInt("max-backups", 10);
        if (maxBackups <= 0) {
            return;
        }
        
        File[] backupFiles = backupFolder.listFiles((dir, name) -> name.startsWith("regions_") && name.endsWith(".yml"));
        if (backupFiles == null || backupFiles.length <= maxBackups) {
            return;
        }
        
        // Sort by last modified (oldest first)
        java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
        
        // Delete oldest backups
        for (int i = 0; i < backupFiles.length - maxBackups; i++) {
            if (backupFiles[i].delete()) {
                Logger.debug("Deleted old backup: " + backupFiles[i].getName());
            }
        }
    }
    
    public int getPlayerClaimBlocks(UUID playerId) {
        return playerClaimBlocks.getOrDefault(playerId, 
                plugin.getConfigManager().getMainConfig().getInt("claims.default-claim-blocks", 1000));
    }
    
    public void setPlayerClaimBlocks(UUID playerId, int blocks) {
        playerClaimBlocks.put(playerId, blocks);
    }
    
    public void addPlayerClaimBlocks(UUID playerId, int blocks) {
        int current = getPlayerClaimBlocks(playerId);
        playerClaimBlocks.put(playerId, current + blocks);
    }
} 