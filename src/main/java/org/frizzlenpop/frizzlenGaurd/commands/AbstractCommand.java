package org.frizzlenpop.frizzlenGaurd.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;

public abstract class AbstractCommand implements SubCommand {
    protected final FrizzlenGaurd plugin;
    private final String name;
    private final String description;
    private final String usage;
    private final String permission;
    
    public AbstractCommand(FrizzlenGaurd plugin, String name, String description, String usage, String permission) {
        this.plugin = plugin;
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public String getUsage() {
        return usage;
    }
    
    @Override
    public String getPermission() {
        return permission;
    }
    
    /**
     * Check if the sender has permission to execute this command
     *
     * @param sender The command sender
     * @return true if they have permission, false otherwise
     */
    protected boolean hasPermission(CommandSender sender) {
        if (sender.hasPermission(getPermission()) || sender.hasPermission("frizzlengaurd.*") || 
                (sender.hasPermission("frizzlengaurd.admin.*") && getPermission().startsWith("frizzlengaurd.admin."))) {
            return true;
        }
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigManager().getMessagesConfig().getString("general.no-permission", 
                        "&cYou don't have permission to do that.")));
        return false;
    }
    
    /**
     * Check if the sender is a player
     *
     * @param sender The command sender
     * @return true if they are a player, false otherwise
     */
    protected boolean isPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return true;
        }
        
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfigManager().getMessagesConfig().getString("general.player-only",
                        "&cThis command can only be used by players.")));
        return false;
    }
    
    /**
     * Get a message from the messages.yml configuration
     *
     * @param path The path to the message
     * @param defaultMessage The default message if the path doesn't exist
     * @return The colored message
     */
    protected String getMessage(String path, String defaultMessage) {
        return ChatColor.translateAlternateColorCodes('&',
                plugin.getConfigManager().getMessagesConfig().getString(path, defaultMessage));
    }
    
    /**
     * Get a player from the command sender
     *
     * @param sender The command sender
     * @return The player, or null if the sender is not a player
     */
    protected Player getPlayer(CommandSender sender) {
        return (sender instanceof Player) ? (Player) sender : null;
    }
} 