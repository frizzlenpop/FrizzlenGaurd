package org.frizzlenpop.frizzlenGaurd;

import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.frizzlenGaurd.commands.CommandHandler;
import org.frizzlenpop.frizzlenGaurd.config.ConfigManager;
import org.frizzlenpop.frizzlenGaurd.data.DataManager;
import org.frizzlenpop.frizzlenGaurd.data.RegionManager;
import org.frizzlenpop.frizzlenGaurd.gui.MenuListener;
import org.frizzlenpop.frizzlenGaurd.listeners.BlockListeners;
import org.frizzlenpop.frizzlenGaurd.listeners.PlayerListeners;
import org.frizzlenpop.frizzlenGaurd.listeners.InteractionListeners;
import org.frizzlenpop.frizzlenGaurd.utils.EconomyHandler;
import org.frizzlenpop.frizzlenGaurd.utils.Logger;
import org.frizzlenpop.frizzlenGaurd.visuals.VisualsManager;
import org.frizzlenpop.frizzlenGaurd.utils.RollbackManager;
import org.frizzlenpop.frizzlenGaurd.commands.player.MergeCommand;
import org.frizzlenpop.frizzlenGaurd.commands.player.ResizeCommand;
import org.frizzlenpop.frizzlenGaurd.commands.admin.ScanCommand;
import org.frizzlenpop.frizzlenGaurd.commands.player.ManageCommand;
import org.frizzlenpop.frizzlenGaurd.commands.player.TeleportCommand;
import org.frizzlenpop.frizzlenGaurd.commands.player.RentCommand;
import org.frizzlenpop.frizzlenGaurd.commands.player.NotifyCommand;
import org.frizzlenpop.frizzlenGaurd.commands.admin.BackupCommand;
import org.frizzlenpop.frizzlenGaurd.commands.admin.LimitCommand;
import org.frizzlenpop.frizzlenGaurd.listeners.GUIListener;
import org.frizzlenpop.frizzlenGaurd.managers.RentManager;
import org.frizzlenpop.frizzlenGaurd.managers.NotificationManager;
import org.frizzlenpop.frizzlenGaurd.managers.BackupManager;
import org.frizzlenpop.frizzlenGaurd.managers.LimitManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Objects;

public final class FrizzlenGaurd extends JavaPlugin {

    private static FrizzlenGaurd instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private RegionManager regionManager;
    private VisualsManager visualsManager;
    private MenuListener menuListener;
    private boolean vaultEnabled = false;
    private RollbackManager rollbackManager;
    private GUIListener guiListener;
    private RentManager rentManager;
    private Economy economy;
    private NotificationManager notificationManager;
    private BackupManager backupManager;
    private LimitManager limitManager;
    
    @Override
    public void onEnable() {
        // Set instance
        instance = this;
        
        // Initialize logger
        Logger.init(this);
        Logger.info("§aInitializing FrizzlenGaurd v" + getDescription().getVersion());
        
        // Load configurations
        this.configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        // Initialize managers
        this.dataManager = new DataManager(this);
        this.regionManager = new RegionManager(this);
        this.visualsManager = new VisualsManager(this);
        this.menuListener = new MenuListener(this);
        this.rollbackManager = new RollbackManager(this);
        this.guiListener = new GUIListener(this);
        this.notificationManager = new NotificationManager(this);
        this.backupManager = new BackupManager(this);
        this.limitManager = new LimitManager(this);
        
        // Load data
        dataManager.loadData();
        
        // Setup Vault
        if (setupEconomy()) {
            this.rentManager = new RentManager(this);
            vaultEnabled = true;
        } else {
            getLogger().warning("Vault not found! Rental features will be disabled.");
        }
        
        // Register commands
        Objects.requireNonNull(getCommand("fg")).setExecutor(new CommandHandler(this));
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new BlockListeners(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);
        getServer().getPluginManager().registerEvents(new InteractionListeners(this), this);
        getServer().getPluginManager().registerEvents(menuListener, this);
        getServer().getPluginManager().registerEvents(guiListener, this);
        
        // Start scheduled tasks
        startTasks();
        
        // Register new commands
        registerCommands();
        
        Logger.info("§aFrizzlenGaurd has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Save all data
        if (dataManager != null) {
            dataManager.saveData();
        }
        
        // Cancel all tasks
        getServer().getScheduler().cancelTasks(this);
        
        Logger.info("§cFrizzlenGaurd has been disabled!");
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        
        economy = rsp.getProvider();
        return economy != null;
    }
    
    private void startTasks() {
        // Auto-save task
        long saveInterval = configManager.getMainConfig().getLong("auto-save-interval", 5) * 20 * 60; // minutes to ticks
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> dataManager.saveData(), saveInterval, saveInterval);
        
        // Auto-backup task
        long backupInterval = configManager.getMainConfig().getLong("auto-backup-interval", 60) * 20 * 60; // minutes to ticks
        if (backupInterval > 0) {
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> dataManager.createBackup(), backupInterval, backupInterval);
        }
    }
    
    private void registerCommands() {
        new MergeCommand(this);
        new ResizeCommand(this);
        new ScanCommand(this);
        new ManageCommand(this);
        new TeleportCommand(this);
        new RentCommand(this);
        new NotifyCommand(this);
        new BackupCommand(this);
        new LimitCommand(this);
    }
    
    public static FrizzlenGaurd getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public RegionManager getRegionManager() {
        return regionManager;
    }
    
    public VisualsManager getVisualsManager() {
        return visualsManager;
    }
    
    public MenuListener getMenuListener() {
        return menuListener;
    }
    
    public boolean isVaultEnabled() {
        return vaultEnabled;
    }
    
    public RollbackManager getRollbackManager() {
        return rollbackManager;
    }
    
    public GUIListener getGUIListener() {
        return guiListener;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public RentManager getRentManager() {
        return rentManager;
    }
    
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }
    
    public BackupManager getBackupManager() {
        return backupManager;
    }
    
    public LimitManager getLimitManager() {
        return limitManager;
    }
}
