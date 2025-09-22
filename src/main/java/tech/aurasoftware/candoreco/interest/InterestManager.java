package tech.aurasoftware.candoreco.interest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import tech.aurasoftware.candoreco.configuration.EconomyConfig;
import tech.aurasoftware.candoreco.economy.AccountManager;
import tech.aurasoftware.candoreco.economy.PlayerAccount;
import tech.aurasoftware.candoreco.util.Placeholder;

/**
 * Manages the interest system for online players
 * Provides periodic interest payments to non-frozen players
 */
public class InterestManager {
    
    private final JavaPlugin plugin;
    private final AccountManager accountManager;
    private BukkitTask interestTask;
    
    public InterestManager(JavaPlugin plugin, AccountManager accountManager) {
        this.plugin = plugin;
        this.accountManager = accountManager;
    }
    
    /**
     * Start the interest system
     */
    public void start() {
        if (!EconomyConfig.INTEREST_ENABLED.getBoolean()) {
            plugin.getLogger().info("Interest system is disabled in configuration");
            return;
        }
        
        if (interestTask != null) {
            plugin.getLogger().warning("Interest system is already running!");
            return;
        }
        
        long intervalTicks = EconomyConfig.INTEREST_INTERVAL_MINUTES.getInt() * 20 * 60; // Convert minutes to ticks
        double interestRate = EconomyConfig.INTEREST_RATE.getDouble();
        
        interestTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                processInterestPayments();
            } catch (RuntimeException e) {
                plugin.getLogger().log(Level.SEVERE, "Error processing interest payments", e);
            }
        }, intervalTicks, intervalTicks);
        
        plugin.getLogger().info(String.format("Interest system started: %.2f%% every %d minutes", 
            interestRate, EconomyConfig.INTEREST_INTERVAL_MINUTES.getInt()));
    }
    
    /**
     * Stop the interest system
     */
    public void stop() {
        if (interestTask != null) {
            interestTask.cancel();
            interestTask = null;
            plugin.getLogger().info("Interest system stopped");
        }
    }
    
    /**
     * Process interest payments for all online, non-frozen players
     */
    private void processInterestPayments() {
        if (!EconomyConfig.INTEREST_ENABLED.getBoolean()) {
            stop(); // Stop if disabled during runtime
            return;
        }
        
        double interestRate = EconomyConfig.INTEREST_RATE.getDouble() / 100.0; // Convert percentage to decimal
        int playersProcessed = 0;
        double totalInterestPaid = 0.0;
        
        // Process all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                UUID playerId = player.getUniqueId();
                PlayerAccount account = accountManager.getAccount(playerId);
                
                if (account == null) {
                    plugin.getLogger().log(Level.WARNING, "No account found for online player: {0}", player.getName());
                    continue;
                }
                
                // Skip frozen players
                if (account.isFrozen()) {
                    continue;
                }
                
                double currentBalance = account.getBalanceAsDouble();
                
                // Calculate interest (minimum balance to earn interest could be added here)
                if (currentBalance > 0) {
                    double interestAmount = calculateInterest(currentBalance, interestRate);
                    
                    if (interestAmount > 0) {
                        // Add interest to account
                        account.setBalance(BigDecimal.valueOf(currentBalance + interestAmount));
                        accountManager.saveAccount(account);
                        
                        // Notify player on main thread
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            notifyPlayer(player, interestAmount);
                        });
                        
                        playersProcessed++;
                        totalInterestPaid += interestAmount;
                    }
                }
                
            } catch (RuntimeException e) {
                plugin.getLogger().log(Level.WARNING, 
                    "Error processing interest for player: " + player.getName(), e);
            }
        }
        
        if (playersProcessed > 0) {
            plugin.getLogger().info(String.format("Interest processed: %d players received $%.2f total", 
                playersProcessed, totalInterestPaid));
        }
    }
    
    /**
     * Calculate interest amount based on current balance and rate
     * @param balance Current balance
     * @param rate Interest rate (as decimal, e.g., 0.025 for 2.5%)
     * @return Interest amount
     */
    private double calculateInterest(double balance, double rate) {
        BigDecimal balanceDecimal = BigDecimal.valueOf(balance);
        BigDecimal rateDecimal = BigDecimal.valueOf(rate);
        BigDecimal interestDecimal = balanceDecimal.multiply(rateDecimal);
        
        // Round to 2 decimal places
        return interestDecimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
    
    /**
     * Notify a player that they received interest
     * @param player The player to notify
     * @param amount The interest amount
     */
    private void notifyPlayer(Player player, double amount) {
        String currency = amount == 1.0 ? 
            EconomyConfig.CURRENCY_NAME.getString() : 
            EconomyConfig.CURRENCY_PLURAL.getString();
            
        String message = EconomyConfig.INTEREST_EARNED.getFormattedMessage(
            new Placeholder("{amount}", String.format("%.2f", amount)),
            new Placeholder("{currency}", currency)
        );
        
        player.sendMessage(message);
    }
    
    /**
     * Check if the interest system is currently running
     * @return true if running
     */
    public boolean isRunning() {
        return interestTask != null && !interestTask.isCancelled();
    }
    
    /**
     * Get the current interest rate
     * @return interest rate percentage
     */
    public double getInterestRate() {
        return EconomyConfig.INTEREST_RATE.getDouble();
    }
    
    /**
     * Get the current interest interval in minutes
     * @return interval in minutes
     */
    public int getInterestInterval() {
        return EconomyConfig.INTEREST_INTERVAL_MINUTES.getInt();
    }
}