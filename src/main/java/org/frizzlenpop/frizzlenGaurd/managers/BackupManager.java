package org.frizzlenpop.frizzlenGaurd.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.data.RegionManager;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class BackupManager {
    private final FrizzlenGaurd plugin;
    private final File backupDir;
    private final Map<String, BackupInfo> backups;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final int MAX_BACKUPS = 10;
    
    public BackupManager(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        this.backupDir = new File(plugin.getDataFolder(), "backups");
        this.backups = new ConcurrentHashMap<>();
        
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        
        loadBackups();
        scheduleAutoBackup();
    }
    
    public void createBackup(String name, String description) {
        String timestamp = DATE_FORMAT.format(new Date());
        String fileName = name + "_" + timestamp + ".zip";
        File backupFile = new File(backupDir, fileName);
        
        try (ZipOutputStream zos = new ZipOutputStream(new java.io.FileOutputStream(backupFile))) {
            // Save regions to a temporary file
            File tempRegions = new File(plugin.getDataFolder(), "temp_regions.yml");
            YamlConfiguration config = new YamlConfiguration();
            
            // Save each region
            for (Region region : plugin.getRegionManager().getAllRegions()) {
                String path = "regions." + region.getId() + ".";
                config.set(path + "name", region.getName());
                config.set(path + "world", region.getWorld().getName());
                config.set(path + "minX", region.getMinPoint().getBlockX());
                config.set(path + "minY", region.getMinPoint().getBlockY());
                config.set(path + "minZ", region.getMinPoint().getBlockZ());
                config.set(path + "maxX", region.getMaxPoint().getBlockX());
                config.set(path + "maxY", region.getMaxPoint().getBlockY());
                config.set(path + "maxZ", region.getMaxPoint().getBlockZ());
                config.set(path + "owners", new ArrayList<>(region.getOwners().keySet()));
                config.set(path + "members", new ArrayList<>(region.getMembers().keySet()));
                config.set(path + "flags", new ArrayList<>(region.getFlags().entrySet()));
            }
            
            config.save(tempRegions);
            
            // Add regions file to zip
            addToZip(zos, tempRegions, "regions.yml");
            
            // Add other plugin files
            addToZip(zos, new File(plugin.getDataFolder(), "config.yml"), "config.yml");
            
            // Delete temporary file
            tempRegions.delete();
            
            // Create backup info
            BackupInfo info = new BackupInfo(fileName, description, System.currentTimeMillis());
            backups.put(fileName, info);
            saveBackupInfo();
            
            // Clean up old backups if needed
            cleanupOldBackups();
            
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create backup: " + e.getMessage());
        }
    }
    
    public boolean restoreBackup(String fileName) {
        File backupFile = new File(backupDir, fileName);
        if (!backupFile.exists()) {
            return false;
        }
        
        try (ZipFile zip = new ZipFile(backupFile)) {
            // Create temporary directory
            File tempDir = new File(plugin.getDataFolder(), "temp_restore");
            tempDir.mkdirs();
            
            // Extract files
            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
                ZipEntry entry = e.nextElement();
                File outFile = new File(tempDir, entry.getName());
                
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }
                
                try (java.io.InputStream is = zip.getInputStream(entry);
                     java.io.FileOutputStream fos = new java.io.FileOutputStream(outFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            
            // Load regions from backup
            File regionsFile = new File(tempDir, "regions.yml");
            if (regionsFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(regionsFile);
                
                // Clear current regions
                plugin.getRegionManager().clear();
                
                // Load regions from backup
                if (config.contains("regions")) {
                    for (String id : config.getConfigurationSection("regions").getKeys(false)) {
                        String path = "regions." + id + ".";
                        
                        try {
                            String name = config.getString(path + "name");
                            String worldName = config.getString(path + "world");
                            
                            // Create region
                            Region region = new Region(name,
                                    Bukkit.getWorld(worldName).getBlockAt(
                                            config.getInt(path + "minX"),
                                            config.getInt(path + "minY"),
                                            config.getInt(path + "minZ")).getLocation(),
                                    Bukkit.getWorld(worldName).getBlockAt(
                                            config.getInt(path + "maxX"),
                                            config.getInt(path + "maxY"),
                                            config.getInt(path + "maxZ")).getLocation());
                            
                            // Load owners
                            List<String> owners = config.getStringList(path + "owners");
                            for (String ownerUUID : owners) {
                                region.addOwner(UUID.fromString(ownerUUID));
                            }
                            
                            // Load members
                            List<String> members = config.getStringList(path + "members");
                            for (String memberUUID : members) {
                                region.addMember(UUID.fromString(memberUUID));
                            }
                            
                            // Load flags
                            if (config.contains(path + "flags")) {
                                @SuppressWarnings("unchecked")
                                List<Map<?, ?>> flagsList = config.getMapList(path + "flags");
                                for (Map<?, ?> entry : flagsList) {
                                    String flagName = entry.get("key").toString();
                                    boolean flagValue = Boolean.parseBoolean(entry.get("value").toString());
                                    region.getFlags().put(flagName, flagValue);
                                }
                            }
                            
                            // Add region
                            plugin.getRegionManager().addRegion(region);
                            
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to load region " + id + 
                                    " from backup: " + e.getMessage());
                        }
                    }
                }
            }
            
            // Copy config if it exists
            File configFile = new File(tempDir, "config.yml");
            if (configFile.exists()) {
                java.nio.file.Files.copy(
                    configFile.toPath(),
                    new File(plugin.getDataFolder(), "config.yml").toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            }
            
            // Clean up temp directory
            deleteDirectory(tempDir);
            
            // Save changes
            plugin.getDataManager().saveData();
            
            return true;
            
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to restore backup: " + e.getMessage());
            return false;
        }
    }
    
    public List<BackupInfo> getBackups() {
        return backups.values().stream()
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .collect(Collectors.toList());
    }
    
    private void loadBackups() {
        File infoFile = new File(backupDir, "backups.yml");
        if (!infoFile.exists()) {
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(infoFile);
        for (String fileName : config.getKeys(false)) {
            String description = config.getString(fileName + ".description");
            long timestamp = config.getLong(fileName + ".timestamp");
            
            if (new File(backupDir, fileName).exists()) {
                backups.put(fileName, new BackupInfo(fileName, description, timestamp));
            }
        }
    }
    
    private void saveBackupInfo() {
        File infoFile = new File(backupDir, "backups.yml");
        YamlConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<String, BackupInfo> entry : backups.entrySet()) {
            BackupInfo info = entry.getValue();
            config.set(info.getFileName() + ".description", info.getDescription());
            config.set(info.getFileName() + ".timestamp", info.getTimestamp());
        }
        
        try {
            config.save(infoFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save backup info: " + e.getMessage());
        }
    }
    
    private void cleanupOldBackups() {
        List<BackupInfo> sortedBackups = getBackups();
        if (sortedBackups.size() > MAX_BACKUPS) {
            for (int i = MAX_BACKUPS; i < sortedBackups.size(); i++) {
                BackupInfo info = sortedBackups.get(i);
                File backupFile = new File(backupDir, info.getFileName());
                if (backupFile.exists() && backupFile.delete()) {
                    backups.remove(info.getFileName());
                }
            }
            saveBackupInfo();
        }
    }
    
    private void scheduleAutoBackup() {
        long interval = plugin.getConfig().getLong("backup.interval", 24 * 60 * 60 * 20); // Default: 24 hours
        if (interval > 0) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                createBackup("auto", "Automatic backup");
                plugin.getLogger().info("Created automatic backup.");
            }, interval, interval);
        }
    }
    
    private void addToZip(ZipOutputStream zos, File file, String name) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        
        byte[] buffer = new byte[1024];
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        }
        
        zos.closeEntry();
    }
    
    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
    
    public static class BackupInfo {
        private final String fileName;
        private final String description;
        private final long timestamp;
        
        public BackupInfo(String fileName, String description, long timestamp) {
            this.fileName = fileName;
            this.description = description;
            this.timestamp = timestamp;
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public String getFormattedDate() {
            return DATE_FORMAT.format(new Date(timestamp));
        }
    }
} 