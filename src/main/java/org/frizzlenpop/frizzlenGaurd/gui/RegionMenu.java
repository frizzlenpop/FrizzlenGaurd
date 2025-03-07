package org.frizzlenpop.frizzlenGaurd.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RegionMenu extends Menu {
    private final Region region;
    
    public RegionMenu(FrizzlenGaurd plugin, Player player, Region region) {
        super(plugin, player, ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getMessagesConfig().getString("gui.region-title", "&8Region: %name%")
                        .replace("%name%", region.getName())),
                plugin.getConfigManager().getMainConfig().getInt("gui.region-menu-rows", 5));
        
        this.region = region;
        setupItems();
    }
    
    @Override
    protected void setupItems() {
        UUID playerId = player.getUniqueId();
        boolean isOwner = region.getOwner().equals(playerId);
        boolean isAdmin = player.hasPermission("frizzlengaurd.admin.*");
        Region.Role playerRole = region.getMemberRole(playerId);
        
        // Region Info
        OfflinePlayer owner = Bukkit.getOfflinePlayer(region.getOwner());
        String ownerName = owner.getName() != null ? owner.getName() : "Unknown";
        
        List<String> infoLore = new ArrayList<>();
        infoLore.add("&7Owner: &f" + ownerName);
        infoLore.add("&7World: &f" + region.getWorldName());
        infoLore.add("&7Area: &f" + 
                ((region.getMaxX() - region.getMinX() + 1) * (region.getMaxZ() - region.getMinZ() + 1)) + 
                " blocks");
        infoLore.add("&7Volume: &f" + region.getVolume() + " blocks");
        infoLore.add("&7Members: &f" + region.getMembers().size());
        infoLore.add("&7Your Role: &e" + playerRole.name());
        
        if (region.isSubregion()) {
            infoLore.add("&7Parent: &f" + region.getParent().getName());
        }
        
        setItem(4, createItem(Material.BOOK, "&e&l" + region.getName(), infoLore.toArray(new String[0])), 
                e -> {
                    e.setCancelled(true);
                });
        
        // Manage Members Button
        if (isOwner || isAdmin || playerRole == Region.Role.OWNER) {
            setItem(20, createItem(Material.PLAYER_HEAD, "&e&lManage Members", 
                    "&7Click to add or remove members",
                    "&7from this region."), 
                    e -> {
                        e.setCancelled(true);
                        player.closeInventory();
                        player.sendMessage(ChatColor.YELLOW + "Coming soon: Member management GUI");
                    });
        } else {
            setItem(20, createItem(Material.PLAYER_HEAD, "&7&lMembers", 
                    "&7You don't have permission to",
                    "&7manage members in this region."), 
                    e -> {
                        e.setCancelled(true);
                    });
        }
        
        // Manage Flags Button
        if (isOwner || isAdmin || playerRole.canModifyFlags()) {
            setItem(22, createItem(Material.REDSTONE_TORCH, "&e&lManage Flags", 
                    "&7Click to change flags like PvP,",
                    "&7mob spawning, and more."), 
                    e -> {
                        e.setCancelled(true);
                        player.closeInventory();
                        player.sendMessage(ChatColor.YELLOW + "Coming soon: Flag management GUI");
                    });
        } else {
            setItem(22, createItem(Material.TORCH, "&7&lFlags", 
                    "&7You don't have permission to",
                    "&7change flags in this region."), 
                    e -> {
                        e.setCancelled(true);
                    });
        }
        
        // Show Boundaries Button
        setItem(24, createItem(Material.GLOWSTONE_DUST, "&e&lShow Boundaries", 
                "&7Click to visualize the boundaries",
                "&7of this region."), 
                e -> {
                    e.setCancelled(true);
                    player.closeInventory();
                    plugin.getVisualsManager().showRegionBoundaries(player, region);
                    player.sendMessage(ChatColor.GREEN + "Showing boundaries for region '" + region.getName() + "'.");
                });
        
        // View Logs Button
        setItem(30, createItem(Material.PAPER, "&e&lView Logs", 
                "&7Click to view the history of",
                "&7actions in this region."), 
                e -> {
                    e.setCancelled(true);
                    player.closeInventory();
                    player.performCommand("fg logs " + region.getName());
                });
        
        // Teleport Button (if player has permission)
        if (playerRole == Region.Role.OWNER || playerRole == Region.Role.MEMBER || isAdmin) {
            setItem(32, createItem(Material.ENDER_PEARL, "&e&lTeleport", 
                    "&7Click to teleport to this region."), 
                    e -> {
                        e.setCancelled(true);
                        player.closeInventory();
                        player.sendMessage(ChatColor.YELLOW + "Coming soon: Teleport feature");
                    });
        }
        
        // Delete Region Button (owner/admin only)
        if (isOwner || isAdmin) {
            setItem(40, createItem(Material.TNT, "&c&lDelete Region", 
                    "&7Click to permanently delete this region.",
                    "&4&lWARNING: &cThis cannot be undone!"), 
                    e -> {
                        e.setCancelled(true);
                        player.closeInventory();
                        player.sendMessage(ChatColor.YELLOW + "Coming soon: Region deletion feature");
                    });
        }
        
        // Back Button
        setItem(36, createItem(Material.ARROW, "&e&lBack", 
                "&7Click to go back to the main menu."), 
                e -> {
                    e.setCancelled(true);
                    player.closeInventory();
                    new MainMenu(plugin, player).open();
                });
        
        // Close Button
        setItem(44, createItem(Material.BARRIER, "&c&lClose", 
                "&7Click to close this menu."), 
                e -> {
                    e.setCancelled(true);
                    player.closeInventory();
                });
        
        // Fill empty slots
        fillEmptySlots(Material.BLACK_STAINED_GLASS_PANE);
    }
} 