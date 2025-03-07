package org.frizzlenpop.frizzlenGaurd.visuals;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VisualsManager {
    private final FrizzlenGaurd plugin;
    private final Map<UUID, Integer> activeTasks;
    private final Map<UUID, PreviewSession> previewSessions;
    
    public VisualsManager(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        this.activeTasks = new HashMap<>();
        this.previewSessions = new HashMap<>();
    }
    
    /**
     * Show region boundaries to a player
     * 
     * @param player The player to show boundaries to
     * @param region The region to visualize
     * @param durationTicks Duration in ticks
     */
    public void showRegionBoundaries(Player player, Region region, int durationTicks) {
        if (!isVisualsEnabled()) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Cancel any existing visualization for this player
        cancelVisualization(playerId);
        
        // Create new visualization task
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            visualizeRegion(player, region);
        }, 0L, 2L); // Run every 2 ticks for smooth visualization
        
        activeTasks.put(playerId, taskId);
        
        // Schedule task cancellation
        if (durationTicks > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                cancelVisualization(playerId);
            }, durationTicks);
        }
    }
    
    /**
     * Show region boundaries to a player
     * 
     * @param player The player to show boundaries to
     * @param region The region to visualize
     */
    public void showRegionBoundaries(Player player, Region region) {
        showRegionBoundaries(player, region, 0); // Show indefinitely
    }
    
    /**
     * Show preview of a region based on selection points
     * 
     * @param player The player to show preview to
     * @param pos1 First corner position
     * @param pos2 Second corner position
     */
    public void showPreview(Player player, Location pos1, Location pos2) {
        if (!isVisualsEnabled()) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Cancel any existing preview
        cancelPreview(player);
        
        // Create a preview session
        PreviewSession session = new PreviewSession(pos1, pos2);
        previewSessions.put(playerId, session);
        
        // Start visualization task
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                () -> visualizePreview(player, session),
                0,
                10 // Run every 10 ticks (0.5 seconds)
        );
        
        activeTasks.put(playerId, task.getTaskId());
    }
    
    /**
     * Cancel active visualization for a player
     * 
     * @param player The player
     */
    public void cancelVisualization(Player player) {
        cancelVisualization(player.getUniqueId());
    }
    
    /**
     * Cancel preview for a player
     * 
     * @param player The player
     */
    public void cancelPreview(Player player) {
        UUID playerId = player.getUniqueId();
        previewSessions.remove(playerId);
        cancelVisualization(playerId);
    }
    
    /**
     * Visualize a region using particles
     * 
     * @param player The player to show particles to
     * @param region The region to visualize
     */
    private void visualizeRegion(Player player, Region region) {
        if (!player.isOnline() || !player.getWorld().getName().equals(region.getWorldName())) {
            cancelVisualization(player);
            return;
        }
        
        // Get visualization settings
        Particle particleType = getParticleType();
        Color particleColor = getParticleColor();
        int density = getParticleDensity();
        
        // Create particle data for colored particles
        Particle.DustOptions dustOptions = new Particle.DustOptions(particleColor, 1.0f);
        
        // Get region boundaries
        int minX = region.getMinX();
        int minY = region.getMinY();
        int minZ = region.getMinZ();
        int maxX = region.getMaxX();
        int maxY = region.getMaxY();
        int maxZ = region.getMaxZ();
        
        // Calculate step size based on density
        double step = 1.0 / density;
        
        // Visualize the edges of the region
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // Bottom edges
            for (double x = minX; x <= maxX; x += step) {
                spawnParticle(player, particleType, dustOptions, x, minY, minZ);
                spawnParticle(player, particleType, dustOptions, x, minY, maxZ);
            }
            
            for (double z = minZ; z <= maxZ; z += step) {
                spawnParticle(player, particleType, dustOptions, minX, minY, z);
                spawnParticle(player, particleType, dustOptions, maxX, minY, z);
            }
            
            // Top edges
            for (double x = minX; x <= maxX; x += step) {
                spawnParticle(player, particleType, dustOptions, x, maxY, minZ);
                spawnParticle(player, particleType, dustOptions, x, maxY, maxZ);
            }
            
            for (double z = minZ; z <= maxZ; z += step) {
                spawnParticle(player, particleType, dustOptions, minX, maxY, z);
                spawnParticle(player, particleType, dustOptions, maxX, maxY, z);
            }
            
            // Vertical edges
            for (double y = minY; y <= maxY; y += step) {
                spawnParticle(player, particleType, dustOptions, minX, y, minZ);
                spawnParticle(player, particleType, dustOptions, minX, y, maxZ);
                spawnParticle(player, particleType, dustOptions, maxX, y, minZ);
                spawnParticle(player, particleType, dustOptions, maxX, y, maxZ);
            }
            
            // Add corner markers
            spawnCornerMarker(player, minX, minY, minZ);
            spawnCornerMarker(player, minX, minY, maxZ);
            spawnCornerMarker(player, minX, maxY, minZ);
            spawnCornerMarker(player, minX, maxY, maxZ);
            spawnCornerMarker(player, maxX, minY, minZ);
            spawnCornerMarker(player, maxX, minY, maxZ);
            spawnCornerMarker(player, maxX, maxY, minZ);
            spawnCornerMarker(player, maxX, maxY, maxZ);
        });
    }
    
    /**
     * Visualize a preview selection
     * 
     * @param player The player to show the preview to
     * @param session The preview session
     */
    private void visualizePreview(Player player, PreviewSession session) {
        if (!player.isOnline()) {
            cancelVisualization(player);
            return;
        }
        
        // Get locations
        Location pos1 = session.getPos1();
        Location pos2 = session.getPos2();
        
        // Make sure they're in the same world
        if (!pos1.getWorld().equals(pos2.getWorld()) || !player.getWorld().equals(pos1.getWorld())) {
            cancelVisualization(player);
            return;
        }
        
        // Get visualization settings
        Particle particleType = Particle.DUST;
        Color particleColor = Color.YELLOW; // Preview uses yellow
        int density = getParticleDensity();
        
        // Create particle data
        Particle.DustOptions dustOptions = new Particle.DustOptions(particleColor, 1.0f);
        
        // Get boundaries
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        
        // Calculate step size based on density
        double step = 1.0 / density;
        
        // Visualize the edges
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // Add holographic markers at corners
            spawnCornerMarker(player, minX, minY, minZ);
            spawnCornerMarker(player, minX, minY, maxZ);
            spawnCornerMarker(player, minX, maxY, minZ);
            spawnCornerMarker(player, minX, maxY, maxZ);
            spawnCornerMarker(player, maxX, minY, minZ);
            spawnCornerMarker(player, maxX, minY, maxZ);
            spawnCornerMarker(player, maxX, maxY, minZ);
            spawnCornerMarker(player, maxX, maxY, maxZ);
            
            // Bottom edges
            for (double x = minX; x <= maxX; x += step) {
                spawnParticle(player, particleType, dustOptions, x, minY, minZ);
                spawnParticle(player, particleType, dustOptions, x, minY, maxZ);
            }
            
            for (double z = minZ; z <= maxZ; z += step) {
                spawnParticle(player, particleType, dustOptions, minX, minY, z);
                spawnParticle(player, particleType, dustOptions, maxX, minY, z);
            }
            
            // Top edges
            for (double x = minX; x <= maxX; x += step) {
                spawnParticle(player, particleType, dustOptions, x, maxY, minZ);
                spawnParticle(player, particleType, dustOptions, x, maxY, maxZ);
            }
            
            for (double z = minZ; z <= maxZ; z += step) {
                spawnParticle(player, particleType, dustOptions, minX, maxY, z);
                spawnParticle(player, particleType, dustOptions, maxX, maxY, z);
            }
            
            // Vertical edges
            for (double y = minY; y <= maxY; y += step) {
                spawnParticle(player, particleType, dustOptions, minX, y, minZ);
                spawnParticle(player, particleType, dustOptions, minX, y, maxZ);
                spawnParticle(player, particleType, dustOptions, maxX, y, minZ);
                spawnParticle(player, particleType, dustOptions, maxX, y, maxZ);
            }
            
            // Send size information
            int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
            int area = (maxX - minX + 1) * (maxZ - minZ + 1);
            player.sendActionBar("§eArea: §f" + area + " §eVolume: §f" + volume);
        });
    }
    
    /**
     * Spawn corner marker at a position
     * 
     * @param player The player to show the marker to
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    private void spawnCornerMarker(Player player, double x, double y, double z) {
        // Create a more noticeable corner marker with multiple particles
        Location loc = new Location(player.getWorld(), x, y, z);
        
        player.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
        
        // Add some glow effect
        Particle.DustOptions glowOptions = new Particle.DustOptions(Color.WHITE, 0.7f);
        for (int i = 0; i < 5; i++) {
            player.spawnParticle(Particle.DUST, loc, 1, 0.1, 0.1, 0.1, 0, glowOptions);
        }
    }
    
    /**
     * Spawn a particle at the specified position
     *
     * @param player The player to show the particle to
     * @param particleType The type of particle
     * @param dustOptions Options for dust particles
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    private void spawnParticle(Player player, Particle particleType, Particle.DustOptions dustOptions, double x, double y, double z) {
        Location loc = new Location(player.getWorld(), x, y, z);
        
        if (particleType == Particle.DUST) {
            player.spawnParticle(particleType, loc, 1, 0, 0, 0, 0, dustOptions);
        } else {
            player.spawnParticle(particleType, loc, 1, 0, 0, 0, 0);
        }
    }
    
    /**
     * Check if the visuals are enabled in the config
     *
     * @return true if visuals are enabled
     */
    private boolean isVisualsEnabled() {
        return plugin.getConfigManager().getMainConfig().getBoolean("visuals.enabled", true);
    }
    
    /**
     * Get the particle type from the config
     *
     * @return The particle type
     */
    private Particle getParticleType() {
        String particleTypeName = plugin.getConfigManager().getMainConfig()
                .getString("visuals.particle-type", "REDSTONE");
        
        try {
            // Convert legacy "REDSTONE" to "DUST"
            if ("REDSTONE".equalsIgnoreCase(particleTypeName)) {
                return Particle.DUST;
            }
            return Particle.valueOf(particleTypeName);
        } catch (IllegalArgumentException e) {
            Logger.warning("Invalid particle type: " + particleTypeName + ". Using DUST instead.");
            return Particle.DUST;
        }
    }
    
    /**
     * Get the particle color from the config
     *
     * @return The particle color
     */
    private Color getParticleColor() {
        String colorName = plugin.getConfigManager().getMainConfig()
                .getString("visuals.particle-color", "RED");
        
        switch (colorName.toUpperCase()) {
            case "RED":
                return Color.RED;
            case "GREEN":
                return Color.GREEN;
            case "BLUE":
                return Color.BLUE;
            case "YELLOW":
                return Color.YELLOW;
            case "PURPLE":
                return Color.PURPLE;
            case "ORANGE":
                return Color.ORANGE;
            case "WHITE":
                return Color.WHITE;
            default:
                Logger.warning("Invalid particle color: " + colorName + ". Using RED instead.");
                return Color.RED;
        }
    }
    
    /**
     * Get the particle density from the config
     *
     * @return The particle density
     */
    private int getParticleDensity() {
        return plugin.getConfigManager().getMainConfig().getInt("visuals.particle-density", 2);
    }
    
    /**
     * Check if visuals should be shown when entering a region
     *
     * @return true if they should be shown
     */
    public boolean shouldShowOnEntry() {
        return plugin.getConfigManager().getMainConfig().getBoolean("visuals.show-on-entry", true);
    }
    
    /**
     * Check if visuals should be shown when right-clicking with a stick
     *
     * @return true if they should be shown
     */
    public boolean shouldShowOnStickRightClick() {
        return plugin.getConfigManager().getMainConfig().getBoolean("visuals.show-on-stick-right-click", true);
    }
    
    /**
     * Preview session class to store selection points
     */
    public static class PreviewSession {
        private final Location pos1;
        private final Location pos2;
        
        public PreviewSession(Location pos1, Location pos2) {
            this.pos1 = pos1;
            this.pos2 = pos2;
        }
        
        public Location getPos1() {
            return pos1;
        }
        
        public Location getPos2() {
            return pos2;
        }
    }

    public void cancelVisualization(UUID playerId) {
        Integer taskId = activeTasks.remove(playerId);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
} 