package org.frizzlenpop.frizzlenGaurd.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.gui.AbstractGUI;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class GUIListener implements Listener {
    private final FrizzlenGaurd plugin;
    private final Map<UUID, AbstractGUI> openGUIs;
    private final Map<UUID, AddMemberCallback> pendingAddMembers;
    private static final long CALLBACK_TIMEOUT = 30000; // 30 seconds
    
    public GUIListener(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        this.openGUIs = new HashMap<>();
        this.pendingAddMembers = new HashMap<>();
        
        // Start cleanup task
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredCallbacks();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }
    
    public void registerGUI(Player player, AbstractGUI gui) {
        openGUIs.put(player.getUniqueId(), gui);
    }
    
    public void unregisterGUI(Player player) {
        openGUIs.remove(player.getUniqueId());
        // Also remove any pending callbacks when GUI is closed
        pendingAddMembers.remove(player.getUniqueId());
    }
    
    public void registerPendingAddMember(Player player, Region region) {
        pendingAddMembers.put(player.getUniqueId(), new AddMemberCallback(region));
    }
    
    private void cleanupExpiredCallbacks() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, AddMemberCallback>> it = pendingAddMembers.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry<UUID, AddMemberCallback> entry = it.next();
            if (now - entry.getValue().timestamp > CALLBACK_TIMEOUT) {
                Player player = plugin.getServer().getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    player.sendMessage("§cAdd member request has expired.");
                }
                it.remove();
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        AbstractGUI gui = openGUIs.get(player.getUniqueId());
        
        if (gui != null && event.getClickedInventory() != null) {
            Inventory clickedInventory = event.getClickedInventory();
            if (clickedInventory.equals(gui.getInventory())) {
                gui.handleClick(event);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        unregisterGUI(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        unregisterGUI(player);
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        AddMemberCallback callback = pendingAddMembers.remove(player.getUniqueId());
        
        if (callback != null) {
            event.setCancelled(true);
            String targetName = event.getMessage();
            
            // Process on the main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                callback.process(player, targetName);
            });
        }
    }
    
    private static class AddMemberCallback {
        private final Region region;
        private final long timestamp;
        
        public AddMemberCallback(Region region) {
            this.region = region;
            this.timestamp = System.currentTimeMillis();
        }
        
        public void process(Player player, String targetName) {
            // Check if the callback has expired (after 30 seconds)
            if (System.currentTimeMillis() - timestamp > CALLBACK_TIMEOUT) {
                player.sendMessage("§cAdd member request has expired. Please try again.");
                return;
            }
            
            // Find the target player
            Player target = player.getServer().getPlayer(targetName);
            if (target == null) {
                player.sendMessage("§cPlayer '" + targetName + "' not found or is not online.");
                return;
            }
            
            // Add the player as a member
            region.addMember(target.getUniqueId());
            player.sendMessage("§aAdded " + target.getName() + " as a member to region '" + 
                    region.getName() + "'.");
            target.sendMessage("§aYou have been added as a member to region '" + 
                    region.getName() + "'.");
        }
    }
} 