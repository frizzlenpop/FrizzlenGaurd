package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.utils.PermissionsManager;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Command to check claim block status
 */
public class BlocksCommand extends AbstractCommand {
    
    public BlocksCommand(FrizzlenGaurd plugin) {
        super(plugin, "blocks", "Check your available claim blocks", 
              "/fg blocks", "frizzlengaurd.blocks");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("b", "claimblocks");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        Player player = getPlayer(sender);
        UUID playerId = player.getUniqueId();
        
        PermissionsManager permissionsManager = plugin.getPermissionsManager();
        
        // Get claim blocks data
        int maxClaimBlocks = permissionsManager.getClaimBlocks(player);
        int usedClaimBlocks = permissionsManager.getUsedClaimBlocks(playerId);
        int remainingClaimBlocks = permissionsManager.getRemainingClaimBlocks(player);
        
        // Get claims count data
        int currentClaims = plugin.getRegionManager().getRegionsByOwner(playerId).size();
        int maxClaims = permissionsManager.getMaxClaims(player);
        
        // Show header
        sender.sendMessage(ChatColor.GOLD + "=== Claim Blocks Information ===");
        
        // Show claim blocks info
        sender.sendMessage(ChatColor.YELLOW + "Total Claim Blocks: " + ChatColor.WHITE + maxClaimBlocks);
        sender.sendMessage(ChatColor.YELLOW + "Used Claim Blocks: " + ChatColor.WHITE + usedClaimBlocks);
        sender.sendMessage(ChatColor.YELLOW + "Remaining Claim Blocks: " + ChatColor.WHITE + remainingClaimBlocks);
        
        // Show claims count info
        sender.sendMessage(ChatColor.YELLOW + "Your Claims: " + ChatColor.WHITE + currentClaims + 
                ChatColor.GRAY + "/" + ChatColor.WHITE + maxClaims);
        
        // Show permission info
        if (player.hasPermission("frizzlengaurd.admin.bypass")) {
            sender.sendMessage(ChatColor.GREEN + "You have unlimited claim blocks and claims (Admin)");
        } else {
            // Find and display the exact permission that grants the claim blocks
            for (int i = 10000; i >= 100; i -= 50) {
                if (player.hasPermission("frizzlengaurd.blocks." + i) && i == maxClaimBlocks) {
                    sender.sendMessage(ChatColor.GRAY + "Permission: frizzlengaurd.blocks." + i);
                    break;
                }
            }
            
            // Find and display the exact permission that grants max claims
            for (int i = 50; i >= 1; i--) {
                if (player.hasPermission("frizzlengaurd.claims." + i) && i == maxClaims) {
                    sender.sendMessage(ChatColor.GRAY + "Permission: frizzlengaurd.claims." + i);
                    break;
                }
            }
        }
        
        return true;
    }
} 