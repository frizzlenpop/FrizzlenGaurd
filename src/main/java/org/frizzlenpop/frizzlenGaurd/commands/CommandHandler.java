package org.frizzlenpop.frizzlenGaurd.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.admin.*;
import org.frizzlenpop.frizzlenGaurd.commands.player.*;
import org.frizzlenpop.frizzlenGaurd.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final FrizzlenGaurd plugin;
    private final Map<String, SubCommand> commands;
    private ClaimCommand claimCommand;
    
    public CommandHandler(FrizzlenGaurd plugin) {
        this.plugin = plugin;
        this.commands = new HashMap<>();
        
        // Register player commands
        this.claimCommand = new ClaimCommand(plugin);
        registerCommand(claimCommand);
        registerCommand(new SubclaimCommand(plugin));
        registerCommand(new AddFriendCommand(plugin));
        registerCommand(new RemoveFriendCommand(plugin));
        registerCommand(new SetRoleCommand(plugin));
        registerCommand(new RegionInfoCommand(plugin));
        registerCommand(new SetFlagCommand(plugin));
        registerCommand(new ListRegionsCommand(plugin));
        registerCommand(new GuiCommand(plugin));
        registerCommand(new LogsCommand(plugin));
        registerCommand(new HelpCommand(plugin, commands));
        
        // Register admin commands
        registerCommand(new DeleteRegionCommand(plugin));
        registerCommand(new BackupCommand(plugin));
        registerCommand(new ReloadCommand(plugin));
        registerCommand(new ScanCommand(plugin));
        registerCommand(new MergeCommand(plugin));
        registerCommand(new ResizeCommand(plugin));
        registerCommand(new SetExcludeCommand(plugin));
        registerCommand(new RemoveExcludeCommand(plugin));
        registerCommand(new ListExcludeCommand(plugin));
    }
    
    private void registerCommand(SubCommand command) {
        commands.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            commands.put(alias.toLowerCase(), command);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Default to help command if no arguments
            return commands.get("help").execute(sender, args);
        }
        
        String subCommand = args[0].toLowerCase();
        if (!commands.containsKey(subCommand)) {
            sender.sendMessage(ChatColor.RED + "Unknown command. Type /fg help for a list of commands.");
            return true;
        }
        
        try {
            // Remove the first argument (the subcommand name) and pass the rest to the handler
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, args.length - 1);
            
            return commands.get(subCommand).execute(sender, subArgs);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "An error occurred while executing the command.");
            Logger.error("Error executing command '" + subCommand + "': " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Complete subcommand names
            return commands.keySet().stream()
                    .distinct() // Remove duplicates from aliases
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .filter(cmd -> {
                        SubCommand subCmd = commands.get(cmd);
                        return sender.hasPermission(subCmd.getPermission());
                    })
                    .collect(Collectors.toList());
        } else if (args.length > 1) {
            // Pass to subcommand for further completion
            String subCommand = args[0].toLowerCase();
            if (commands.containsKey(subCommand)) {
                // Remove the first argument and pass the rest
                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subArgs, 0, args.length - 1);
                
                SubCommand cmd = commands.get(subCommand);
                return cmd.tabComplete(sender, subArgs);
            }
        }
        
        return new ArrayList<>();
    }
    
    public Map<String, SubCommand> getCommands() {
        return commands;
    }
    
    public ClaimCommand getClaimCommand() {
        return claimCommand;
    }
} 