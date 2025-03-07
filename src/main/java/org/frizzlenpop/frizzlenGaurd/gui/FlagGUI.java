package org.frizzlenpop.frizzlenGaurd.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FlagGUI extends AbstractGUI {
    private final Region region;
    private final Map<String, Boolean> flags;
    private final List<String> availableFlags;
    private int currentPage;
    
    private static final int ROWS = 6;
    private static final int PAGE_SIZE = (ROWS - 1) * 9;
    
    public FlagGUI(FrizzlenGaurd plugin, Player player, Region region) {
        super(plugin, player, "&8Region Flags: &6" + region.getName(), ROWS * 9);
        this.region = region;
        this.flags = region.getFlags();
        this.availableFlags = new ArrayList<>(Arrays.asList(
                "pvp",
                "mob-spawning",
                "mob-damage",
                "creeper-explosion",
                "tnt-explosion",
                "fire-spread",
                "leaf-decay",
                "ice-form",
                "ice-melt",
                "snow-fall",
                "snow-melt",
                "water-flow",
                "lava-flow",
                "use",
                "chest-access",
                "ride",
                "sleep",
                "farm",
                "invincible",
                "entry",
                "greeting",
                "farewell"
        ));
        this.currentPage = 0;
    }
    
    @Override
    protected void initialize() {
        updatePage();
    }
    
    private void updatePage() {
        inventory.clear();
        clickHandlers.clear();
        
        int totalPages = (availableFlags.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        int startIndex = currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, availableFlags.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String flag = availableFlags.get(i);
            boolean value = flags.getOrDefault(flag, false);
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Current value: " + (value ? "&aEnabled" : "&cDisabled"));
            lore.add("");
            lore.add("&eClick to toggle");
            
            Material material = value ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
            String displayName = "&6" + formatFlagName(flag);
            
            setItem(i - startIndex, createItem(material, displayName, lore), e -> {
                if (!region.canModifyFlags(player)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to modify flags in this region.");
                    return;
                }
                
                flags.put(flag, !value);
                plugin.getDataManager().saveData();
                updatePage();
            });
        }
        
        fillEmptySlots();
        addNavigationButtons(currentPage > 0 ? currentPage - 1 : -1, currentPage, totalPages);
    }
    
    @Override
    protected void onPageChange(int newPage) {
        this.currentPage = newPage;
        updatePage();
    }
    
    private String formatFlagName(String flag) {
        String[] words = flag.split("-");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1).toLowerCase());
        }
        
        return formatted.toString();
    }
} 