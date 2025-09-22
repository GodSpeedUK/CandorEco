package tech.aurasoftware.candoreco.economy;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import tech.aurasoftware.candoreco.CandorEco;
import tech.aurasoftware.candoreco.database.DatabaseManager;

/**
 * Manages player accounts, caching, and database operations
 */
public class AccountManager {
    
    private final CandorEco plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, PlayerAccount> accountCache = new ConcurrentHashMap<>();
    
    public AccountManager(CandorEco plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }
    
    /**
     * Load a player's account from database into cache
     */
    public CompletableFuture<PlayerAccount> loadAccount(UUID uuid, String username) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = databaseManager.getConnection()) {
                String sql = "SELECT uuid, balance, frozen, last_earn FROM player_accounts WHERE uuid = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            BigDecimal balance = rs.getBigDecimal("balance");
                            boolean frozen = rs.getBoolean("frozen");
                            long lastEarn = rs.getLong("last_earn");
                            
                            PlayerAccount account = new PlayerAccount(uuid, balance, frozen, lastEarn);
                            
                            accountCache.put(uuid, account);
                            plugin.getLogger().info("Loaded account for " + uuid + " with balance $" + balance);
                            return account;
                        } else {
                            // Create new account
                            PlayerAccount newAccount = new PlayerAccount(uuid);
                            accountCache.put(uuid, newAccount);
                            saveAccountSync(newAccount); // Create in database
                            plugin.getLogger().info("Created new account for " + uuid);
                            return newAccount;
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load account for " + uuid, e);
                // Return a default account in case of database error
                PlayerAccount fallbackAccount = new PlayerAccount(uuid);
                accountCache.put(uuid, fallbackAccount);
                return fallbackAccount;
            }
        });
    }
    
    /**
     * Unload a player's account from cache (save if dirty)
     */
    public CompletableFuture<Void> unloadAccount(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            PlayerAccount account = accountCache.get(uuid);
            if (account != null) {
                if (account.isDirty()) {
                    saveAccountSync(account);
                }
                accountCache.remove(uuid);
                plugin.getLogger().info("Unloaded account for " + uuid);
            }
        });
    }
    
    /**
     * Get a player's account from cache
     */
    public PlayerAccount getAccount(UUID uuid) {
        return accountCache.get(uuid);
    }
    
    /**
     * Get or create a player's account
     */
    public PlayerAccount getOrCreateAccount(UUID uuid, String username) {
        PlayerAccount account = accountCache.get(uuid);
        if (account == null) {
            // This should normally not happen as accounts are loaded on join
            // But we provide a fallback for safety
            account = new PlayerAccount(uuid);
            accountCache.put(uuid, account);
            saveAccount(account); // Save asynchronously
            plugin.getLogger().warning("Account for " + uuid + " was not in cache, created new one");
        }
        return account;
    }
    
    /**
     * Check if a player has an account (in cache or database)
     */
    public boolean hasAccount(UUID uuid) {
        if (accountCache.containsKey(uuid)) {
            return true;
        }
        
        // Check database
        try (Connection conn = databaseManager.getConnection()) {
            String sql = "SELECT 1 FROM player_accounts WHERE uuid = ? LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error checking account existence for " + uuid, e);
            return false;
        }
    }
    
    /**
     * Save an account to database asynchronously
     */
    public void saveAccount(PlayerAccount account) {
        if (!account.isDirty()) {
            return; // No need to save
        }
        
        CompletableFuture.runAsync(() -> saveAccountSync(account));
    }
    
    /**
     * Save an account to database synchronously
     */
    private void saveAccountSync(PlayerAccount account) {
        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                INSERT INTO player_accounts (uuid, balance, frozen, last_earn) 
                VALUES (?, ?, ?, ?) 
                ON DUPLICATE KEY UPDATE 
                balance = VALUES(balance),
                frozen = VALUES(frozen),
                last_earn = VALUES(last_earn)
                """;
                
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, account.getUuid().toString());
                stmt.setBigDecimal(2, account.getBalance());
                stmt.setBoolean(3, account.isFrozen());
                stmt.setLong(4, account.getLastEarn());
                
                stmt.executeUpdate();
                account.markClean();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save account for " + account.getUuid(), e);
        }
    }
    
    /**
     * Save all dirty accounts
     */
    public CompletableFuture<Void> saveAllAccounts() {
        return CompletableFuture.runAsync(() -> {
            for (PlayerAccount account : accountCache.values()) {
                if (account.isDirty()) {
                    saveAccountSync(account);
                }
            }
            plugin.getLogger().info("Saved all dirty accounts");
        });
    }
    
    /**
     * Get the number of accounts currently in cache
     */
    public int getCachedAccountCount() {
        return accountCache.size();
    }
    
    /**
     * Get all cached accounts (for administrative purposes)
     */
    public Map<UUID, PlayerAccount> getCachedAccounts() {
        return new ConcurrentHashMap<>(accountCache);
    }
    
    /**
     * Force refresh an account from database
     */
    public CompletableFuture<PlayerAccount> refreshAccount(UUID uuid, String username) {
        accountCache.remove(uuid); // Remove from cache
        return loadAccount(uuid, username); // Reload from database
    }
    
    /**
     * Get account by username (less efficient, searches cache)
     * This method is deprecated since we're removing username support
     */
    @Deprecated
    public PlayerAccount getAccountByUsername(String username) {
        // Since we no longer store usernames, this method cannot work
        // Return null to indicate username-based lookups are not supported
        return null;
    }
    
    /**
     * Get or load account by UUID, even for offline players
     * This method will check cache first, then database if not found
     */
    public CompletableFuture<PlayerAccount> getOrLoadAccountByUUID(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // Check cache first
            PlayerAccount cachedAccount = accountCache.get(uuid);
            if (cachedAccount != null) {
                return cachedAccount;
            }
            
            // Not in cache, check database
            try (Connection conn = databaseManager.getConnection()) {
                String sql = "SELECT uuid, balance, frozen, last_earn FROM player_accounts WHERE uuid = ?";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, uuid.toString());
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            BigDecimal balance = rs.getBigDecimal("balance");
                            boolean frozen = rs.getBoolean("frozen");
                            long lastEarn = rs.getLong("last_earn");
                            
                            PlayerAccount account = new PlayerAccount(uuid, balance, frozen, lastEarn);
                            
                            // Don't add to cache for offline players - they'll be removed when we're done
                            // accountCache.put(uuid, account);
                            
                            plugin.getLogger().info("Loaded offline account for " + uuid + " with balance $" + balance);
                            return account;
                        } else {
                            // Account doesn't exist in database
                            return null;
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load account for UUID " + uuid, e);
                return null;
            }
        });
    }
    
    /**
     * Save an account to database without adding to cache (for offline players)
     */
    public CompletableFuture<Void> saveOfflineAccount(PlayerAccount account) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = databaseManager.getConnection()) {
                String sql = """
                    INSERT INTO player_accounts (uuid, balance, frozen, last_earn) 
                    VALUES (?, ?, ?, ?) 
                    ON DUPLICATE KEY UPDATE 
                    balance = VALUES(balance),
                    frozen = VALUES(frozen),
                    last_earn = VALUES(last_earn)
                    """;
                    
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, account.getUuid().toString());
                    stmt.setBigDecimal(2, account.getBalance());
                    stmt.setBoolean(3, account.isFrozen());
                    stmt.setLong(4, account.getLastEarn());
                    
                    stmt.executeUpdate();
                    account.markClean();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save offline account for " + account.getUuid(), e);
            }
        });
    }
    
    /**
     * Clear all cached accounts (save dirty ones first)
     */
    public CompletableFuture<Void> clearCache() {
        return CompletableFuture.runAsync(() -> {
            // Save all dirty accounts first
            for (PlayerAccount account : accountCache.values()) {
                if (account.isDirty()) {
                    saveAccountSync(account);
                }
            }
            
            accountCache.clear();
            plugin.getLogger().info("Cleared account cache");
        });
    }
}