package org.frizzlenpop.frizzlenGaurd.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LogEntry implements ConfigurationSerializable {
    private final UUID playerId;
    private final String playerName;
    private final LogAction action;
    private final long timestamp;
    private final String worldName;
    private final int x, y, z;
    private final String details;
    private final BlockState previousState;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public LogEntry(Player player, LogAction action, String details, BlockState previousState) {
        this.playerId = player.getUniqueId();
        this.playerName = player.getName();
        this.action = action;
        this.timestamp = System.currentTimeMillis();
        
        Location loc = player.getLocation();
        this.worldName = loc.getWorld().getName();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        
        this.details = details;
        this.previousState = previousState;
    }
    
    public LogEntry(UUID playerId, String playerName, LogAction action, long timestamp, 
                   String worldName, int x, int y, int z, String details, BlockState previousState) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.action = action;
        this.timestamp = timestamp;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.details = details;
        this.previousState = previousState;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public LogAction getAction() {
        return action;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getFormattedTimestamp() {
        return DATE_FORMAT.format(new Date(timestamp));
    }
    
    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    public Location getLocation() {
        World world = getWorld();
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z);
    }
    
    public String getDetails() {
        return details;
    }
    
    public BlockState getPreviousState() {
        return previousState;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s %s at %d,%d,%d in %s: %s", 
                getFormattedTimestamp(), playerName, action.name(), x, y, z, worldName, details);
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("playerId", playerId.toString());
        result.put("playerName", playerName);
        result.put("action", action.name());
        result.put("timestamp", timestamp);
        result.put("world", worldName);
        result.put("x", x);
        result.put("y", y);
        result.put("z", z);
        result.put("details", details);
        result.put("previousState", previousState);
        return result;
    }
    
    public static LogEntry deserialize(Map<String, Object> data) {
        UUID playerId = UUID.fromString((String) data.get("playerId"));
        String playerName = (String) data.get("playerName");
        LogAction action = LogAction.valueOf((String) data.get("action"));
        long timestamp = (long) data.get("timestamp");
        String worldName = (String) data.get("world");
        int x = (int) data.get("x");
        int y = (int) data.get("y");
        int z = (int) data.get("z");
        String details = (String) data.get("details");
        BlockState previousState = (BlockState) data.get("previousState");
        
        return new LogEntry(playerId, playerName, action, timestamp, worldName, x, y, z, details, previousState);
    }
    
    public enum LogAction {
        BLOCK_BREAK,
        BLOCK_PLACE,
        INTERACT,
        CONTAINER_ACCESS,
        CLAIM_CREATE,
        CLAIM_DELETE,
        CLAIM_MODIFY,
        MEMBER_ADD,
        MEMBER_REMOVE,
        FLAG_CHANGE,
        ADMIN_ACTION
    }
} 