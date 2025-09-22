package tech.aurasoftware.candoreco.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tech.aurasoftware.candoreco.CandorEco;
import tech.aurasoftware.candoreco.economy.AccountManager;

import java.util.logging.Level;

public class PlayerListener implements Listener {
    
    private final CandorEco plugin;
    private final AccountManager accountManager;
    
    public PlayerListener(CandorEco plugin, AccountManager accountManager) {
        this.plugin = plugin;
        this.accountManager = accountManager;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Load player's economy data asynchronously
        accountManager.loadAccount(player.getUniqueId(), player.getName())
            .thenAccept(account -> {
                plugin.getLogger().info("Economy data loaded for " + player.getName());
            })
            .exceptionally(throwable -> {
                plugin.getLogger().log(Level.SEVERE, 
                    "Failed to load economy data for " + player.getName(), throwable);
                return null;
            });
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Unload player's economy data asynchronously
        accountManager.unloadAccount(player.getUniqueId())
            .thenRun(() -> {
                plugin.getLogger().info("Economy data unloaded for " + player.getName());
            })
            .exceptionally(throwable -> {
                plugin.getLogger().log(Level.SEVERE, 
                    "Failed to unload economy data for " + player.getName(), throwable);
                return null;
            });
    }
}