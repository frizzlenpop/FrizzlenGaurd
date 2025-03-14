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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Menu {
    protected final FrizzlenGaurd plugin;
    protected final Player player;
    protected Inventory inventory;
    protected final Map<Integer, Consumer<InventoryClickEvent>> clickHandlers = new HashMap<>();
    
    /**
     * Create a new menu
     * 
     * @param plugin The plugin instance
     * @param player The player viewing the menu
     * @param title The title of the menu
     * @param rows The number of rows in the menu (1-6)
     */
    public Menu(FrizzlenGaurd plugin, Player player, String title, int rows) {
        this.plugin = plugin;
        this.player = player;
        
        // Validate rows
        if (rows < 1) rows = 1;
        if (rows > 6) rows = 6;
        
        // Create the inventory
        this.inventory = Bukkit.createInventory(null, rows * 9, ChatColor.translateAlternateColorCodes('&', title));
    }
    
    /**
     * Set an item in the menu
     * 
     * @param slot The slot to set the item in
     * @param item The item to set
     * @param clickHandler The handler to call when the item is clicked, or null for no action
     */
    protected void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> clickHandler) {
        inventory.setItem(slot, item);
        
        if (clickHandler != null) {
            clickHandlers.put(slot, clickHandler);
        } else {
            clickHandlers.remove(slot);
        }
    }
    
    /**
     * Create an item with a custom name and lore
     * 
     * @param material The material of the item
     * @param name The name of the item
     * @param lore The lore of the item
     * @return The created item
     */
    protected ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            
            if (lore.length > 0) {
                List<String> coloredLore = Arrays.asList(lore);
                coloredLore.replaceAll(line -> ChatColor.translateAlternateColorCodes('&', line));
                meta.setLore(coloredLore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Fill empty slots with a filler item
     * 
     * @param material The material to use for the filler
     */
    protected void fillEmptySlots(Material material) {
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
    
    /**
     * Open the inventory for the player
     */
    public void open() {
        // Register this menu with the menu listener
        plugin.getMenuListener().registerMenu(player, this);
        
        // Then open the inventory
        player.openInventory(inventory);
    }
    
    /**
     * Handle a click in this menu
     * 
     * @param event The click event
     * @return true if the event was handled
     */
    public boolean handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        
        if (slot >= 0 && slot < inventory.getSize() && clickHandlers.containsKey(slot)) {
            clickHandlers.get(slot).accept(event);
            return true;
        }
        
        return false;
    }
    
    /**
     * Get the inventory for this menu
     * 
     * @return The inventory
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Set up the items in the menu
     */
    protected abstract void setupItems();
} 