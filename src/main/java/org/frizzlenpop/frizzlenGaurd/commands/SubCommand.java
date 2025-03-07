package org.frizzlenpop.frizzlenGaurd.commands;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public interface SubCommand {
    /**
     * Get the name of the command
     *
     * @return Command name
     */
    String getName();
    
    /**
     * Get description of the command
     *
     * @return Command description
     */
    String getDescription();
    
    /**
     * Get the usage of the command
     *
     * @return Command usage
     */
    String getUsage();
    
    /**
     * Get the permission required to use this command
     *
     * @return Permission node
     */
    String getPermission();
    
    /**
     * Get the aliases of the command
     *
     * @return Command aliases
     */
    default List<String> getAliases() {
        return new ArrayList<>();
    }
    
    /**
     * Execute the command
     *
     * @param sender Command sender
     * @param args   Command arguments
     * @return true if command executed successfully, false otherwise
     */
    boolean execute(CommandSender sender, String[] args);
    
    /**
     * Provide tab completion for the command
     *
     * @param sender Command sender
     * @param args   Command arguments
     * @return List of tab completion options
     */
    default List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
} 