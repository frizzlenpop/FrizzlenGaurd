package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.managers.NotificationManager.NotificationSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NotifyCommand extends AbstractCommand {
    
    public NotifyCommand(FrizzlenGaurd plugin) {
        super(plugin, "notify", "Manage region notifications", 
              "/fg notify <toggle|settings> [type] [value]", "frizzlengaurd.notify");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        Player player = getPlayer(sender);
        String action = args[0].toLowerCase();
        NotificationSettings settings = plugin.getNotificationManager().getSettings(player.getUniqueId());
        
        switch (action) {
            case "toggle":
                settings.setEnabled(!settings.isEnabled());
                plugin.getNotificationManager().updateSettings(player.getUniqueId(), settings);
                
                String status = settings.isEnabled() ? "enabled" : "disabled";
                player.sendMessage(ChatColor.GREEN + "Region notifications are now " + status + ".");
                break;
                
            case "settings":
                if (args.length < 2) {
                    showSettings(player, settings);
                    return true;
                }
                
                String type = args[1].toLowerCase();
                boolean value = args.length > 2 ? Boolean.parseBoolean(args[2]) : true;
                
                switch (type) {
                    case "chat":
                        settings.setChatEnabled(value);
                        player.sendMessage(ChatColor.GREEN + "Chat notifications " + 
                                (value ? "enabled" : "disabled") + ".");
                        break;
                        
                    case "title":
                        settings.setTitleEnabled(value);
                        player.sendMessage(ChatColor.GREEN + "Title notifications " + 
                                (value ? "enabled" : "disabled") + ".");
                        break;
                        
                    case "sound":
                        settings.setSoundEnabled(value);
                        player.sendMessage(ChatColor.GREEN + "Sound notifications " + 
                                (value ? "enabled" : "disabled") + ".");
                        break;
                        
                    case "location":
                        settings.setLocationEnabled(value);
                        player.sendMessage(ChatColor.GREEN + "Location display " + 
                                (value ? "enabled" : "disabled") + ".");
                        break;
                        
                    default:
                        player.sendMessage(ChatColor.RED + "Unknown setting type. Use: chat, title, sound, location");
                        return true;
                }
                
                plugin.getNotificationManager().updateSettings(player.getUniqueId(), settings);
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action. Use: toggle, settings");
                break;
        }
        
        return true;
    }
    
    private void showSettings(Player player, NotificationSettings settings) {
        player.sendMessage(ChatColor.GREEN + "Notification Settings:");
        player.sendMessage(ChatColor.YELLOW + "Status: " + 
                formatBoolean(settings.isEnabled()));
        player.sendMessage(ChatColor.YELLOW + "Chat Messages: " + 
                formatBoolean(settings.isChatEnabled()));
        player.sendMessage(ChatColor.YELLOW + "Title Messages: " + 
                formatBoolean(settings.isTitleEnabled()));
        player.sendMessage(ChatColor.YELLOW + "Sound Effects: " + 
                formatBoolean(settings.isSoundEnabled()));
        player.sendMessage(ChatColor.YELLOW + "Location Display: " + 
                formatBoolean(settings.isLocationEnabled()));
        player.sendMessage(ChatColor.GRAY + "Use /fg notify settings <type> [true|false] to change settings");
    }
    
    private String formatBoolean(boolean value) {
        return value ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled";
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> actions = Arrays.asList("toggle", "settings");
            
            return actions.stream()
                    .filter(action -> action.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("settings")) {
            String partial = args[1].toLowerCase();
            List<String> types = Arrays.asList("chat", "title", "sound", "location");
            
            return types.stream()
                    .filter(type -> type.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("settings")) {
            String partial = args[2].toLowerCase();
            List<String> values = Arrays.asList("true", "false");
            
            return values.stream()
                    .filter(value -> value.startsWith(partial))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 