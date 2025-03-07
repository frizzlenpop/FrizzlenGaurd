package org.frizzlenpop.frizzlenGaurd.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.List;
import java.util.UUID;

public class MainMenu extends Menu {
    
    public MainMenu(FrizzlenGaurd plugin, Player player) {
        super(plugin, player, ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getMessagesConfig().getString("gui.main-title", "&8FrizzlenGaurd - Main Menu")),
                plugin.getConfigManager().getMainConfig().getInt("gui.main-menu-rows", 3));
        
        setupItems();
    }
    
    @Override
    protected void setupItems() {
        UUID playerId = player.getUniqueId();
        
        // My Regions Button
        setItem(10, createItem(Material.CHEST, "&e&lMy Regions", 
                "&7Click to view regions you own",
                "&7or have access to."), 
                e -> {
                    e.setCancelled(true);
                    player.closeInventory();
                    player.performCommand("fg listregions");
                });
        
        // Create Region Button
        setItem(12, createItem(Material.GOLDEN_AXE, "&a&lCreate Region", 
                "&7Click to create a new region.",
                "&7You'll need to select two points",
                "&7using the selection tool."), 
                e -> {
                    e.setCancelled(true);
                    player.closeInventory();
                    player.performCommand("fg claim");
                });
        
        // Claims Near Me Button
        setItem(14, createItem(Material.COMPASS, "&b&lClaims Near Me", 
                "&7Click to show claims near your",
                "&7current location."), 
                e -> {
                    e.setCancelled(true);
                    player.closeInventory();
                    // Get current claim
                    Region region = plugin.getRegionManager().getRegionAt(player.getLocation());
                    if (region != null) {
                        player.performCommand("fg regioninfo");
                    } else {
                        player.sendMessage(ChatColor.RED + "There are no claims at your current location.");
                    }
                });
        
        // Admin Menu Button
        if (player.hasPermission("frizzlengaurd.admin.*")) {
            setItem(16, createItem(Material.REDSTONE, "&c&lAdmin Menu", 
                    "&7Click to open the admin menu.",
                    "&7Requires admin permissions."), 
                    e -> {
                        e.setCancelled(true);
                        // Open admin menu (to be implemented)
                        player.closeInventory();
                        player.sendMessage(ChatColor.YELLOW + "Coming soon: Admin Menu");
                    });
        }
        
        // Claim Blocks Info
        int claimBlocks = plugin.getDataManager().getPlayerClaimBlocks(playerId);
        setItem(28, createItem(Material.GRASS_BLOCK, "&e&lClaim Blocks: &f" + claimBlocks, 
                "&7These are used to claim land.",
                "&7You earn more by playing on the server."), 
                e -> {
                    e.setCancelled(true);
                });
        
        // Help & Documentation
        setItem(31, createItem(Material.BOOK, "&e&lHelp & Commands", 
                "&7Click to see a list of commands",
                "&7and how to use them."), 
                e -> {
                    e.setCancelled(true);
                    player.closeInventory();
                    player.performCommand("fg help");
                });
        
        // Close Button
        setItem(34, createItem(Material.BARRIER, "&c&lClose", 
                "&7Click to close this menu."), 
                e -> {
                    e.setCancelled(true);
                    player.closeInventory();
                });
        
        // Fill empty slots
        fillEmptySlots(Material.BLACK_STAINED_GLASS_PANE);
    }
} 