package org.frizzlenpop.frizzlenGaurd.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractGUI {
    protected final FrizzlenGaurd plugin;
    protected final Player player;
    protected final Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers;
    
    public AbstractGUI(FrizzlenGaurd plugin, Player player, String title, int size) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', title));
        this.clickHandlers = new HashMap<>();
        
        initialize();
    }
    
    protected abstract void initialize();
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        Consumer<InventoryClickEvent> handler = clickHandlers.get(event.getSlot());
        if (handler != null) {
            handler.accept(event);
        }
    }
    
    protected void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> handler) {
        inventory.setItem(slot, item);
        if (handler != null) {
            clickHandlers.put(slot, handler);
        }
    }
    
    protected ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            
            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    protected ItemStack createItem(Material material, String name) {
        return createItem(material, name, null);
    }
    
    protected void fillEmptySlots() {
        ItemStack filler = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
    
    protected void addNavigationButtons(int previousPage, int currentPage, int totalPages) {
        if (previousPage >= 0) {
            setItem(inventory.getSize() - 9, 
                    createItem(Material.ARROW, "&aPrevious Page"), 
                    e -> onPageChange(previousPage));
        }
        
        setItem(inventory.getSize() - 5, 
                createItem(Material.PAPER, "&7Page " + (currentPage + 1) + " of " + totalPages), 
                null);
        
        if (currentPage < totalPages - 1) {
            setItem(inventory.getSize() - 1, 
                    createItem(Material.ARROW, "&aNext Page"), 
                    e -> onPageChange(currentPage + 1));
        }
    }
    
    protected void onPageChange(int newPage) {
        // Override in subclasses if needed
    }
} 