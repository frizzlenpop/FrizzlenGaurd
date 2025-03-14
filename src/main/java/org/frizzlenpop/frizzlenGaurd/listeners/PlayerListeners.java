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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.player.ClaimCommand;
import org.frizzlenpop.frizzlenGaurd.models.LogEntry;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.utils.Logger;
import org.frizzlenpop.frizzlenGaurd.commands.CommandHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListeners implements Listener {
    private final FrizzlenGaurd plugin;
    private final Map<UUID, Region> lastRegion = new HashMap<>();
    public static final String SELECTION_STICK_NAME = ChatColor.GOLD + "Claim Selection Tool";
    
    public PlayerListeners(FrizzlenGaurd plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Debug logging for stick interactions
        if (item != null && item.getType() == Material.STICK) {
            Logger.debug("Player " + player.getName() + " interacted with a stick - Action: " + event.getAction());
            
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                Logger.debug("Stick has display name: " + item.getItemMeta().getDisplayName());
            }
        }
        
        // Check if the player is using a selection stick
        if (item != null && item.getType() == Material.STICK && 
                item.hasItemMeta() && item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals(SELECTION_STICK_NAME)) {
            
            Logger.debug("Player is using selection stick");
            event.setCancelled(true); // Prevent normal stick behavior
            
            // Get the clicked block
            if (event.getClickedBlock() == null) {
                Logger.debug("No block was clicked");
                return;
            }
            
            Location clickedLoc = event.getClickedBlock().getLocation();
            ClaimCommand claimCommand = getClaimCommand();
            
            if (claimCommand == null) {
                Logger.debug("Failed to get ClaimCommand instance");
                return;
            }
            
            // Left click for pos1, right click for pos2
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                claimCommand.setFirstPoint(player.getUniqueId(), clickedLoc);
                String message = plugin.getConfigManager().getMessagesConfig().getString(
                    "claim.first-point-set", "&aFirst point set at &f%x%&a, &f%y%&a, &f%z%&a.");
                message = ChatColor.translateAlternateColorCodes('&', message)
                    .replace("%x%", String.valueOf(clickedLoc.getBlockX()))
                    .replace("%y%", String.valueOf(clickedLoc.getBlockY()))
                    .replace("%z%", String.valueOf(clickedLoc.getBlockZ()));
                player.sendMessage(message);
                
                // Show preview if both points are set
                if (claimCommand.getSecondPoint(player.getUniqueId()) != null) {
                    claimCommand.showPreview(player);
                }
                return;
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                claimCommand.setSecondPoint(player.getUniqueId(), clickedLoc);
                String message = plugin.getConfigManager().getMessagesConfig().getString(
                    "claim.second-point-set", "&aSecond point set at &f%x%&a, &f%y%&a, &f%z%&a.");
                message = ChatColor.translateAlternateColorCodes('&', message)
                    .replace("%x%", String.valueOf(clickedLoc.getBlockX()))
                    .replace("%y%", String.valueOf(clickedLoc.getBlockY()))
                    .replace("%z%", String.valueOf(clickedLoc.getBlockZ()));
                player.sendMessage(message);
                
                // Show preview if both points are set
                if (claimCommand.getFirstPoint(player.getUniqueId()) != null) {
                    claimCommand.showPreview(player);
                    claimCommand.displayClaimInfo(player);
                }
                return;
            }
            return; // Add this to ensure we exit the method if using a selection stick
        }
        
        // Handle normal stick right-click for region visualization
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && 
                event.getItem() != null && 
                event.getItem().getType() == Material.STICK &&
                !event.getItem().hasItemMeta() &&
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
    
    private ClaimCommand getClaimCommand() {
        try {
            return ((CommandHandler) plugin.getCommand("fg").getExecutor()).getClaimCommand();
        } catch (Exception e) {
            Logger.debug("Failed to get ClaimCommand executor");
            return null;
        }
    }
    
    public static ItemStack createSelectionStick() {
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(SELECTION_STICK_NAME);
            
            // Add instructions as lore
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.GRAY + "Left-click: Set first position");
            lore.add(ChatColor.GRAY + "Right-click: Set second position");
            meta.setLore(lore);
            
            stick.setItemMeta(meta);
        }
        return stick;
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