package org.frizzlenpop.frizzlenGaurd.utils;

import org.bukkit.Location;
import org.frizzlenpop.frizzlenGaurd.models.Region;

public class RegionUtils {
    
    /**
     * Check if two regions are adjacent (sharing at least one face)
     * 
     * @param region1 First region
     * @param region2 Second region
     * @return true if regions are adjacent
     */
    public static boolean areRegionsAdjacent(Region region1, Region region2) {
        // Check if regions are in the same world
        if (!region1.getWorld().equals(region2.getWorld())) {
            return false;
        }
        
        // Get region bounds
        Location min1 = region1.getMinPoint();
        Location max1 = region1.getMaxPoint();
        Location min2 = region2.getMinPoint();
        Location max2 = region2.getMaxPoint();
        
        // Check if regions share a face on any axis
        return (min1.getBlockX() == max2.getBlockX() + 1 || max1.getBlockX() == min2.getBlockX() - 1) &&
               overlapsOnAxis(min1.getBlockY(), max1.getBlockY(), min2.getBlockY(), max2.getBlockY()) &&
               overlapsOnAxis(min1.getBlockZ(), max1.getBlockZ(), min2.getBlockZ(), max2.getBlockZ()) ||
               
               (min1.getBlockY() == max2.getBlockY() + 1 || max1.getBlockY() == min2.getBlockY() - 1) &&
               overlapsOnAxis(min1.getBlockX(), max1.getBlockX(), min2.getBlockX(), max2.getBlockX()) &&
               overlapsOnAxis(min1.getBlockZ(), max1.getBlockZ(), min2.getBlockZ(), max2.getBlockZ()) ||
               
               (min1.getBlockZ() == max2.getBlockZ() + 1 || max1.getBlockZ() == min2.getBlockZ() - 1) &&
               overlapsOnAxis(min1.getBlockX(), max1.getBlockX(), min2.getBlockX(), max2.getBlockX()) &&
               overlapsOnAxis(min1.getBlockY(), max1.getBlockY(), min2.getBlockY(), max2.getBlockY());
    }
    
    /**
     * Check if two regions overlap
     * 
     * @param region1 First region
     * @param region2 Second region
     * @return true if regions overlap
     */
    public static boolean doRegionsOverlap(Region region1, Region region2) {
        // Check if regions are in the same world
        if (!region1.getWorld().equals(region2.getWorld())) {
            return false;
        }
        
        // Get region bounds
        Location min1 = region1.getMinPoint();
        Location max1 = region1.getMaxPoint();
        Location min2 = region2.getMinPoint();
        Location max2 = region2.getMaxPoint();
        
        // Check if regions overlap on all axes
        return overlapsOnAxis(min1.getBlockX(), max1.getBlockX(), min2.getBlockX(), max2.getBlockX()) &&
               overlapsOnAxis(min1.getBlockY(), max1.getBlockY(), min2.getBlockY(), max2.getBlockY()) &&
               overlapsOnAxis(min1.getBlockZ(), max1.getBlockZ(), min2.getBlockZ(), max2.getBlockZ());
    }
    
    /**
     * Merge two regions into one
     * 
     * @param region1 First region
     * @param region2 Second region
     * @param newName Name for the merged region
     * @return Merged region
     * @throws IllegalArgumentException if regions cannot be merged
     */
    public static Region mergeRegions(Region region1, Region region2, String newName) {
        // Check if regions are in the same world
        if (!region1.getWorld().equals(region2.getWorld())) {
            throw new IllegalArgumentException("Regions must be in the same world to merge.");
        }
        
        // Get region bounds
        Location min1 = region1.getMinPoint();
        Location max1 = region1.getMaxPoint();
        Location min2 = region2.getMinPoint();
        Location max2 = region2.getMaxPoint();
        
        // Calculate bounds of merged region
        Location mergedMin = new Location(region1.getWorld(),
                Math.min(min1.getBlockX(), min2.getBlockX()),
                Math.min(min1.getBlockY(), min2.getBlockY()),
                Math.min(min1.getBlockZ(), min2.getBlockZ()));
        
        Location mergedMax = new Location(region1.getWorld(),
                Math.max(max1.getBlockX(), max2.getBlockX()),
                Math.max(max1.getBlockY(), max2.getBlockY()),
                Math.max(max1.getBlockZ(), max2.getBlockZ()));
        
        // Create new region
        return new Region(newName, mergedMin, mergedMax);
    }
    
    /**
     * Check if two ranges overlap on a single axis
     * 
     * @param min1 First range minimum
     * @param max1 First range maximum
     * @param min2 Second range minimum
     * @param max2 Second range maximum
     * @return true if ranges overlap
     */
    private static boolean overlapsOnAxis(int min1, int max1, int min2, int max2) {
        return min1 <= max2 && max1 >= min2;
    }
} 