package org.frizzlenpop.frizzlenGaurd.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final FrizzlenGaurd plugin;
    private final Map<String, FileConfiguration> configs;
    private FileConfiguration mainConfig;
    private FileConfiguration messagesConfig;
    
    public ConfigManager(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
    }
    
    public void loadConfigs() {
        // Create plugin directory if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Load main config
        loadMainConfig();
        
        // Load messages config
        loadMessagesConfig();
        
        Logger.info("All configurations loaded successfully.");
    }
    
    private void loadMainConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        mainConfig = YamlConfiguration.loadConfiguration(configFile);
        configs.put("config", mainConfig);
        
        // Update config with any new values
        boolean updated = false;
        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(plugin.getResource("config.yml")));
        
        for (String key : defaultConfig.getKeys(true)) {
            if (!mainConfig.contains(key)) {
                mainConfig.set(key, defaultConfig.get(key));
                updated = true;
            }
        }
        
        if (updated) {
            try {
                mainConfig.save(configFile);
                Logger.info("Updated config.yml with new values.");
            } catch (IOException e) {
                Logger.error("Failed to save updated config.yml: " + e.getMessage());
            }
        }
    }
    
    private void loadMessagesConfig() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        configs.put("messages", messagesConfig);
        
        // Update messages with any new values
        boolean updated = false;
        FileConfiguration defaultMessages = YamlConfiguration.loadConfiguration(
                new InputStreamReader(plugin.getResource("messages.yml")));
        
        for (String key : defaultMessages.getKeys(true)) {
            if (!messagesConfig.contains(key)) {
                messagesConfig.set(key, defaultMessages.get(key));
                updated = true;
            }
        }
        
        if (updated) {
            try {
                messagesConfig.save(messagesFile);
                Logger.info("Updated messages.yml with new values.");
            } catch (IOException e) {
                Logger.error("Failed to save updated messages.yml: " + e.getMessage());
            }
        }
    }
    
    public FileConfiguration getConfig(String name) {
        return configs.get(name);
    }
    
    public FileConfiguration getMainConfig() {
        return mainConfig;
    }
    
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    
    public void reloadConfigs() {
        loadConfigs();
    }
    
    public void saveConfig(String name) {
        FileConfiguration config = configs.get(name);
        if (config == null) {
            Logger.error("Attempted to save non-existent config: " + name);
            return;
        }
        
        try {
            File configFile = new File(plugin.getDataFolder(), name + ".yml");
            config.save(configFile);
            Logger.info("Saved " + name + ".yml successfully.");
        } catch (IOException e) {
            Logger.error("Failed to save " + name + ".yml: " + e.getMessage());
        }
    }
} 