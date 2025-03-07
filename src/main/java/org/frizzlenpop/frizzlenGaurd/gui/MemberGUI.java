package org.frizzlenpop.frizzlenGaurd.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.Region;
import org.frizzlenpop.frizzlenGaurd.models.Region.Role;

import java.util.*;
import java.util.stream.Collectors;

public class MemberGUI extends AbstractGUI {
    private final Region region;
    private final Map<UUID, Role> members;
    private final Map<UUID, Role> owners;
    private int currentPage;
    
    private static final int ROWS = 6;
    private static final int PAGE_SIZE = (ROWS - 1) * 9;
    
    public MemberGUI(FrizzlenGaurd plugin, Player player, Region region) {
        super(plugin, player, "&8Region Members: &6" + region.getName(), ROWS * 9);
        this.region = region;
        this.members = region.getMembers();
        this.owners = region.getOwners();
        this.currentPage = 0;
    }
    
    @Override
    protected void initialize() {
        updatePage();
    }
    
    private void updatePage() {
        inventory.clear();
        clickHandlers.clear();
        
        List<UUID> allMembers = new ArrayList<>();
        allMembers.addAll(owners.keySet());
        allMembers.addAll(members.keySet());
        
        int totalPages = (allMembers.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, allMembers.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            UUID memberId = allMembers.get(i);
            Role role = owners.containsKey(memberId) ? Role.OWNER : Role.MEMBER;
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Role: " + (role == Role.OWNER ? "&6Owner" : "&aMember"));
            lore.add("");
            lore.add("&eLeft-click to change role");
            lore.add("&cRight-click to remove");
            
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(member);
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
                        "&6" + (member.getName() != null ? member.getName() : memberId.toString())));
                
                List<String> coloredLore = lore.stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());
                meta.setLore(coloredLore);
                
                skull.setItemMeta(meta);
            }
            
            setItem(i - startIndex, skull, e -> {
                if (!region.isOwner(player)) {
                    player.sendMessage(ChatColor.RED + "You must be an owner to modify members.");
                    return;
                }
                
                if (e.isRightClick()) {
                    // Remove member
                    if (role == Role.OWNER) {
                        owners.remove(memberId);
                    } else {
                        members.remove(memberId);
                    }
                } else {
                    // Toggle role
                    if (role == Role.OWNER) {
                        owners.remove(memberId);
                        members.put(memberId, Role.MEMBER);
                    } else {
                        members.remove(memberId);
                        owners.put(memberId, Role.OWNER);
                    }
                }
                
                plugin.getDataManager().saveData();
                updatePage();
            });
        }
        
        // Add member button
        setItem(inventory.getSize() - 5, 
                createItem(Material.EMERALD, "&aAdd Member", Arrays.asList(
                        "",
                        "&7Click to add a new member",
                        "&7to this region"
                )), 
                e -> {
                    if (!region.isOwner(player)) {
                        player.sendMessage(ChatColor.RED + "You must be an owner to add members.");
                        return;
                    }
                    
                    player.closeInventory();
                    player.sendMessage(ChatColor.GREEN + "Type the name of the player you want to add.");
                    // The actual adding of the player would be handled by a chat event listener
                });
        
        fillEmptySlots();
        addNavigationButtons(currentPage > 0 ? currentPage - 1 : -1, currentPage, totalPages);
    }
    
    @Override
    protected void onPageChange(int newPage) {
        this.currentPage = newPage;
        updatePage();
    }
} 