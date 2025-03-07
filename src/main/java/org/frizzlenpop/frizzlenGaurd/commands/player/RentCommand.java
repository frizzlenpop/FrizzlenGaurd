package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.managers.RentManager;
import org.frizzlenpop.frizzlenGaurd.models.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RentCommand extends AbstractCommand {
    
    public RentCommand(FrizzlenGaurd plugin) {
        super(plugin, "rent", "Manage region rentals", 
              "/fg rent <set|cancel|list|info> [region] [price] [duration]", "frizzlengaurd.rent");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        if (!plugin.isVaultEnabled()) {
            sender.sendMessage(ChatColor.RED + "Rental features are disabled (Vault not found).");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }
        
        Player player = getPlayer(sender);
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "set":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /fg rent set <region> <price> <duration>");
                    sender.sendMessage(ChatColor.RED + "Duration format: <number>[s|m|h|d] (e.g., 7d for 7 days)");
                    return true;
                }
                handleSetRent(player, args[1], args[2], args[3]);
                break;
                
            case "cancel":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /fg rent cancel <region>");
                    return true;
                }
                handleCancelRent(player, args[1]);
                break;
                
            case "list":
                handleListRentals(player);
                break;
                
            case "info":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /fg rent info <region>");
                    return true;
                }
                handleRentInfo(player, args[1]);
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown action. Use: set, cancel, list, info");
                break;
        }
        
        return true;
    }
    
    private void handleSetRent(Player player, String regionName, String priceStr, String durationStr) {
        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            player.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found.");
            return;
        }
        
        if (!region.isOwner(player) && !player.hasPermission("frizzlengaurd.admin")) {
            player.sendMessage(ChatColor.RED + "You must own this region to set it for rent.");
            return;
        }
        
        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Price must be a positive number.");
            return;
        }
        
        long duration = parseDuration(durationStr);
        if (duration <= 0) {
            player.sendMessage(ChatColor.RED + "Invalid duration format. Use: <number>[s|m|h|d]");
            return;
        }
        
        if (plugin.getRentManager().setForRent(region, player, price, duration)) {
            player.sendMessage(ChatColor.GREEN + "Region '" + regionName + "' is now available for rent.");
            player.sendMessage(ChatColor.GREEN + "Price: " + price + " " + 
                    plugin.getEconomy().currencyNamePlural());
            player.sendMessage(ChatColor.GREEN + "Duration: " + formatDuration(duration));
        } else {
            player.sendMessage(ChatColor.RED + "Failed to set region for rent.");
        }
    }
    
    private void handleCancelRent(Player player, String regionName) {
        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            player.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found.");
            return;
        }
        
        if (!region.isOwner(player) && !player.hasPermission("frizzlengaurd.admin")) {
            player.sendMessage(ChatColor.RED + "You must own this region to cancel its rental.");
            return;
        }
        
        if (plugin.getRentManager().cancelRent(region, player)) {
            player.sendMessage(ChatColor.GREEN + "Rental cancelled for region '" + regionName + "'.");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to cancel rental. Region might not be for rent.");
        }
    }
    
    private void handleListRentals(Player player) {
        RentManager rentManager = plugin.getRentManager();
        List<Region> availableRentals = plugin.getRegionManager().getAllRegions().stream()
                .filter(rentManager::isForRent)
                .collect(Collectors.toList());
        
        if (availableRentals.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No regions are currently available for rent.");
            return;
        }
        
        player.sendMessage(ChatColor.GREEN + "Available rentals:");
        for (Region region : availableRentals) {
            RentManager.RentInfo info = rentManager.getRentInfo(region);
            if (info != null && info.getCurrentTenant() == null) {
                player.sendMessage(ChatColor.YELLOW + "- " + region.getName() + 
                        ChatColor.GREEN + " (" + info.getPrice() + " " + 
                        plugin.getEconomy().currencyNamePlural() + " for " + 
                        formatDuration(info.getDuration()) + ")");
            }
        }
    }
    
    private void handleRentInfo(Player player, String regionName) {
        Region region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            player.sendMessage(ChatColor.RED + "Region '" + regionName + "' not found.");
            return;
        }
        
        RentManager.RentInfo info = plugin.getRentManager().getRentInfo(region);
        if (info == null) {
            player.sendMessage(ChatColor.RED + "Region '" + regionName + "' is not for rent.");
            return;
        }
        
        player.sendMessage(ChatColor.GREEN + "Rental information for '" + regionName + "':");
        player.sendMessage(ChatColor.YELLOW + "Price: " + ChatColor.GREEN + 
                info.getPrice() + " " + plugin.getEconomy().currencyNamePlural());
        player.sendMessage(ChatColor.YELLOW + "Duration: " + ChatColor.GREEN + 
                formatDuration(info.getDuration()));
        
        if (info.getCurrentTenant() != null) {
            player.sendMessage(ChatColor.YELLOW + "Status: " + ChatColor.RED + "Rented");
            player.sendMessage(ChatColor.YELLOW + "Time remaining: " + ChatColor.GREEN + 
                    formatDuration(info.getTimeRemaining()));
        } else {
            player.sendMessage(ChatColor.YELLOW + "Status: " + ChatColor.GREEN + "Available");
        }
    }
    
    private long parseDuration(String duration) {
        try {
            String number = duration.substring(0, duration.length() - 1);
            char unit = duration.charAt(duration.length() - 1);
            long value = Long.parseLong(number);
            
            switch (unit) {
                case 's':
                    return TimeUnit.SECONDS.toMillis(value);
                case 'm':
                    return TimeUnit.MINUTES.toMillis(value);
                case 'h':
                    return TimeUnit.HOURS.toMillis(value);
                case 'd':
                    return TimeUnit.DAYS.toMillis(value);
                default:
                    return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }
    
    private String formatDuration(long duration) {
        long days = TimeUnit.MILLISECONDS.toDays(duration);
        duration -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        duration -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m");
        }
        
        return sb.toString().trim();
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> actions = Arrays.asList("set", "cancel", "list", "info");
            
            return actions.stream()
                    .filter(action -> action.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("list")) {
            String partial = args[1].toLowerCase();
            
            if (args[0].equalsIgnoreCase("set")) {
                // Show only regions the player owns
                return plugin.getRegionManager().getAllRegions().stream()
                        .filter(region -> region.isOwner(player))
                        .map(Region::getName)
                        .filter(name -> name.toLowerCase().startsWith(partial))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("cancel")) {
                // Show only regions the player owns that are for rent
                return plugin.getRegionManager().getAllRegions().stream()
                        .filter(region -> region.isOwner(player) && 
                                        plugin.getRentManager().isForRent(region))
                        .map(Region::getName)
                        .filter(name -> name.toLowerCase().startsWith(partial))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("info")) {
                // Show all regions that are for rent
                return plugin.getRegionManager().getAllRegions().stream()
                        .filter(region -> plugin.getRentManager().isForRent(region))
                        .map(Region::getName)
                        .filter(name -> name.toLowerCase().startsWith(partial))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            // Suggest some common prices
            String partial = args[2];
            List<String> prices = Arrays.asList("100", "500", "1000", "5000", "10000");
            
            return prices.stream()
                    .filter(price -> price.startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            // Suggest some common durations
            String partial = args[3];
            List<String> durations = Arrays.asList("1h", "12h", "1d", "7d", "30d");
            
            return durations.stream()
                    .filter(duration -> duration.startsWith(partial))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
} 