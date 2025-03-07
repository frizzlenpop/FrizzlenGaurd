package org.frizzlenpop.frizzlenGaurd.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuListener implements Listener {
    private final FrizzlenGaurd plugin;
    private final Map<UUID, Menu> openMenus = new ConcurrentHashMap<>();
    
    public MenuListener(FrizzlenGaurd plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Register an open menu
     * 
     * @param player The player viewing the menu
     * @param menu The menu they are viewing
     */
    public void registerMenu(Player player, Menu menu) {
        openMenus.put(player.getUniqueId(), menu);
    }
    
    /**
     * Unregister a player's open menu
     * 
     * @param player The player to unregister
     */
    public void unregisterMenu(Player player) {
        openMenus.remove(player.getUniqueId());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Menu menu = openMenus.get(player.getUniqueId());
        
        if (menu != null && event.getInventory().equals(menu.getInventory())) {
            // Cancel all clicks in the menu
            event.setCancelled(true);
            
            // Let the menu handle the click
            menu.handleClick(event);
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Menu menu = openMenus.get(player.getUniqueId());
        
        if (menu != null && event.getInventory().equals(menu.getInventory())) {
            // Cancel all drags in the menu
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        Menu menu = openMenus.get(player.getUniqueId());
        
        if (menu != null && event.getInventory().equals(menu.getInventory())) {
            // Unregister the menu when it's closed
            openMenus.remove(player.getUniqueId());
        }
    }
} 