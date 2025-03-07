package org.frizzlenpop.frizzlenGaurd.models;

import org.bukkit.ChatColor;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;

public enum Role {
    OWNER("owner", ChatColor.RED),
    CO_OWNER("co-owner", ChatColor.GOLD),
    TRUSTED("trusted", ChatColor.GREEN),
    VISITOR("visitor", ChatColor.GRAY);
    
    private final String configKey;
    private final ChatColor defaultColor;
    
    Role(String configKey, ChatColor defaultColor) {
        this.configKey = configKey;
        this.defaultColor = defaultColor;
    }
    
    public String getConfigKey() {
        return configKey;
    }
    
    public ChatColor getDefaultColor() {
        return defaultColor;
    }
    
    public ChatColor getColor() {
        FrizzlenGaurd plugin = FrizzlenGaurd.getInstance();
        if (plugin == null || plugin.getConfigManager() == null) {
            return defaultColor;
        }
        
        String colorStr = plugin.getConfigManager().getMainConfig()
                .getString("roles." + configKey + ".color", defaultColor.toString());
        
        try {
            return ChatColor.valueOf(colorStr.replace("&", "").toUpperCase());
        } catch (IllegalArgumentException e) {
            return ChatColor.getByChar(colorStr.replace("&", "").charAt(0));
        }
    }
    
    public boolean canBuild() {
        return getPermission("can-build", true);
    }
    
    public boolean canInteract() {
        return getPermission("can-interact", true);
    }
    
    public boolean canContainer() {
        return getPermission("can-container", this != VISITOR);
    }
    
    public boolean canTeleport() {
        return getPermission("can-teleport", true);
    }
    
    public boolean canModifyFlags() {
        return getPermission("can-modify-flags", this == OWNER || this == CO_OWNER);
    }
    
    public boolean canInvite() {
        return getPermission("can-invite", this == OWNER || this == CO_OWNER);
    }
    
    public boolean canKick() {
        return getPermission("can-kick", this == OWNER || this == CO_OWNER);
    }
    
    public boolean canCreateSubregion() {
        return getPermission("can-create-subregion", this == OWNER);
    }
    
    private boolean getPermission(String permission, boolean defaultValue) {
        FrizzlenGaurd plugin = FrizzlenGaurd.getInstance();
        if (plugin == null || plugin.getConfigManager() == null) {
            return defaultValue;
        }
        
        return plugin.getConfigManager().getMainConfig()
                .getBoolean("roles." + configKey + "." + permission, defaultValue);
    }
    
    public static Role fromString(String roleName) {
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(roleName) || 
                    role.configKey.equalsIgnoreCase(roleName)) {
                return role;
            }
        }
        return VISITOR; // Default role
    }
} 