package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.models.LogEntry;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SetFlagCommand extends AbstractCommand {
    private static final List<String> VALID_FLAGS = Arrays.asList(
            "pvp", "mob-spawning", "mob-damage", "explosions", "fire-spread", 
            "leaf-decay", "crop-trample", "piston-protection", "redstone",
            "animal-breeding", "animal-damage", "villager-trade", "item-pickup", 
            "item-drop", "exp-pickup", "armor-stands", "item-frames", "paintings",
            "chest-access", "door-use", "button-use", "lever-use", "pressure-plate",
            "trapdoor-use", "fence-gate-use", "hopper-use", "dispenser-use", "dropper-use",
            "repeater-use", "comparator-use", "noteblock-use", "jukebox-use", 
            "furnace-use", "crafting-table-use", "enchanting-table-use", "anvil-use",
            "grindstone-use", "smithing-table-use", "loom-use", "campfire-use", "cauldron-use"
    );
    
    private static final List<String> VALID_VALUES = Arrays.asList("true", "false", "on", "off", "allow", "deny", "yes", "no");
    
    public SetFlagCommand(FrizzlenGaurd plugin) {
        super(plugin, "setflag", "Sets a flag for a region", 
              "/fg setflag <regionName> <flag> <value>", "frizzlengaurd.setflag");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("flag", "f");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        Player player = getPlayer(sender);
        UUID playerId = player.getUniqueId();
        
        String regionName = args[0];
        String flagName = args[1].toLowerCase();
        String flagValue = args[2].toLowerCase();
        
        // Validate flag name
        if (!VALID_FLAGS.contains(flagName)) {
            String message = getMessage("flag.flag-not-found", "&cFlag &f%flag% &cdoes not exist.");
            message = message.replace("%flag%", flagName);
            sender.sendMessage(message);
            return true;
        }
        
        // Validate flag value
        if (!VALID_VALUES.contains(flagValue)) {
            String message = getMessage("flag.invalid-value", "&cInvalid value for flag &f%flag%&c.");
            message = message.replace("%flag%", flagName);
            sender.sendMessage(message);
            return true;
        }
        
        // Convert string value to boolean
        boolean boolValue = flagValue.equals("true") || flagValue.equals("on") || 
                            flagValue.equals("allow") || flagValue.equals("yes");
        
        // Get the region
        List<Region> playerRegions = plugin.getRegionManager().getPlayerRegions(playerId);
        Region targetRegion = null;
        
        // First try exact match
        for (Region region : playerRegions) {
            if (region.getName().equalsIgnoreCase(regionName)) {
                targetRegion = region;
                break;
            }
        }
        
        // If not found and player is admin, search all regions
        if (targetRegion == null && player.hasPermission("frizzlengaurd.admin.*")) {
            for (Region region : plugin.getRegionManager().getAllRegions()) {
                if (region.getName().equalsIgnoreCase(regionName)) {
                    targetRegion = region;
                    break;
                }
            }
        }
        
        if (targetRegion == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found or you don't have access to it.");
            return true;
        }
        
        // Check if player has permission to modify flags in this region
        if (!player.hasPermission("frizzlengaurd.admin.*")) {
            Region.Role playerRole = targetRegion.getMemberRole(playerId);
            if (!playerRole.canModifyFlags()) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to change flags in this region.");
                return true;
            }
        }
        
        // Set the flag
        targetRegion.setFlag(flagName, boolValue);
        
        // Log the action
        targetRegion.addLogEntry(new LogEntry(player, LogEntry.LogAction.FLAG_CHANGE, 
                "Changed flag " + flagName + " to " + boolValue, null));
        
        // Send success message
        String message = getMessage("flag.flag-set", "&aFlag &f%flag% &aset to &f%value%&a.");
        message = message.replace("%flag%", flagName)
                       .replace("%value%", String.valueOf(boolValue));
        sender.sendMessage(message);
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            // Tab complete the region name
            List<String> regions = new ArrayList<>();
            String partialName = args[0].toLowerCase();
            UUID playerId = player.getUniqueId();
            boolean isAdmin = player.hasPermission("frizzlengaurd.admin.*");
            
            if (isAdmin) {
                // Admins can see all regions
                regions = plugin.getRegionManager().getAllRegions().stream()
                        .map(Region::getName)
                        .filter(name -> name.toLowerCase().startsWith(partialName))
                        .collect(Collectors.toList());
            } else {
                // Regular players can only see their own regions
                regions = plugin.getRegionManager().getPlayerRegions(playerId).stream()
                        .map(Region::getName)
                        .filter(name -> name.toLowerCase().startsWith(partialName))
                        .collect(Collectors.toList());
            }
            
            return regions;
        } else if (args.length == 2) {
            // Tab complete flags
            String partialFlag = args[1].toLowerCase();
            return VALID_FLAGS.stream()
                    .filter(flag -> flag.startsWith(partialFlag))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            // Tab complete flag values
            String partialValue = args[2].toLowerCase();
            return VALID_VALUES.stream()
                    .filter(value -> value.startsWith(partialValue))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 