package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.commands.SubCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HelpCommand extends AbstractCommand {
    private final Map<String, SubCommand> commands;
    
    public HelpCommand(FrizzlenGaurd plugin, Map<String, SubCommand> commands) {
        super(plugin, "help", "Displays a list of available commands", "/fg help [page|command]", "frizzlengaurd.help");
        this.commands = commands;
    }
    
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("?");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (args.length == 0) {
            // Show command list (page 1)
            showCommandList(sender, 1);
            return true;
        }
        
        // Check if the argument is a page number
        try {
            int page = Integer.parseInt(args[0]);
            showCommandList(sender, page);
            return true;
        } catch (NumberFormatException e) {
            // Not a number, check if it's a command name
            String commandName = args[0].toLowerCase();
            
            if (commands.containsKey(commandName)) {
                SubCommand command = commands.get(commandName);
                
                // Check if the sender has permission for this command
                if (!sender.hasPermission(command.getPermission()) && 
                        !sender.hasPermission("frizzlengaurd.*") &&
                        !(sender.hasPermission("frizzlengaurd.admin.*") && 
                          command.getPermission().startsWith("frizzlengaurd.admin."))) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to view this command.");
                    return true;
                }
                
                // Show command help
                sender.sendMessage(ChatColor.GOLD + "----- " + ChatColor.WHITE + 
                        "FrizzlenGaurd: " + command.getName() + ChatColor.GOLD + " -----");
                sender.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + command.getDescription());
                sender.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + command.getUsage());
                sender.sendMessage(ChatColor.YELLOW + "Permission: " + ChatColor.WHITE + command.getPermission());
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Unknown command: " + commandName);
                return true;
            }
        }
    }
    
    private void showCommandList(CommandSender sender, int page) {
        // Get commands the sender can use
        List<SubCommand> availableCommands = commands.values().stream()
                .distinct() // Remove duplicates (from aliases)
                .filter(cmd -> sender.hasPermission(cmd.getPermission()) || 
                               sender.hasPermission("frizzlengaurd.*") ||
                               (sender.hasPermission("frizzlengaurd.admin.*") && 
                                cmd.getPermission().startsWith("frizzlengaurd.admin.")))
                .sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
                .collect(Collectors.toList());
        
        // Calculate total pages
        int itemsPerPage = 8;
        int totalPages = (int) Math.ceil((double) availableCommands.size() / itemsPerPage);
        
        // Validate page number
        if (page < 1) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }
        
        // Calculate page range
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, availableCommands.size());
        
        // Display header
        sender.sendMessage(ChatColor.GOLD + "----- " + ChatColor.WHITE + 
                "FrizzlenGaurd Help (" + page + "/" + totalPages + ")" + ChatColor.GOLD + " -----");
        
        // Display command list
        if (availableCommands.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No commands available.");
        } else {
            for (int i = startIndex; i < endIndex; i++) {
                SubCommand cmd = availableCommands.get(i);
                sender.sendMessage(ChatColor.YELLOW + cmd.getUsage() + ChatColor.GRAY + " - " + 
                        ChatColor.WHITE + cmd.getDescription());
            }
        }
        
        // Display navigation
        sender.sendMessage(ChatColor.GOLD + "----- " + ChatColor.WHITE + 
                "Type /fg help [command] for more info" + ChatColor.GOLD + " -----");
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            
            // Add page numbers
            for (int i = 1; i <= Math.ceil((double) commands.size() / 8); i++) {
                completions.add(String.valueOf(i));
            }
            
            // Add command names the sender has permission for
            commands.values().stream()
                    .distinct() // Remove duplicates (from aliases)
                    .filter(cmd -> sender.hasPermission(cmd.getPermission()) || 
                                   sender.hasPermission("frizzlengaurd.*") ||
                                   (sender.hasPermission("frizzlengaurd.admin.*") && 
                                    cmd.getPermission().startsWith("frizzlengaurd.admin.")))
                    .map(SubCommand::getName)
                    .forEach(completions::add);
            
            // Return completions that match the current input
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 