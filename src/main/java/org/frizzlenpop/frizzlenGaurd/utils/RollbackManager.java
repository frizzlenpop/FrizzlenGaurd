package org.frizzlenpop.frizzlenGaurd.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.LogEntry;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages rollback operations for regions
 */
public class RollbackManager {
    private final FrizzlenGaurd plugin;
    private final Map<UUID, RollbackSession> activeSessions;
    private static final int BATCH_SIZE = 50;
    private static final long BATCH_DELAY_TICKS = 1L;
    
    /**
     * Constructor
     * 
     * @param plugin Plugin instance
     */
    public RollbackManager(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        this.activeSessions = new ConcurrentHashMap<>();
    }
    
    /**
     * Start a rollback operation for a region
     * 
     * @param player Player initiating the rollback
     * @param region Region to rollback
     * @param timestamp Timestamp to rollback to (logs after this time will be rolled back)
     * @param playerFilter Optional player filter (only rollback changes by this player)
     * @param actionFilter Optional action filter (only rollback specific actions)
     * @return true if rollback was started successfully
     */
    public boolean startRollback(Player player, Region region, long timestamp, 
                                UUID playerFilter, List<LogEntry.LogAction> actionFilter) {
        UUID playerId = player.getUniqueId();
        if (activeSessions.containsKey(playerId)) {
            return false;
        }

        List<LogEntry> logs = getLogsForRollback(region, timestamp, playerFilter, actionFilter);
        if (logs.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No changes found to rollback.");
            return false;
        }

        RollbackSession session = new RollbackSession(player, region, logs);
        activeSessions.put(playerId, session);
        processRollback(session);
        return true;
    }
    
    /**
     * Cancel an active rollback session
     * 
     * @param playerId Player ID
     * @return true if a session was cancelled
     */
    public boolean cancelRollback(UUID playerId) {
        RollbackSession session = activeSessions.remove(playerId);
        if (session != null && session.getTaskId() != -1) {
            Bukkit.getScheduler().cancelTask(session.getTaskId());
            return true;
        }
        return false;
    }
    
    /**
     * Get logs for rollback based on filtering criteria
     * 
     * @param region Region to get logs for
     * @param timestamp Timestamp to get logs after
     * @param playerFilter Optional player filter
     * @param actionFilter Optional action filter
     * @return Filtered logs
     */
    private List<LogEntry> getLogsForRollback(Region region, long timestamp, 
                                           UUID playerFilter, List<LogEntry.LogAction> actionFilter) {
        List<LogEntry> logs = new ArrayList<>();
        // Get logs from the database or storage system
        // Filter based on region, timestamp, player, and action type
        // Sort logs in reverse chronological order
        // This is a placeholder - actual implementation would depend on how logs are stored
        return logs;
    }
    
    /**
     * Process a rollback session
     * 
     * @param session Rollback session
     */
    private void processRollback(RollbackSession session) {
        List<LogEntry> logs = session.getLogs();
        Player player = session.getPlayer();
        int totalLogs = logs.size();
        int[] processedCount = {0};

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            int start = processedCount[0];
            int end = Math.min(start + BATCH_SIZE, totalLogs);
            
            if (start >= totalLogs) {
                // Rollback complete
                player.sendMessage(ChatColor.GREEN + "Rollback complete! Processed " + totalLogs + " changes.");
                activeSessions.remove(player.getUniqueId());
                Bukkit.getScheduler().cancelTask(session.getTaskId());
                return;
            }

            // Process this batch
            processBatch(logs.subList(start, end));
            processedCount[0] = end;

            // Update progress
            int progress = (int) ((end / (double) totalLogs) * 100);
            player.sendMessage(ChatColor.YELLOW + "Rollback progress: " + progress + "% (" + end + "/" + totalLogs + ")");
        }, 0L, BATCH_DELAY_TICKS);

        session.setTaskId(taskId);
    }
    
    /**
     * Process a batch of logs for rollback
     * 
     * @param batch Batch of logs to process
     */
    private void processBatch(List<LogEntry> batch) {
        for (LogEntry log : batch) {
            processLogEntry(log);
        }
    }
    
    /**
     * Process a single log entry for rollback
     * 
     * @param log Log entry to process
     */
    private void processLogEntry(LogEntry log) {
        Location location = log.getLocation();
        if (location == null || location.getWorld() == null) {
            return;
        }

        Block block = location.getBlock();
        switch (log.getAction()) {
            case BLOCK_BREAK:
                // Restore the broken block
                BlockState previousState = log.getPreviousState();
                if (previousState != null) {
                    previousState.update(true, false);
                }
                break;
            case BLOCK_PLACE:
                // Remove the placed block (restore to air)
                block.setType(log.getPreviousState().getType(), false);
                break;
            // Add other action types as needed
        }
    }
    
    /**
     * Represents an active rollback session
     */
    public static class RollbackSession {
        private final Player player;
        private final Region region;
        private final List<LogEntry> logs;
        private int taskId = -1;
        
        /**
         * Constructor
         * 
         * @param player Player initiating the rollback
         * @param region Region being rolled back
         * @param logs Logs to process
         */
        public RollbackSession(Player player, Region region, List<LogEntry> logs) {
            this.player = player;
            this.region = region;
            this.logs = logs;
        }
        
        /**
         * Get player
         * 
         * @return Player
         */
        public Player getPlayer() {
            return player;
        }
        
        /**
         * Get region
         * 
         * @return Region
         */
        public Region getRegion() {
            return region;
        }
        
        /**
         * Get logs
         * 
         * @return Logs
         */
        public List<LogEntry> getLogs() {
            return logs;
        }
        
        /**
         * Get task ID
         * 
         * @return Task ID
         */
        public int getTaskId() {
            return taskId;
        }
        
        /**
         * Set task ID
         * 
         * @param taskId New task ID
         */
        public void setTaskId(int taskId) {
            this.taskId = taskId;
        }
    }
} 