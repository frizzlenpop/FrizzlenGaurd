package org.frizzlenpop.frizzlenGaurd.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationManager {
    private final FrizzlenGaurd plugin;
    private final Map<UUID, NotificationSettings> playerSettings;
    private final Map<UUID, Long> lastNotificationTime;
    private static final long NOTIFICATION_COOLDOWN = 5000; // 5 seconds
    
    public NotificationManager(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        this.playerSettings = new ConcurrentHashMap<>();
        this.lastNotificationTime = new ConcurrentHashMap<>();
        loadSettings();
    }
    
    public void notifyRegionOwners(Region region, String message, Location location) {
        long now = System.currentTimeMillis();
        
        // Notify owners and members based on their settings
        Set<UUID> notified = new HashSet<>();
        
        // Notify owners first
        for (UUID ownerId : region.getOwners().keySet()) {
            Player owner = Bukkit.getPlayer(ownerId);
            if (owner != null && owner.isOnline()) {
                NotificationSettings settings = getSettings(ownerId);
                if (settings.isEnabled() && canNotify(ownerId, now)) {
                    sendNotification(owner, message, location, settings);
                    notified.add(ownerId);
                }
            }
        }
        
        // Then notify members if they have permission
        for (UUID memberId : region.getMembers().keySet()) {
            if (!notified.contains(memberId)) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline() && 
                    member.hasPermission("frizzlengaurd.notify.member")) {
                    NotificationSettings settings = getSettings(memberId);
                    if (settings.isEnabled() && canNotify(memberId, now)) {
                        sendNotification(member, message, location, settings);
                    }
                }
            }
        }
    }
    
    private void sendNotification(Player player, String message, Location location, NotificationSettings settings) {
        // Update last notification time
        lastNotificationTime.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Send chat message if enabled
        if (settings.isChatEnabled()) {
            player.sendMessage(ChatColor.RED + "[FrizzlenGaurd] " + message);
        }
        
        // Send title if enabled
        if (settings.isTitleEnabled()) {
            player.sendTitle(
                ChatColor.RED + "Region Alert",
                ChatColor.YELLOW + message,
                10, 40, 10
            );
        }
        
        // Play sound if enabled
        if (settings.isSoundEnabled()) {
            player.playSound(player.getLocation(), settings.getAlertSound(), 1.0f, 1.0f);
        }
        
        // Show location if enabled
        if (settings.isLocationEnabled() && location != null) {
            String locationMsg = String.format(
                ChatColor.GRAY + "Location: %d, %d, %d in %s",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getWorld().getName()
            );
            player.sendMessage(locationMsg);
        }
    }
    
    private boolean canNotify(UUID playerId, long now) {
        Long lastTime = lastNotificationTime.get(playerId);
        return lastTime == null || now - lastTime >= NOTIFICATION_COOLDOWN;
    }
    
    public NotificationSettings getSettings(UUID playerId) {
        return playerSettings.computeIfAbsent(playerId, id -> new NotificationSettings());
    }
    
    public void updateSettings(UUID playerId, NotificationSettings settings) {
        playerSettings.put(playerId, settings);
        saveSettings();
    }
    
    private void loadSettings() {
        // Load settings from config
        if (plugin.getConfig().contains("notifications")) {
            for (String uuidStr : plugin.getConfig().getConfigurationSection("notifications").getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(uuidStr);
                    String path = "notifications." + uuidStr + ".";
                    
                    NotificationSettings settings = new NotificationSettings();
                    settings.setEnabled(plugin.getConfig().getBoolean(path + "enabled", true));
                    settings.setChatEnabled(plugin.getConfig().getBoolean(path + "chat", true));
                    settings.setTitleEnabled(plugin.getConfig().getBoolean(path + "title", true));
                    settings.setSoundEnabled(plugin.getConfig().getBoolean(path + "sound", true));
                    settings.setLocationEnabled(plugin.getConfig().getBoolean(path + "location", true));
                    
                    playerSettings.put(playerId, settings);
                } catch (IllegalArgumentException ignored) {
                    // Invalid UUID, skip it
                }
            }
        }
    }
    
    private void saveSettings() {
        // Save settings to config
        for (Map.Entry<UUID, NotificationSettings> entry : playerSettings.entrySet()) {
            String path = "notifications." + entry.getKey() + ".";
            NotificationSettings settings = entry.getValue();
            
            plugin.getConfig().set(path + "enabled", settings.isEnabled());
            plugin.getConfig().set(path + "chat", settings.isChatEnabled());
            plugin.getConfig().set(path + "title", settings.isTitleEnabled());
            plugin.getConfig().set(path + "sound", settings.isSoundEnabled());
            plugin.getConfig().set(path + "location", settings.isLocationEnabled());
        }
        
        plugin.saveConfig();
    }
    
    public static class NotificationSettings {
        private boolean enabled = true;
        private boolean chatEnabled = true;
        private boolean titleEnabled = true;
        private boolean soundEnabled = true;
        private boolean locationEnabled = true;
        private String alertSound = "BLOCK_NOTE_BLOCK_PLING";
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean isChatEnabled() {
            return chatEnabled;
        }
        
        public void setChatEnabled(boolean chatEnabled) {
            this.chatEnabled = chatEnabled;
        }
        
        public boolean isTitleEnabled() {
            return titleEnabled;
        }
        
        public void setTitleEnabled(boolean titleEnabled) {
            this.titleEnabled = titleEnabled;
        }
        
        public boolean isSoundEnabled() {
            return soundEnabled;
        }
        
        public void setSoundEnabled(boolean soundEnabled) {
            this.soundEnabled = soundEnabled;
        }
        
        public boolean isLocationEnabled() {
            return locationEnabled;
        }
        
        public void setLocationEnabled(boolean locationEnabled) {
            this.locationEnabled = locationEnabled;
        }
        
        public String getAlertSound() {
            return alertSound;
        }
        
        public void setAlertSound(String alertSound) {
            this.alertSound = alertSound;
        }
    }
} 