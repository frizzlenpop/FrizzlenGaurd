package org.frizzlenpop.frizzlenGaurd.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RentManager {
    private final FrizzlenGaurd plugin;
    private final Map<String, RentInfo> rentals;
    private final Map<UUID, Set<String>> playerRentals;
    
    public RentManager(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        this.rentals = new ConcurrentHashMap<>();
        this.playerRentals = new ConcurrentHashMap<>();
        loadRentals();
        startRentTask();
    }
    
    public boolean setForRent(Region region, Player owner, double price, long duration) {
        if (!region.isOwner(owner)) {
            return false;
        }
        
        RentInfo info = new RentInfo(region.getName(), owner.getUniqueId(), price, duration);
        rentals.put(region.getName(), info);
        saveRentals();
        return true;
    }
    
    public boolean cancelRent(Region region, Player owner) {
        if (!region.isOwner(owner)) {
            return false;
        }
        
        RentInfo info = rentals.remove(region.getName());
        if (info != null && info.getCurrentTenant() != null) {
            // Remove tenant from region
            region.getMembers().remove(info.getCurrentTenant());
            
            // Remove from player rentals
            Set<String> tenantRentals = playerRentals.get(info.getCurrentTenant());
            if (tenantRentals != null) {
                tenantRentals.remove(region.getName());
                if (tenantRentals.isEmpty()) {
                    playerRentals.remove(info.getCurrentTenant());
                }
            }
            
            // Notify tenant if online
            Player tenant = Bukkit.getPlayer(info.getCurrentTenant());
            if (tenant != null) {
                tenant.sendMessage("§cYour rental of region '" + region.getName() + "' has been cancelled.");
            }
        }
        
        saveRentals();
        return true;
    }
    
    public boolean rentRegion(Region region, Player tenant) {
        RentInfo info = rentals.get(region.getName());
        if (info == null || info.getCurrentTenant() != null) {
            return false;
        }
        
        // Check if player can afford the rent
        if (!plugin.getEconomy().has(tenant, info.getPrice())) {
            return false;
        }
        
        // Charge the player
        plugin.getEconomy().withdrawPlayer(tenant, info.getPrice());
        plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(info.getOwner()), info.getPrice());
        
        // Update rental info
        info.setCurrentTenant(tenant.getUniqueId());
        info.setRentStart(System.currentTimeMillis());
        
        // Add tenant to region
        region.getMembers().put(tenant.getUniqueId(), Region.Role.MEMBER);
        
        // Add to player rentals
        playerRentals.computeIfAbsent(tenant.getUniqueId(), k -> new HashSet<>())
                .add(region.getName());
        
        saveRentals();
        return true;
    }
    
    public boolean isForRent(Region region) {
        return rentals.containsKey(region.getName());
    }
    
    public RentInfo getRentInfo(Region region) {
        return rentals.get(region.getName());
    }
    
    public Set<String> getPlayerRentals(UUID playerId) {
        return playerRentals.getOrDefault(playerId, Collections.emptySet());
    }
    
    private void startRentTask() {
        // Check rentals every minute
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            
            for (RentInfo info : new ArrayList<>(rentals.values())) {
                if (info.getCurrentTenant() != null && 
                    now - info.getRentStart() >= info.getDuration()) {
                    
                    // Process on main thread
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Region region = plugin.getRegionManager().getRegion(info.getRegionName());
                        if (region != null) {
                            // Remove tenant
                            region.getMembers().remove(info.getCurrentTenant());
                            
                            // Remove from player rentals
                            Set<String> tenantRentals = playerRentals.get(info.getCurrentTenant());
                            if (tenantRentals != null) {
                                tenantRentals.remove(region.getName());
                                if (tenantRentals.isEmpty()) {
                                    playerRentals.remove(info.getCurrentTenant());
                                }
                            }
                            
                            // Reset rental info
                            info.setCurrentTenant(null);
                            info.setRentStart(0);
                            
                            // Notify players
                            Player tenant = Bukkit.getPlayer(info.getCurrentTenant());
                            if (tenant != null) {
                                tenant.sendMessage("§cYour rental of region '" + region.getName() + 
                                        "' has expired.");
                            }
                            
                            Player owner = Bukkit.getPlayer(info.getOwner());
                            if (owner != null) {
                                owner.sendMessage("§eRental of region '" + region.getName() + 
                                        "' has expired.");
                            }
                            
                            saveRentals();
                        }
                    });
                }
            }
        }, 1200L, 1200L); // Run every minute (20 ticks * 60)
    }
    
    private void loadRentals() {
        File rentalsFile = new File(plugin.getDataFolder(), "rentals.yml");
        if (!rentalsFile.exists()) {
            return;
        }
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(rentalsFile);
        if (!config.contains("rentals")) {
            return;
        }
        
        for (String regionName : config.getConfigurationSection("rentals").getKeys(false)) {
            String path = "rentals." + regionName;
            
            UUID owner = UUID.fromString(config.getString(path + ".owner"));
            double price = config.getDouble(path + ".price");
            long duration = config.getLong(path + ".duration");
            
            RentInfo info = new RentInfo(regionName, owner, price, duration);
            
            if (config.contains(path + ".tenant")) {
                UUID tenant = UUID.fromString(config.getString(path + ".tenant"));
                long rentStart = config.getLong(path + ".rentStart");
                info.setCurrentTenant(tenant);
                info.setRentStart(rentStart);
                
                // Add to player rentals
                playerRentals.computeIfAbsent(tenant, k -> new HashSet<>())
                        .add(regionName);
            }
            
            rentals.put(regionName, info);
        }
    }
    
    private void saveRentals() {
        File rentalsFile = new File(plugin.getDataFolder(), "rentals.yml");
        YamlConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<String, RentInfo> entry : rentals.entrySet()) {
            String regionName = entry.getKey();
            RentInfo info = entry.getValue();
            String path = "rentals." + regionName;
            
            config.set(path + ".owner", info.getOwner().toString());
            config.set(path + ".price", info.getPrice());
            config.set(path + ".duration", info.getDuration());
            
            if (info.getCurrentTenant() != null) {
                config.set(path + ".tenant", info.getCurrentTenant().toString());
                config.set(path + ".rentStart", info.getRentStart());
            }
        }
        
        try {
            config.save(rentalsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save rentals: " + e.getMessage());
        }
    }
    
    public static class RentInfo {
        private final String regionName;
        private final UUID owner;
        private final double price;
        private final long duration;
        private UUID currentTenant;
        private long rentStart;
        
        public RentInfo(String regionName, UUID owner, double price, long duration) {
            this.regionName = regionName;
            this.owner = owner;
            this.price = price;
            this.duration = duration;
        }
        
        public String getRegionName() {
            return regionName;
        }
        
        public UUID getOwner() {
            return owner;
        }
        
        public double getPrice() {
            return price;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public UUID getCurrentTenant() {
            return currentTenant;
        }
        
        public void setCurrentTenant(UUID currentTenant) {
            this.currentTenant = currentTenant;
        }
        
        public long getRentStart() {
            return rentStart;
        }
        
        public void setRentStart(long rentStart) {
            this.rentStart = rentStart;
        }
        
        public long getTimeRemaining() {
            if (currentTenant == null) {
                return 0;
            }
            return Math.max(0, duration - (System.currentTimeMillis() - rentStart));
        }
    }
} 