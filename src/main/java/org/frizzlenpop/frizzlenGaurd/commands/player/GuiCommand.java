package org.frizzlenpop.frizzlenGaurd.commands.player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenGaurd.FrizzlenGaurd;
import org.frizzlenpop.frizzlenGaurd.commands.AbstractCommand;
import org.frizzlenpop.frizzlenGaurd.gui.MainMenu;

import java.util.Arrays;
import java.util.List;

public class GuiCommand extends AbstractCommand {
    
    public GuiCommand(FrizzlenGaurd plugin) {
        super(plugin, "gui", "Opens the FrizzlenGaurd GUI", 
              "/fg gui", "frizzlengaurd.gui");
    }
    
    @Override
    public List<String> getAliases() {
        return Arrays.asList("menu", "g");
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!hasPermission(sender)) {
            return true;
        }
        
        if (!isPlayer(sender)) {
            return true;
        }
        
        // Check if GUI is enabled in config
        if (!plugin.getConfigManager().getMainConfig().getBoolean("gui.enabled", true)) {
            sender.sendMessage(getMessage("gui.disabled", "&cThe GUI is currently disabled."));
            return true;
        }
        
        Player player = getPlayer(sender);
        
        // Open the main GUI
        new MainMenu(plugin, player).open();
        
        return true;
    }
} 