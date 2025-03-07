package org.frizzlenpop.frizzlenGaurd.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;

import java.util.logging.Level;

public class Logger {
    private static FrizzlenGaurd plugin;
    private static final String PREFIX = "[FrizzlenGaurd] ";
    
    public static void init(FrizzlenGaurd instance) {
        plugin = instance;
    }
    
    public static void info(String message) {
        log(Level.INFO, message);
    }
    
    public static void warning(String message) {
        log(Level.WARNING, message);
    }
    
    public static void error(String message) {
        log(Level.SEVERE, message);
    }
    
    public static void debug(String message) {
        if (plugin != null && plugin.getConfigManager() != null && 
                plugin.getConfigManager().getMainConfig().getBoolean("debug-mode", false)) {
            log(Level.INFO, "[DEBUG] " + message);
        }
    }
    
    private static void log(Level level, String message) {
        if (plugin == null) {
            Bukkit.getLogger().log(level, ChatColor.translateAlternateColorCodes('&', PREFIX + message));
        } else {
            plugin.getLogger().log(level, ChatColor.translateAlternateColorCodes('&', message));
        }
    }
} 