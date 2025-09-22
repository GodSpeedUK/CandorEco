package tech.aurasoftware.candoreco.economy;

import java.math.BigDecimal;
import java.util.UUID;

import org.bukkit.Bukkit;

/**
 * Represents a player's economy account
 */
public class PlayerAccount {
    
    private final UUID uuid;
    private BigDecimal balance;
    private boolean frozen;
    private long lastEarn;
    private boolean dirty; // Flag to track if data needs to be saved
    
    public PlayerAccount(UUID uuid) {
        this(uuid, BigDecimal.ZERO, false, 0L);
    }
    
    public PlayerAccount(UUID uuid, BigDecimal balance) {
        this(uuid, balance, false, 0L);
    }
    
    public PlayerAccount(UUID uuid, BigDecimal balance, boolean frozen, long lastEarn) {
        this.uuid = uuid;
        this.balance = balance;
        this.frozen = frozen;
        this.lastEarn = lastEarn;
        this.dirty = false;
    }
    
    /**
     * Get the player's UUID
     */
    public UUID getUuid() {
        return uuid;
    }
    
    public String getUsername(){
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    /**
     * Get the player's balance
     */
    public BigDecimal getBalance() {
        return balance;
    }
    
    /**
     * Set the player's balance
     */
    public void setBalance(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        this.balance = balance;
        this.dirty = true;
    }
    
    /**
     * Add money to the player's balance
     */
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
        this.dirty = true;
    }
    
    /**
     * Remove money from the player's balance
     * @param amount the amount to withdraw
     * @return true if the withdrawal was successful, false if insufficient funds
     */
    public boolean withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        if (this.balance.compareTo(amount) >= 0) {
            this.balance = this.balance.subtract(amount);
            this.dirty = true;
            return true;
        }
        return false;
    }
    
    /**
     * Check if the player has at least the specified amount
     */
    public boolean hasBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
    
    /**
     * Get the balance as a double (for Vault compatibility)
     */
    public double getBalanceAsDouble() {
        return balance.doubleValue();
    }
    
    /**
     * Set the balance from a double (for Vault compatibility)
     */
    public void setBalance(double balance) {
        setBalance(BigDecimal.valueOf(balance));
    }
    
    /**
     * Deposit using double (for Vault compatibility)
     */
    public void deposit(double amount) {
        deposit(BigDecimal.valueOf(amount));
    }
    
    /**
     * Withdraw using double (for Vault compatibility)
     */
    public boolean withdraw(double amount) {
        return withdraw(BigDecimal.valueOf(amount));
    }
    
    /**
     * Check if the player has at least the specified amount (double version)
     */
    public boolean hasBalance(double amount) {
        return hasBalance(BigDecimal.valueOf(amount));
    }
    
    /**
     * Check if the player is frozen
     */
    public boolean isFrozen() {
        return frozen;
    }
    
    /**
     * Set the player's frozen status
     */
    public void setFrozen(boolean frozen) {
        if (this.frozen != frozen) {
            this.frozen = frozen;
            this.dirty = true;
        }
    }
    
    /**
     * Get the timestamp of the last earn command usage
     */
    public long getLastEarn() {
        return lastEarn;
    }
    
    /**
     * Set the timestamp of the last earn command usage
     */
    public void setLastEarn(long lastEarn) {
        if (this.lastEarn != lastEarn) {
            this.lastEarn = lastEarn;
            this.dirty = true;
        }
    }
    
    /**
     * Check if the player can use the earn command again
     * @param cooldownMs cooldown period in milliseconds
     * @return true if enough time has passed since last earn
     */
    public boolean canEarn(long cooldownMs) {
        if (frozen) {
            return false;
        }
        return System.currentTimeMillis() - lastEarn >= cooldownMs;
    }
    
    /**
     * Update the last earn timestamp to current time
     */
    public void updateLastEarn() {
        setLastEarn(System.currentTimeMillis());
    }
    
    /**
     * Check if the account data has been modified and needs saving
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * Mark the account as clean (data has been saved)
     */
    public void markClean() {
        this.dirty = false;
    }
    
    /**
     * Mark the account as dirty (data needs saving)
     */
    public void markDirty() {
        this.dirty = true;
    }
    
    @Override
    public String toString() {
        return String.format("PlayerAccount{uuid=%s, balance=%s, frozen=%s, lastEarn=%s, dirty=%s}", 
            uuid, balance, frozen, lastEarn, dirty);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlayerAccount that = (PlayerAccount) obj;
        return uuid.equals(that.uuid);
    }
    
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}