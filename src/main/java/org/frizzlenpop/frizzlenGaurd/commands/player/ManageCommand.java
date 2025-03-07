package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.gui.FlagGUI;
import org.frizzlenpop.frizzlenGaurd.gui.MemberGUI;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ManageCommand extends AbstractCommand {
    
    public ManageCommand(FrizzlenGaurd plugin) {
        super(plugin, "manage", "Open region management GUI", 
              "/fg manage <region> <flags|members>", "frizzlengaurd.manage");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        Player player = getPlayer(sender);
        String regionName = args[0];
        String type = args[1].toLowerCase();
        
        // Find the region
        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            sender.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found.");
            return true;
        }
        
        // Check if player can manage the region
        if (!region.isOwner(player) && !player.hasPermission("frizzlengaurd.admin")) {
            sender.sendMessage(ChatColor.RED + "You must own this region to manage it.");
            return true;
        }
        
        // Open the appropriate GUI
        switch (type) {
            case "flags":
                if (!region.canModifyFlags(player)) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to modify flags in this region.");
                    return true;
                }
                new FlagGUI(plugin, player, region).open();
                break;
                
            case "members":
                if (!region.isOwner(player)) {
                    sender.sendMessage(ChatColor.RED + "You must be an owner to manage members.");
                    return true;
                }
                new MemberGUI(plugin, player, region).open();
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Invalid management type. Use: flags, members");
                return true;
        }
        
        return true;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            // Tab complete region names that the player owns
            String partial = args[0].toLowerCase();
            
            return plugin.getRegionManager().getAllRegions().stream()
                    .filter(region -> region.isOwner(player) || 
                                    player.hasPermission("frizzlengaurd.admin"))
                    .map(Region::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Tab complete management types
            String partial = args[1].toLowerCase();
            List<String> types = Arrays.asList("flags", "members");
            
            return types.stream()
                    .filter(type -> type.startsWith(partial))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 