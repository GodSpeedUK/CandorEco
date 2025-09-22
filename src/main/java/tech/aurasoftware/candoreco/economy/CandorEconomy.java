package tech.aurasoftware.candoreco.economy;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import tech.aurasoftware.candoreco.CandorEco;

/**
 * CandorEco implementation of Vault's Economy interface
 */
public class CandorEconomy implements Economy {
    
    private final CandorEco plugin;
    private final AccountManager accountManager;
    private final String currencyName = "Dollar";
    private final String currencyNamePlural = "Dollars";
    
    public CandorEconomy(CandorEco plugin, AccountManager accountManager) {
        this.plugin = plugin;
        this.accountManager = accountManager;
    }
    
    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }
    
    @Override
    public String getName() {
        return "CandorEco";
    }
    
    @Override
    public boolean hasBankSupport() {
        return false; // Banks not implemented in this version
    }
    
    @Override
    public int fractionalDigits() {
        return 2; // Support cents
    }
    
    @Override
    public String format(double amount) {
        return String.format("$%.2f", amount);
    }
    
    @Override
    public String currencyNamePlural() {
        return currencyNamePlural;
    }
    
    @Override
    public String currencyNameSingular() {
        return currencyName;
    }
    
    @Override
    public boolean hasAccount(String playerName) {
        try {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
            return accountManager.hasAccount(player.getUniqueId());
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error checking account for " + playerName, e);
            return false;
        }
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return accountManager.hasAccount(player.getUniqueId());
    }
    
    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName); // World-specific economies not implemented
    }
    
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player); // World-specific economies not implemented
    }
    
    @Override
    public double getBalance(String playerName) {
        try {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
            return getBalance(player);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error getting balance for " + playerName, e);
            return 0.0;
        }
    }
    
    @Override
    public double getBalance(OfflinePlayer player) {
        try {
            PlayerAccount account = accountManager.getAccount(player.getUniqueId());
            return account != null ? account.getBalanceAsDouble() : 0.0;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error getting balance for " + player.getName(), e);
            return 0.0;
        }
    }
    
    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName); // World-specific economies not implemented
    }
    
    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player); // World-specific economies not implemented
    }
    
    @Override
    public boolean has(String playerName, double amount) {
        try {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
            return has(player, amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error checking balance for " + playerName, e);
            return false;
        }
    }
    
    @Override
    public boolean has(OfflinePlayer player, double amount) {
        try {
            PlayerAccount account = accountManager.getAccount(player.getUniqueId());
            return account != null && account.hasBalance(amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error checking balance for " + player.getName(), e);
            return false;
        }
    }
    
    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount); // World-specific economies not implemented
    }
    
    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount); // World-specific economies not implemented
    }
    
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        try {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
            return withdrawPlayer(player, amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error withdrawing from " + playerName, e);
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Internal error");
        }
    }
    
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        try {
            if (amount < 0) {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amounts");
            }
            
            PlayerAccount account = accountManager.getOrCreateAccount(player.getUniqueId(), player.getName());
            
            if (!account.hasBalance(amount)) {
                return new EconomyResponse(amount, account.getBalanceAsDouble(), 
                    EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
            }
            
            account.withdraw(amount);
            accountManager.saveAccount(account);
            
            return new EconomyResponse(amount, account.getBalanceAsDouble(), 
                EconomyResponse.ResponseType.SUCCESS, null);
                
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error withdrawing from " + player.getName(), e);
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Internal error");
        }
    }
    
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount); // World-specific economies not implemented
    }
    
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount); // World-specific economies not implemented
    }
    
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        try {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
            return depositPlayer(player, amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error depositing to " + playerName, e);
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Internal error");
        }
    }
    
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        try {
            if (amount < 0) {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amounts");
            }
            
            PlayerAccount account = accountManager.getOrCreateAccount(player.getUniqueId(), player.getName());
            account.deposit(amount);
            accountManager.saveAccount(account);
            
            return new EconomyResponse(amount, account.getBalanceAsDouble(), 
                EconomyResponse.ResponseType.SUCCESS, null);
                
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error depositing to " + player.getName(), e);
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Internal error");
        }
    }
    
    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount); // World-specific economies not implemented
    }
    
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount); // World-specific economies not implemented
    }
    
    @Override
    public boolean createPlayerAccount(String playerName) {
        try {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
            return createPlayerAccount(player);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error creating account for " + playerName, e);
            return false;
        }
    }
    
    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        try {
            accountManager.getOrCreateAccount(player.getUniqueId(), player.getName());
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error creating account for " + player.getName(), e);
            return false;
        }
    }
    
    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName); // World-specific economies not implemented
    }
    
    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player); // World-specific economies not implemented
    }
    
    // Bank methods - not implemented
    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public List<String> getBanks() {
        return List.of(); // No banks supported
    }
    
    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
    
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Banks are not supported");
    }
}