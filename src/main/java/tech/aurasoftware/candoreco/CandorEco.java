package tech.aurasoftware.candoreco;

import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import tech.aurasoftware.candoreco.commands.CommandRegistry;
import tech.aurasoftware.candoreco.commands.impl.EconomyCommands;
import tech.aurasoftware.candoreco.configuration.Configuration;
import tech.aurasoftware.candoreco.configuration.DatabaseConfig;
import tech.aurasoftware.candoreco.configuration.EconomyConfig;
import tech.aurasoftware.candoreco.configuration.serialization.Serialization;
import tech.aurasoftware.candoreco.database.DatabaseManager;
import tech.aurasoftware.candoreco.economy.AccountManager;
import tech.aurasoftware.candoreco.economy.CandorEconomy;
import tech.aurasoftware.candoreco.file.YamlFile;
import tech.aurasoftware.candoreco.listeners.PlayerListener;
import tech.aurasoftware.candoreco.managers.PluginManager;

public class CandorEco extends JavaPlugin {
    
    private CommandRegistry commandRegistry;
    private DatabaseManager databaseManager;
    private AccountManager accountManager;
    private CandorEconomy economy;
    private boolean vaultEnabled = false;
    
    @Override
    public void onEnable() {
        
        // Load configuration system
        loadConfiguration();
        
        // Initialize command registry
        commandRegistry = new CommandRegistry(this);
        
        // Initialize database
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize().thenAccept(success -> {
            if (success) {
                getLogger().info("Database initialization completed successfully");
                
                // Initialize account manager
                accountManager = new AccountManager(this, databaseManager);
                
                // Setup Vault integration if available (on main thread)
                getServer().getScheduler().runTask(this, this::setupVault);
                
                // Register listeners with proper accountManager (on main thread)
                getServer().getScheduler().runTask(this, () -> {
                    getServer().getPluginManager().registerEvents(new PlayerListener(this, accountManager), this);
                });
                
            } else {
                getLogger().severe("Database initialization failed! Plugin functionality may be limited.");
            }
        }).exceptionally(throwable -> {
            getLogger().log(Level.SEVERE, "Database initialization error", throwable);
            return null;
        });
        
        // Initialize managers
        PluginManager.getInstance().initialize();
        
        // Register commands
        commandRegistry.registerCommands(new EconomyCommands(this));
        
        getLogger().info("CandorEco has been enabled!");
    }
    
    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().info("Vault not found, economy provider not registered");
            return;
        }
        
        if (accountManager == null) {
            getLogger().warning("AccountManager not initialized, cannot register Vault economy");
            return;
        }
        
        try {
            economy = new CandorEconomy(this, accountManager);
            getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.Highest);
            vaultEnabled = true;
            getLogger().info("Vault economy provider registered successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register Vault economy provider", e);
        }
    }

    @Override
    public void onDisable() {
        // Save all accounts before shutdown
        if (accountManager != null) {
            accountManager.saveAllAccounts().join(); // Wait for completion
            getLogger().info("All economy data saved successfully");
        }
        
        // Unregister Vault economy provider
        if (vaultEnabled && economy != null) {
            getServer().getServicesManager().unregister(Economy.class, economy);
            getLogger().info("Vault economy provider unregistered");
        }
        
        // Close database connection
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        
        getLogger().info("CandorEco has been disabled!");
    }
    
    /**
     * Get the command registry instance
     * @return the CommandRegistry
     */
    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }
    
    /**
     * Get the database manager instance
     * @return the DatabaseManager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    /**
     * Get the account manager instance
     * @return the AccountManager
     */
    public AccountManager getAccountManager() {
        return accountManager;
    }
    
    /**
     * Get the economy instance
     * @return the CandorEconomy instance
     */
    public CandorEconomy getEconomy() {
        return economy;
    }
    
    /**
     * Check if Vault integration is enabled
     * @return true if Vault is enabled and integrated
     */
    public boolean isVaultEnabled() {
        return vaultEnabled;
    }
    
    /**
     * Load configuration from file
     */
    private void loadConfiguration() {
        try {
            // Register serializable classes
            Serialization.register(DatabaseConfig.class);
            
            // Create config file
            YamlFile configFile = new YamlFile("config.yml", getDataFolder().getAbsolutePath(), null, this);
        
            // Load configuration values
            Configuration.loadConfig(configFile, EconomyConfig.values());
            
            getLogger().info("Configuration loaded successfully!");
            
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to load configuration!", e);
        }
    }
    
}