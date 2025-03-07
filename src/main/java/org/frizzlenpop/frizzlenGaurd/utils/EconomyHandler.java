package org.frizzlenpop.frizzlenGaurd.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.UUID;

public class EconomyHandler {
    private static Economy economy = null;
    private static FrizzlenGaurd plugin = null;
    
    /**
     * Set up the economy integration
     * 
     * @param plugin The plugin instance
     * @return true if economy was successfully set up
     */
    public static boolean setupEconomy(FrizzlenGaurd plugin) {
        EconomyHandler.plugin = plugin;
        
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = 
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        
        if (rsp == null) {
            return false;
        }
        
        economy = rsp.getProvider();
        return economy != null;
    }
    
    /**
     * Check if a player can afford a claim
     * 
     * @param playerId The player's UUID
     * @param area The area of the claim in blocks
     * @return true if they can afford it
     */
    public static boolean canAffordClaim(UUID playerId, int area) {
        if (!isEconomyEnabled() || !isClaimCostEnabled()) {
            return true;
        }
        
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        double cost = calculateClaimCost(area);
        
        return economy.has(player, cost);
    }
    
    /**
     * Charge a player for a claim
     * 
     * @param playerId The player's UUID
     * @param area The area of the claim in blocks
     * @return true if the transaction was successful
     */
    public static boolean chargeClaim(UUID playerId, int area) {
        if (!isEconomyEnabled() || !isClaimCostEnabled()) {
            return true;
        }
        
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        double cost = calculateClaimCost(area);
        
        if (!economy.has(player, cost)) {
            return false;
        }
        
        economy.withdrawPlayer(player, cost);
        return true;
    }
    
    /**
     * Calculate the cost of a claim
     * 
     * @param area The area of the claim in blocks
     * @return The cost
     */
    public static double calculateClaimCost(int area) {
        if (!isEconomyEnabled() || !isClaimCostEnabled()) {
            return 0.0;
        }
        
        double costPerBlock = plugin.getConfigManager().getMainConfig()
                .getDouble("economy.claim-cost-per-block", 0.5);
        
        return area * costPerBlock;
    }
    
    /**
     * Get a formatted string of the cost
     * 
     * @param cost The cost to format
     * @return The formatted cost
     */
    public static String formatCost(double cost) {
        if (!isEconomyEnabled()) {
            return String.format("%.2f", cost);
        }
        
        return economy.format(cost);
    }
    
    /**
     * Check if a player can afford a claim tax payment
     * 
     * @param playerId The player's UUID
     * @param region The region to tax
     * @return true if they can afford it
     */
    public static boolean canAffordTax(UUID playerId, Region region) {
        if (!isEconomyEnabled() || !isTaxEnabled()) {
            return true;
        }
        
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        double tax = calculateTax(region);
        
        return economy.has(player, tax);
    }
    
    /**
     * Charge a player tax for a claim
     * 
     * @param playerId The player's UUID
     * @param region The region to tax
     * @return true if the transaction was successful
     */
    public static boolean chargeTax(UUID playerId, Region region) {
        if (!isEconomyEnabled() || !isTaxEnabled()) {
            return true;
        }
        
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        double tax = calculateTax(region);
        
        if (!economy.has(player, tax)) {
            return false;
        }
        
        economy.withdrawPlayer(player, tax);
        return true;
    }
    
    /**
     * Calculate the tax for a region
     * 
     * @param region The region to calculate tax for
     * @return The tax amount
     */
    public static double calculateTax(Region region) {
        if (!isEconomyEnabled() || !isTaxEnabled()) {
            return 0.0;
        }
        
        int area = (region.getMaxX() - region.getMinX() + 1) * (region.getMaxZ() - region.getMinZ() + 1);
        double taxPerBlock = plugin.getConfigManager().getMainConfig()
                .getDouble("economy.tax-cost-per-block", 0.01);
        
        return area * taxPerBlock;
    }
    
    /**
     * Schedule tax collection for all regions
     */
    public static void scheduleTaxCollection() {
        if (!isEconomyEnabled() || !isTaxEnabled()) {
            return;
        }
        
        int taxIntervalMinutes = plugin.getConfigManager().getMainConfig().getInt("economy.tax-interval", 1440);
        if (taxIntervalMinutes <= 0) {
            return;
        }
        
        long taxIntervalTicks = taxIntervalMinutes * 20 * 60L; // Convert minutes to ticks
        
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            collectTaxes();
        }, taxIntervalTicks, taxIntervalTicks);
        
        Logger.info("Scheduled tax collection every " + taxIntervalMinutes + " minutes.");
    }
    
    /**
     * Collect taxes for all regions
     */
    private static void collectTaxes() {
        if (!isEconomyEnabled() || !isTaxEnabled()) {
            return;
        }
        
        Logger.info("Collecting taxes for all regions...");
        
        int successCount = 0;
        int failCount = 0;
        
        for (Region region : plugin.getRegionManager().getAllRegions()) {
            UUID ownerId = region.getOwner();
            
            // Skip regions in excluded worlds
            if (plugin.getRegionManager().isWorldExcluded(region.getWorldName())) {
                continue;
            }
            
            // Calculate and charge tax
            if (chargeTax(ownerId, region)) {
                successCount++;
            } else {
                failCount++;
                // TODO: Handle tax evasion (e.g., warning, then seizure of property)
                OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerId);
                Logger.warning("Player " + owner.getName() + " could not afford tax for region " + region.getName());
            }
        }
        
        Logger.info("Tax collection complete. Successful: " + successCount + ", Failed: " + failCount);
    }
    
    /**
     * Check if economy is enabled
     * 
     * @return true if economy is enabled
     */
    public static boolean isEconomyEnabled() {
        return economy != null && plugin != null && 
                plugin.isVaultEnabled() && 
                plugin.getConfigManager().getMainConfig().getBoolean("economy.enabled", true);
    }
    
    /**
     * Check if claiming costs money
     * 
     * @return true if claiming costs money
     */
    public static boolean isClaimCostEnabled() {
        return isEconomyEnabled() && 
                plugin.getConfigManager().getMainConfig().getDouble("economy.claim-cost-per-block", 0.5) > 0;
    }
    
    /**
     * Check if tax is enabled
     * 
     * @return true if tax is enabled
     */
    public static boolean isTaxEnabled() {
        return isEconomyEnabled() && 
                plugin.getConfigManager().getMainConfig().getBoolean("economy.tax-enabled", false);
    }
} 