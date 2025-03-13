package org.frizzlenpop.frizzlenGaurd.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.Region;

public class InteractionListeners implements Listener {
    private final FrizzlenGaurd plugin;
    
    public InteractionListeners(FrizzlenGaurd plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Skip if admin bypass is enabled
        if (player.hasPermission("frizzlengaurd.admin.bypass")) {
            return;
        }
        
        // Skip if no block is involved
        if (event.getClickedBlock() == null) {
            return;
        }
        
        Block block = event.getClickedBlock();
        Region region = plugin.getRegionManager().getRegionAt(block.getLocation());
        
        // Not in a protected region
        if (region == null) {
            return;
        }
        
        // Check if player can interact based on the block type
        Material blockType = block.getType();
        String denyMessage = ChatColor.RED + "You don't have permission to use that in this region.";
        
        // Door-related blocks
        if (isWoodDoor(blockType) || blockType == Material.IRON_DOOR) {
            if (!region.canInteract(player) && !region.getFlag("door-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        // Trapdoor-related blocks
        else if (isTrapdoor(blockType)) {
            if (!region.canInteract(player) && !region.getFlag("trapdoor-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        // Fence gate-related blocks
        else if (isFenceGate(blockType)) {
            if (!region.canInteract(player) && !region.getFlag("fence-gate-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        // Button-related blocks
        else if (isButton(blockType)) {
            if (!region.canInteract(player) && !region.getFlag("button-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        // Lever
        else if (blockType == Material.LEVER) {
            if (!region.canInteract(player) && !region.getFlag("lever-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        // Pressure plates
        else if (isPressurePlate(blockType)) {
            if (!region.canInteract(player) && !region.getFlag("pressure-plate")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        // Redstone components
        else if (blockType == Material.REPEATER) {
            if (!region.canInteract(player) && !region.getFlag("repeater-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        else if (blockType == Material.COMPARATOR) {
            if (!region.canInteract(player) && !region.getFlag("comparator-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        // Note blocks
        else if (blockType == Material.NOTE_BLOCK) {
            if (!region.canInteract(player) && !region.getFlag("noteblock-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        // Jukebox
        else if (blockType == Material.JUKEBOX) {
            if (!region.canInteract(player) && !region.getFlag("jukebox-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        // Crafting stations
        else if (blockType == Material.CRAFTING_TABLE) {
            if (!region.canInteract(player) && !region.getFlag("crafting-table-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        else if (blockType == Material.ENCHANTING_TABLE) {
            if (!region.canInteract(player) && !region.getFlag("enchanting-table-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        else if (blockType == Material.ANVIL || blockType == Material.CHIPPED_ANVIL || blockType == Material.DAMAGED_ANVIL) {
            if (!region.canInteract(player) && !region.getFlag("anvil-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        else if (blockType == Material.GRINDSTONE) {
            if (!region.canInteract(player) && !region.getFlag("grindstone-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        else if (blockType == Material.SMITHING_TABLE) {
            if (!region.canInteract(player) && !region.getFlag("smithing-table-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        else if (blockType == Material.LOOM) {
            if (!region.canInteract(player) && !region.getFlag("loom-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        // Cooking stations
        else if (blockType == Material.FURNACE || blockType == Material.BLAST_FURNACE || blockType == Material.SMOKER) {
            if (!region.canInteract(player) && !region.getFlag("furnace-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        else if (blockType == Material.CAMPFIRE || blockType == Material.SOUL_CAMPFIRE) {
            if (!region.canInteract(player) && !region.getFlag("campfire-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        else if (blockType == Material.CAULDRON || blockType == Material.WATER_CAULDRON || blockType == Material.LAVA_CAULDRON || 
                blockType == Material.POWDER_SNOW_CAULDRON) {
            if (!region.canInteract(player) && !region.getFlag("cauldron-use")) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
                return;
            }
        }
        
        // Check for containers
        if (block.getState() instanceof InventoryHolder) {
            String flagName = "chest-access";
            
            // Special case for hoppers, droppers, and dispensers
            if (blockType == Material.HOPPER) {
                flagName = "hopper-use";
            } else if (blockType == Material.DISPENSER) {
                flagName = "dispenser-use";
            } else if (blockType == Material.DROPPER) {
                flagName = "dropper-use";
            }
            
            if (!region.canAccessContainers(player) && !region.getFlag(flagName)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to access containers in this region.");
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        // Skip if admin bypass is enabled
        if (player.hasPermission("frizzlengaurd.admin.bypass")) {
            return;
        }
        
        Entity entity = event.getRightClicked();
        Region region = plugin.getRegionManager().getRegionAt(entity.getLocation());
        
        // Not in a protected region
        if (region == null) {
            return;
        }
        
        // Check interaction based on entity type
        EntityType entityType = entity.getType();
        
        // Armor stands
        if (entityType == EntityType.ARMOR_STAND) {
            if (!region.canInteract(player) && !region.getFlag("armor-stands")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to use armor stands in this region.");
                return;
            }
        }
        
        // Item frames
        else if (entityType == EntityType.ITEM_FRAME || entityType == EntityType.GLOW_ITEM_FRAME) {
            if (!region.canInteract(player) && !region.getFlag("item-frames")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to use item frames in this region.");
                return;
            }
        }
        
        // Villagers - trade
        else if (entityType == EntityType.VILLAGER) {
            if (!region.canInteract(player) && !region.getFlag("villager-trade")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to trade with villagers in this region.");
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getBreeder();
        
        // Skip if admin bypass is enabled
        if (player.hasPermission("frizzlengaurd.admin.bypass")) {
            return;
        }
        
        Region region = plugin.getRegionManager().getRegionAt(event.getEntity().getLocation());
        
        // Not in a protected region
        if (region == null) {
            return;
        }
        
        // Check if animal breeding is allowed
        if (!region.canInteract(player) && !region.getFlag("animal-breeding")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to breed animals in this region.");
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Only check damage to animals/mobs by players
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getDamager();
        
        // Skip if admin bypass is enabled
        if (player.hasPermission("frizzlengaurd.admin.bypass")) {
            return;
        }
        
        Entity victim = event.getEntity();
        Region region = plugin.getRegionManager().getRegionAt(victim.getLocation());
        
        // Not in a protected region
        if (region == null) {
            return;
        }
        
        EntityType entityType = victim.getType();
        
        // Check if it's a paintint or item frame
        if (entityType == EntityType.PAINTING) {
            if (!region.canInteract(player) && !region.getFlag("paintings")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to damage paintings in this region.");
                return;
            }
        } 
        else if (entityType == EntityType.ITEM_FRAME || entityType == EntityType.GLOW_ITEM_FRAME) {
            if (!region.canInteract(player) && !region.getFlag("item-frames")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to damage item frames in this region.");
                return;
            }
        }
        // Check if it's an animal
        else if (isAnimal(entityType) && !region.canInteract(player) && !region.getFlag("animal-damage")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You don't have permission to damage animals in this region.");
            return;
        }
    }
    
    private boolean isAnimal(EntityType type) {
        return type == EntityType.CHICKEN || type == EntityType.COW || type == EntityType.SHEEP || 
               type == EntityType.PIG || type == EntityType.HORSE || type == EntityType.DONKEY || 
               type == EntityType.MULE || type == EntityType.RABBIT || type == EntityType.LLAMA || 
               type == EntityType.PARROT || type == EntityType.TURTLE || type == EntityType.PANDA || 
               type == EntityType.FOX || type == EntityType.BEE || type == EntityType.STRIDER ||
               type == EntityType.GOAT || type == EntityType.AXOLOTL;
    }
    
    private boolean isWoodDoor(Material material) {
        return material == Material.OAK_DOOR || material == Material.SPRUCE_DOOR || 
               material == Material.BIRCH_DOOR || material == Material.JUNGLE_DOOR || 
               material == Material.ACACIA_DOOR || material == Material.DARK_OAK_DOOR ||
               material == Material.CRIMSON_DOOR || material == Material.WARPED_DOOR;
    }
    
    private boolean isTrapdoor(Material material) {
        return material == Material.OAK_TRAPDOOR || material == Material.SPRUCE_TRAPDOOR || 
               material == Material.BIRCH_TRAPDOOR || material == Material.JUNGLE_TRAPDOOR || 
               material == Material.ACACIA_TRAPDOOR || material == Material.DARK_OAK_TRAPDOOR ||
               material == Material.CRIMSON_TRAPDOOR || material == Material.WARPED_TRAPDOOR || 
               material == Material.IRON_TRAPDOOR;
    }
    
    private boolean isFenceGate(Material material) {
        return material == Material.OAK_FENCE_GATE || material == Material.SPRUCE_FENCE_GATE || 
               material == Material.BIRCH_FENCE_GATE || material == Material.JUNGLE_FENCE_GATE || 
               material == Material.ACACIA_FENCE_GATE || material == Material.DARK_OAK_FENCE_GATE ||
               material == Material.CRIMSON_FENCE_GATE || material == Material.WARPED_FENCE_GATE;
    }
    
    private boolean isButton(Material material) {
        return material == Material.OAK_BUTTON || material == Material.SPRUCE_BUTTON || 
               material == Material.BIRCH_BUTTON || material == Material.JUNGLE_BUTTON || 
               material == Material.ACACIA_BUTTON || material == Material.DARK_OAK_BUTTON ||
               material == Material.CRIMSON_BUTTON || material == Material.WARPED_BUTTON || 
               material == Material.STONE_BUTTON || material == Material.POLISHED_BLACKSTONE_BUTTON;
    }
    
    private boolean isPressurePlate(Material material) {
        return material == Material.OAK_PRESSURE_PLATE || material == Material.SPRUCE_PRESSURE_PLATE || 
               material == Material.BIRCH_PRESSURE_PLATE || material == Material.JUNGLE_PRESSURE_PLATE || 
               material == Material.ACACIA_PRESSURE_PLATE || material == Material.DARK_OAK_PRESSURE_PLATE ||
               material == Material.CRIMSON_PRESSURE_PLATE || material == Material.WARPED_PRESSURE_PLATE || 
               material == Material.STONE_PRESSURE_PLATE || material == Material.POLISHED_BLACKSTONE_PRESSURE_PLATE ||
               material == Material.LIGHT_WEIGHTED_PRESSURE_PLATE || material == Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
    }
} 