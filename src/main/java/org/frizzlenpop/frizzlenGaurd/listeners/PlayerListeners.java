package org.frizzlenpop.frizzlenGaurd.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.player.ClaimCommand;
import org.frizzlenpop.frizzlenGaurd.models.LogEntry;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListeners implements Listener {
    private final FrizzlenGaurd plugin;
    private final Map<UUID, Region> lastRegion = new HashMap<>();
    
    public PlayerListeners(FrizzlenGaurd plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Handle stick right-click for region visualization
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && 
                event.getItem() != null && 
                event.getItem().getType() == Material.STICK &&
                plugin.getVisualsManager().shouldShowOnStickRightClick()) {
            
            Location clickedLoc = event.getClickedBlock().getLocation();
            Region region = plugin.getRegionManager().getRegionAt(clickedLoc);
            
            if (region != null) {
                plugin.getVisualsManager().showRegionBoundaries(player, region);
                
                // Get message from config
                String message = ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfigManager().getMessagesConfig().getString("claim.claim-visualized", 
                                "&aShowing claim boundaries for &f%name%&a."));
                message = message.replace("%name%", region.getName());
                player.sendMessage(message);
                
                // Log the interaction if logging is enabled
                if (plugin.getConfigManager().getMainConfig().getBoolean("logging.log-player-interactions", true)) {
                    region.addLogEntry(new LogEntry(player, LogEntry.LogAction.INTERACT, 
                            "Visualized region boundaries", null));
                }
                
                // Prevent the stick from doing anything else
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check block position changes (not looking around)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check for region entry/exit
        Region currentRegion = plugin.getRegionManager().getRegionAt(event.getTo());
        Region previousRegion = lastRegion.get(playerId);
        
        if (currentRegion != previousRegion) {
            // Handle region exit
            if (previousRegion != null) {
                String exitMessage = ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfigManager().getMessagesConfig().getString("claim.exit-claim", 
                                "&7You have left &f%owner%'s &7claim: &f%name%"));
                
                String ownerName = plugin.getServer().getOfflinePlayer(previousRegion.getOwner()).getName();
                if (ownerName == null) ownerName = "Unknown";
                
                exitMessage = exitMessage.replace("%owner%", ownerName)
                                        .replace("%name%", previousRegion.getName());
                
                player.sendMessage(exitMessage);
            }
            
            // Handle region entry
            if (currentRegion != null) {
                String enterMessage = ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfigManager().getMessagesConfig().getString("claim.enter-claim", 
                                "&7You have entered &f%owner%'s &7claim: &f%name%"));
                
                String ownerName = plugin.getServer().getOfflinePlayer(currentRegion.getOwner()).getName();
                if (ownerName == null) ownerName = "Unknown";
                
                enterMessage = enterMessage.replace("%owner%", ownerName)
                                         .replace("%name%", currentRegion.getName());
                
                player.sendMessage(enterMessage);
                
                // Show region boundaries if enabled
                if (plugin.getVisualsManager().shouldShowOnEntry()) {
                    plugin.getVisualsManager().showRegionBoundaries(player, currentRegion);
                }
            }
            
            // Update last region
            lastRegion.put(playerId, currentRegion);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check if player is in a region when they join
        Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
        if (region != null) {
            lastRegion.put(playerId, region);
            
            // Notify player they're in a region
            String enterMessage = ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfigManager().getMessagesConfig().getString("claim.enter-claim", 
                            "&7You have entered &f%owner%'s &7claim: &f%name%"));
            
            String ownerName = plugin.getServer().getOfflinePlayer(region.getOwner()).getName();
            if (ownerName == null) ownerName = "Unknown";
            
            enterMessage = enterMessage.replace("%owner%", ownerName)
                                     .replace("%name%", region.getName());
            
            player.sendMessage(enterMessage);
            
            // Show region boundaries if enabled
            if (plugin.getVisualsManager().shouldShowOnEntry()) {
                plugin.getVisualsManager().showRegionBoundaries(player, region);
            }
        }
        
        // Award claim blocks for time played
        long now = System.currentTimeMillis();
        long lastLogin = player.getLastPlayed();
        if (lastLogin > 0) {
            long timePlayed = now - lastLogin;
            int hoursPlayed = (int) (timePlayed / (1000 * 60 * 60));
            
            if (hoursPlayed > 0) {
                int blocksPerHour = plugin.getConfigManager().getMainConfig().getInt("claims.blocks-accrued-per-hour", 100);
                int blocksToAward = hoursPlayed * blocksPerHour;
                
                if (blocksToAward > 0) {
                    plugin.getDataManager().addPlayerClaimBlocks(playerId, blocksToAward);
                    Logger.debug("Awarded " + blocksToAward + " claim blocks to " + player.getName() + 
                            " for " + hoursPlayed + " hours played.");
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        
        // Clean up cached data
        lastRegion.remove(playerId);
        
        // Clean up any selection points
        ClaimCommand claimCommand = null;
        try {
            claimCommand = (ClaimCommand) plugin.getCommand("fg").getExecutor();
            if (claimCommand != null) {
                claimCommand.clearPoints(playerId);
            }
        } catch (Exception e) {
            // Command handler not initialized or cast failed
            Logger.debug("Failed to clear selection points for " + event.getPlayer().getName());
        }
        
        // Cancel any active visualizations
        plugin.getVisualsManager().cancelVisualization(event.getPlayer());
    }
} 