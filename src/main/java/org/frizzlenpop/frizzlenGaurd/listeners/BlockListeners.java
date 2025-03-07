package org.frizzlenpop.frizzlenGaurd.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.LogEntry;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.Iterator;
import java.util.List;

public class BlockListeners implements Listener {
    private final FrizzlenGaurd plugin;
    
    public BlockListeners(FrizzlenGaurd plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Admin bypass permission
        if (player.hasPermission("frizzlengaurd.admin.bypass")) {
            return;
        }
        
        // Check if the block is in a protected region
        Region region = plugin.getRegionManager().getRegionAt(block.getLocation());
        if (region != null && !region.canBuild(player)) {
            // Cancel the block break
            event.setCancelled(true);
            
            // Notify the player
            player.sendMessage(ChatColor.RED + "You don't have permission to break blocks in this region.");
            
            // Log the attempt if logging is enabled
            if (plugin.getConfigManager().getMainConfig().getBoolean("logging.log-block-changes", true)) {
                region.addLogEntry(new LogEntry(player, LogEntry.LogAction.BLOCK_BREAK, 
                        "Attempted to break " + block.getType() + " (cancelled)", null));
            }
            
            return;
        }
        
        // Log successful block breaks if logging is enabled
        if (region != null && plugin.getConfigManager().getMainConfig().getBoolean("logging.log-block-changes", true)) {
            region.addLogEntry(new LogEntry(player, LogEntry.LogAction.BLOCK_BREAK, 
                    "Broke " + block.getType(), null));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Admin bypass permission
        if (player.hasPermission("frizzlengaurd.admin.bypass")) {
            return;
        }
        
        // Check if the block is in a protected region
        Region region = plugin.getRegionManager().getRegionAt(block.getLocation());
        if (region != null && !region.canBuild(player)) {
            // Cancel the block place
            event.setCancelled(true);
            
            // Notify the player
            player.sendMessage(ChatColor.RED + "You don't have permission to place blocks in this region.");
            
            // Log the attempt if logging is enabled
            if (plugin.getConfigManager().getMainConfig().getBoolean("logging.log-block-changes", true)) {
                region.addLogEntry(new LogEntry(player, LogEntry.LogAction.BLOCK_PLACE, 
                        "Attempted to place " + block.getType() + " (cancelled)", null));
            }
            
            return;
        }
        
        // Log successful block placements if logging is enabled
        if (region != null && plugin.getConfigManager().getMainConfig().getBoolean("logging.log-block-changes", true)) {
            region.addLogEntry(new LogEntry(player, LogEntry.LogAction.BLOCK_PLACE, 
                    "Placed " + block.getType(), null));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        
        // Check if block is in a protected region
        Region region = plugin.getRegionManager().getRegionAt(block.getLocation());
        if (region != null) {
            // Check fire-spread flag
            if (!region.getFlag("fire-spread")) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        // Check explosions flag for each block
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block block = it.next();
            Region region = plugin.getRegionManager().getRegionAt(block.getLocation());
            
            if (region != null && !region.getFlag("explosions")) {
                // Remove block from explosion list
                it.remove();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        // Check explosions flag for each block
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block block = it.next();
            Region region = plugin.getRegionManager().getRegionAt(block.getLocation());
            
            if (region != null && !region.getFlag("explosions")) {
                // Remove block from explosion list
                it.remove();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        
        // Check for specific entity types and flags
        switch (event.getEntityType()) {
            case ENDERMAN:
                // Enderman block pickup prevention
                Region region = plugin.getRegionManager().getRegionAt(block.getLocation());
                if (region != null && !region.getFlag("mob-damage")) {
                    event.setCancelled(true);
                }
                break;
                
            case FALLING_BLOCK:
                // Allow falling blocks to simulate physics
                break;
                
            case PLAYER:
                // Player block changes are handled by BlockBreakEvent and BlockPlaceEvent
                break;
                
            default:
                // Handle other entity block changes (like wither destruction, etc.)
                Region r = plugin.getRegionManager().getRegionAt(block.getLocation());
                if (r != null && !r.getFlag("mob-damage")) {
                    event.setCancelled(true);
                }
                break;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!handlePistonEvent(event.getBlock(), event.getBlocks())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!handlePistonEvent(event.getBlock(), event.getBlocks())) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Handle piston events to prevent moving blocks in/out of regions
     * 
     * @param pistonBlock The piston block
     * @param blocks The blocks being moved
     * @return true if the piston should be allowed to move, false otherwise
     */
    private boolean handlePistonEvent(Block pistonBlock, List<Block> blocks) {
        if (blocks.isEmpty()) {
            return true;
        }
        
        Region pistonRegion = plugin.getRegionManager().getRegionAt(pistonBlock.getLocation());
        
        // If piston protection is disabled globally or for this region, allow the piston to move
        if (pistonRegion != null && !pistonRegion.getFlag("piston-protection")) {
            return true;
        }
        
        // Check each block being moved
        for (Block block : blocks) {
            Region blockRegion = plugin.getRegionManager().getRegionAt(block.getLocation());
            
            // If regions don't match or either region has piston protection enabled, cancel the piston
            if ((blockRegion != pistonRegion) || 
                (blockRegion != null && blockRegion.getFlag("piston-protection"))) {
                return false;
            }
        }
        
        return true;
    }
} 