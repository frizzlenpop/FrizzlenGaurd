package org.frizzlenpop.frizzlenGaurd.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Region implements ConfigurationSerializable {
    private final String id;
    private String name;
    private UUID owner;
    private final Map<UUID, Role> members = new HashMap<>();
    private final Map<String, Boolean> flags = new HashMap<>();
    private final List<Region> subregions;
    private final List<LogEntry> logs;
    
    // Boundary coordinates
    private final String worldName;
    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;
    
    // Parent region (null for main regions)
    private Region parent;
    
    private final Map<UUID, Role> owners = new HashMap<>();
    private Location minPoint;
    private Location maxPoint;
    private World world;
    
    public Region(String id, String name, UUID owner, Location pos1, Location pos2) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.subregions = new ArrayList<>();
        this.logs = new ArrayList<>();
        
        // Set boundary coordinates
        this.worldName = pos1.getWorld().getName();
        this.minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        // Set default flags
        initDefaultFlags();
    }
    
    public Region(String name, Location minPoint, Location maxPoint) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
        this.world = minPoint.getWorld();
        this.worldName = world.getName();
        this.subregions = new ArrayList<>();
        this.logs = new ArrayList<>();
        
        // Set boundary coordinates
        this.minX = Math.min(minPoint.getBlockX(), maxPoint.getBlockX());
        this.minY = Math.min(minPoint.getBlockY(), maxPoint.getBlockY());
        this.minZ = Math.min(minPoint.getBlockZ(), maxPoint.getBlockZ());
        this.maxX = Math.max(minPoint.getBlockX(), maxPoint.getBlockX());
        this.maxY = Math.max(minPoint.getBlockY(), maxPoint.getBlockY());
        this.maxZ = Math.max(minPoint.getBlockZ(), maxPoint.getBlockZ());
        
        // Set default flags
        initDefaultFlags();
    }
    
    private void initDefaultFlags() {
        FrizzlenGaurd plugin = FrizzlenGaurd.getInstance();
        if (plugin != null && plugin.getConfigManager() != null) {
            ConfigurationSection defaultFlags = plugin.getConfigManager().getMainConfig()
                    .getConfigurationSection("protection.default-flags");
            
            if (defaultFlags != null) {
                for (String key : defaultFlags.getKeys(false)) {
                    flags.put(key, defaultFlags.getBoolean(key));
                }
            }
        }
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public void setOwner(UUID owner) {
        this.owner = owner;
    }
    
    public Map<UUID, Role> getMembers() {
        return members;
    }
    
    public void addMember(UUID playerId, Role role) {
        members.put(playerId, role);
    }
    
    public void removeMember(UUID playerId) {
        members.remove(playerId);
    }
    
    public Role getMemberRole(UUID playerId) {
        if (playerId.equals(owner)) {
            return Role.OWNER;
        }
        return members.getOrDefault(playerId, Role.VISITOR);
    }
    
    public boolean isMember(UUID playerId) {
        return playerId.equals(owner) || members.containsKey(playerId);
    }
    
    public Map<String, Boolean> getFlags() {
        return flags;
    }
    
    public boolean getFlag(String flagName) {
        return flags.getOrDefault(flagName, false);
    }
    
    public void setFlag(String flagName, boolean value) {
        flags.put(flagName, value);
    }
    
    public List<Region> getSubregions() {
        return subregions;
    }
    
    public void addSubregion(Region subregion) {
        subregion.parent = this;
        subregions.add(subregion);
    }
    
    public void removeSubregion(Region subregion) {
        subregions.remove(subregion);
    }
    
    public Region getParent() {
        return parent;
    }
    
    public boolean isSubregion() {
        return parent != null;
    }
    
    public List<LogEntry> getLogs() {
        return logs;
    }
    
    public void addLogEntry(LogEntry entry) {
        logs.add(entry);
        
        // Trim logs if they exceed the maximum age
        long maxAgeMillis = getMaxLogAge();
        if (maxAgeMillis > 0) {
            long cutoffTime = System.currentTimeMillis() - maxAgeMillis;
            logs.removeIf(log -> log.getTimestamp() < cutoffTime);
        }
    }
    
    private long getMaxLogAge() {
        FrizzlenGaurd plugin = FrizzlenGaurd.getInstance();
        if (plugin != null && plugin.getConfigManager() != null) {
            int days = plugin.getConfigManager().getMainConfig().getInt("logging.max-log-age-days", 30);
            return days * 24 * 60 * 60 * 1000L; // Convert days to milliseconds
        }
        return 30 * 24 * 60 * 60 * 1000L; // Default: 30 days
    }
    
    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }
    
    public String getWorldName() {
        return worldName;
    }
    
    public int getMinX() {
        return minX;
    }
    
    public int getMinY() {
        return minY;
    }
    
    public int getMinZ() {
        return minZ;
    }
    
    public int getMaxX() {
        return maxX;
    }
    
    public int getMaxY() {
        return maxY;
    }
    
    public int getMaxZ() {
        return maxZ;
    }
    
    public int getVolume() {
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }
    
    public boolean contains(Location location) {
        if (!location.getWorld().getName().equals(worldName)) {
            return false;
        }
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        return x >= minX && x <= maxX && 
               y >= minY && y <= maxY && 
               z >= minZ && z <= maxZ;
    }
    
    public boolean overlaps(Region other) {
        if (!worldName.equals(other.worldName)) {
            return false;
        }
        
        return !(maxX < other.minX || minX > other.maxX ||
                 maxY < other.minY || minY > other.maxY ||
                 maxZ < other.minZ || minZ > other.maxZ);
    }
    
    public boolean canBuild(Player player) {
        return hasPermission(player, Role::canBuild);
    }
    
    public boolean canInteract(Player player) {
        return hasPermission(player, Role::canInteract);
    }
    
    public boolean canAccessContainers(Player player) {
        return hasPermission(player, Role::canContainer);
    }
    
    public boolean canModifyFlags(Player player) {
        return hasPermission(player, Role::canModifyFlags);
    }
    
    private boolean hasPermission(Player player, Function<Role, Boolean> permissionCheck) {
        if (player.hasPermission("frizzlengaurd.admin.*")) {
            return true;
        }
        
        Role role = getRole(player);
        return permissionCheck.apply(role);
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("owner", owner.toString());
        result.put("world", worldName);
        result.put("minX", minX);
        result.put("minY", minY);
        result.put("minZ", minZ);
        result.put("maxX", maxX);
        result.put("maxY", maxY);
        result.put("maxZ", maxZ);
        
        // Serialize members
        Map<String, String> serializedMembers = new HashMap<>();
        for (Map.Entry<UUID, Role> entry : members.entrySet()) {
            serializedMembers.put(entry.getKey().toString(), entry.getValue().name());
        }
        result.put("members", serializedMembers);
        
        // Serialize flags
        result.put("flags", flags);
        
        // Serialize subregions
        List<Map<String, Object>> serializedSubregions = subregions.stream()
                .map(Region::serialize)
                .collect(Collectors.toList());
        result.put("subregions", serializedSubregions);
        
        // Serialize logs
        List<Map<String, Object>> serializedLogs = logs.stream()
                .map(LogEntry::serialize)
                .collect(Collectors.toList());
        result.put("logs", serializedLogs);
        
        return result;
    }
    
    public static Region deserialize(Map<String, Object> data) {
        String id = (String) data.get("id");
        String name = (String) data.get("name");
        UUID owner = UUID.fromString((String) data.get("owner"));
        String worldName = (String) data.get("world");
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new IllegalArgumentException("World not found: " + worldName);
        }
        
        int minX = (int) data.get("minX");
        int minY = (int) data.get("minY");
        int minZ = (int) data.get("minZ");
        int maxX = (int) data.get("maxX");
        int maxY = (int) data.get("maxY");
        int maxZ = (int) data.get("maxZ");
        
        Location pos1 = new Location(world, minX, minY, minZ);
        Location pos2 = new Location(world, maxX, maxY, maxZ);
        
        Region region = new Region(id, name, owner, pos1, pos2);
        
        // Deserialize members
        Map<String, String> serializedMembers = (Map<String, String>) data.get("members");
        if (serializedMembers != null) {
            for (Map.Entry<String, String> entry : serializedMembers.entrySet()) {
                UUID memberId = UUID.fromString(entry.getKey());
                Role role = Role.valueOf(entry.getValue());
                region.addMember(memberId, role);
            }
        }
        
        // Deserialize flags
        Map<String, Boolean> flags = (Map<String, Boolean>) data.get("flags");
        if (flags != null) {
            region.flags.putAll(flags);
        }
        
        // Deserialize subregions
        List<Map<String, Object>> serializedSubregions = (List<Map<String, Object>>) data.get("subregions");
        if (serializedSubregions != null) {
            for (Map<String, Object> subregionData : serializedSubregions) {
                Region subregion = Region.deserialize(subregionData);
                region.addSubregion(subregion);
            }
        }
        
        // Deserialize logs
        List<Map<String, Object>> serializedLogs = (List<Map<String, Object>>) data.get("logs");
        if (serializedLogs != null) {
            for (Map<String, Object> logData : serializedLogs) {
                LogEntry logEntry = LogEntry.deserialize(logData);
                region.logs.add(logEntry);
            }
        }
        
        return region;
    }
    
    public Role getRole(Player player) {
        UUID playerId = player.getUniqueId();
        if (owners.containsKey(playerId)) {
            return Role.OWNER;
        } else if (members.containsKey(playerId)) {
            return Role.MEMBER;
        }
        return Role.VISITOR;
    }
    
    public Location getMinPoint() {
        return minPoint;
    }
    
    public Location getMaxPoint() {
        return maxPoint;
    }
    
    public Map<UUID, Role> getOwners() {
        return owners;
    }
    
    public void setOwners(Map<UUID, Role> owners) {
        this.owners.clear();
        this.owners.putAll(owners);
    }
    
    public boolean isOwner(Player player) {
        return owners.containsKey(player.getUniqueId());
    }
    
    public void addOwner(UUID playerId) {
        owners.put(playerId, Role.OWNER);
    }
    
    public void addMember(UUID playerId) {
        members.put(playerId, Role.MEMBER);
    }
    
    public void setMembers(Map<UUID, Role> members) {
        this.members.clear();
        this.members.putAll(members);
    }
    
    public void setFlags(Map<String, Boolean> flags) {
        this.flags.clear();
        this.flags.putAll(flags);
    }
    
    public enum Role {
        OWNER(true, true, true, true),
        MEMBER(true, true, true, false),
        VISITOR(false, false, false, false);

        private final boolean canBuild;
        private final boolean canInteract;
        private final boolean canContainer;
        private final boolean canModifyFlags;

        Role(boolean canBuild, boolean canInteract, boolean canContainer, boolean canModifyFlags) {
            this.canBuild = canBuild;
            this.canInteract = canInteract;
            this.canContainer = canContainer;
            this.canModifyFlags = canModifyFlags;
        }

        public boolean canBuild() {
            return canBuild;
        }

        public boolean canInteract() {
            return canInteract;
        }

        public boolean canContainer() {
            return canContainer;
        }

        public boolean canModifyFlags() {
            return canModifyFlags;
        }
    }
} 