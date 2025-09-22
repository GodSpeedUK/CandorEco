package tech.aurasoftware.candoreco.commands.impl;

import java.util.logging.Level;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import tech.aurasoftware.candoreco.CandorEco;
import tech.aurasoftware.candoreco.commands.AuraCommand;
import tech.aurasoftware.candoreco.commands.AuraSubCommand;
import tech.aurasoftware.candoreco.commands.CommandContext;
import tech.aurasoftware.candoreco.configuration.EconomyConfig;
import tech.aurasoftware.candoreco.economy.AccountManager;
import tech.aurasoftware.candoreco.economy.PlayerAccount;
import tech.aurasoftware.candoreco.util.NumberUtil;
import tech.aurasoftware.candoreco.util.Placeholder;

/**
 * Example command class demonstrating the annotation-based command system
 */
public class EconomyCommands {
    
    private final CandorEco plugin;
    
    public EconomyCommands(CandorEco plugin) {
        this.plugin = plugin;
    }
    
    @AuraCommand(
        name = "balance",
        description = "Check your current balance",
        permission = "candoreco.balance",
        usage = "/balance [player]"
    )
    public void balanceCommand(CommandContext context) {
        AccountManager accountManager = plugin.getAccountManager();
        if (accountManager == null) {
            context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                new Placeholder("{error}", "Economy system not ready")));
            return;
        }
        
        if (context.getArgCount() == 0) {
            // Show own balance
            if (!context.isPlayer()) {
                context.sendMessage(EconomyConfig.COMMAND_USAGE.getFormattedMessage(
                    new Placeholder("{usage}", "/balance [player]")));
                return;
            }
            
            Player player = context.getPlayer();
            PlayerAccount account = accountManager.getAccount(player.getUniqueId());
            
            if (account == null) {
                context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                    new Placeholder("{error}", "Account not loaded")));
                return;
            }
            
            context.sendMessage(EconomyConfig.BALANCE_DISPLAY.getFormattedMessage(
                Placeholder.balance(NumberUtil.formatCurrency(account.getBalance())),
                new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString())
            ));
        } else {
            // Show other player's balance
            if (!context.hasPermission("candoreco.balance.others")) {
                context.sendMessage(EconomyConfig.NO_PERMISSION.getFormattedMessage());
                return;
            }
            
            String targetName = context.getArg(0);
            // Try to find online player first
            Player targetPlayer = plugin.getServer().getPlayer(targetName);
            if (targetPlayer != null) {
                PlayerAccount account = accountManager.getAccount(targetPlayer.getUniqueId());
                if (account != null) {
                    context.sendMessage(EconomyConfig.BALANCE_DISPLAY.getFormattedMessage(
                        Placeholder.balance(NumberUtil.formatCurrency(account.getBalance())),
                        new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString()),
                        new Placeholder("{player}", targetPlayer.getName())
                    ).replace("Your balance", targetPlayer.getName() + "'s balance"));
                    return;
                }
            }
            
            // Try to find offline player
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(targetName);
            if (offlinePlayer.hasPlayedBefore()) {
                context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                    new Placeholder("{error}", targetName + "'s account is not currently loaded")));
            } else {
                context.sendMessage(EconomyConfig.PLAYER_NOT_FOUND.getFormattedMessage(
                    Placeholder.player(targetName)));
            }
        }
    }
    
    @AuraCommand(
        name = "pay",
        description = "Pay money to another player",
        permission = "candoreco.pay",
        usage = "/pay <player> <amount>"
    )
    public void payCommand(CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(EconomyConfig.COMMAND_USAGE.getFormattedMessage(
                new Placeholder("{usage}", "/pay <player> <amount>")));
            return;
        }
        
        AccountManager accountManager = plugin.getAccountManager();
        if (accountManager == null) {
            context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                new Placeholder("{error}", "Economy system not ready")));
            return;
        }
        
        if (context.getArgCount() < 2) {
            context.sendMessage(EconomyConfig.COMMAND_USAGE.getFormattedMessage(
                new Placeholder("{usage}", "/pay <player> <amount>")));
            return;
        }
        
        String targetName = context.getArg(0);
        String amountStr = context.getArg(1);
        
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                context.sendMessage(EconomyConfig.INVALID_AMOUNT.getFormattedMessage());
                return;
            }
            
            Player sender = context.getPlayer();
            PlayerAccount senderAccount = accountManager.getAccount(sender.getUniqueId());
            
            if (senderAccount == null) {
                context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                    new Placeholder("{error}", "Your account is not loaded")));
                return;
            }
            
            // Prevent self-payment
            if (sender.getName().equalsIgnoreCase(targetName)) {
                context.sendMessage(EconomyConfig.CANNOT_PAY_SELF.getFormattedMessage());
                return;
            }
            
            if (!senderAccount.hasBalance(amount)) {
                context.sendMessage(EconomyConfig.INSUFFICIENT_FUNDS.getFormattedMessage(
                    new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString())
                ));
                return;
            }
            
            // Check if sender is frozen
            if (senderAccount.isFrozen()) {
                context.sendMessage(EconomyConfig.SENDER_FROZEN.getFormattedMessage());
                return;
            }
            
            PlayerAccount targetAccount = null;
            // Check if target is online first
            Player targetPlayer = plugin.getServer().getPlayer(targetName);
            if (targetPlayer != null) {
                targetAccount = accountManager.getAccount(targetPlayer.getUniqueId());
                
                // Check if target player is frozen
                if (targetAccount != null && targetAccount.isFrozen()) {
                    context.sendMessage(EconomyConfig.RECEIVER_FROZEN.getFormattedMessage());
                    return;
                }
            }
            
            if (targetAccount == null) {
                // Try to find offline player by UUID
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(targetName);
                if (offlinePlayer.hasPlayedBefore()) {
                    // Check if they have an account in the database
                    accountManager.getOrLoadAccountByUUID(offlinePlayer.getUniqueId()).thenAccept(offlineAccount -> {
                        if (offlineAccount == null) {
                            context.sendMessage(EconomyConfig.PLAYER_NOT_FOUND.getFormattedMessage(
                                Placeholder.player(targetName)));
                            return;
                        }
                        
                        // Check if offline target player is frozen
                        if (offlineAccount.isFrozen()) {
                            context.sendMessage(EconomyConfig.RECEIVER_FROZEN.getFormattedMessage());
                            return;
                        }
                        
                        // Perform the transaction with offline player
                        senderAccount.withdraw(amount);
                        offlineAccount.deposit(amount);
                        
                        // Save both accounts
                        accountManager.saveAccount(senderAccount);
                        accountManager.saveOfflineAccount(offlineAccount).thenRun(() -> {
                            context.sendMessage(EconomyConfig.PAYMENT_SENT.getFormattedMessage(
                                Placeholder.amount(NumberUtil.formatCurrency(amount)),
                                new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString()),
                                Placeholder.target(targetName)
                            ));
                            
                            plugin.getLogger().info(sender.getName() + " sent " + NumberUtil.formatCurrency(amount) + 
                                " to offline player " + targetName);
                        });
                    }).exceptionally(throwable -> {
                        context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                            new Placeholder("{error}", "Failed to process payment to offline player")));
                        plugin.getLogger().log(Level.SEVERE, "Error processing offline payment", throwable);
                        return null;
                    });
                } else {
                    context.sendMessage(EconomyConfig.PLAYER_NOT_FOUND.getFormattedMessage(
                        Placeholder.player(targetName)));
                }
                return;
            }
            
            // Online player transaction (existing logic)
            // Perform the transaction
            senderAccount.withdraw(amount);
            targetAccount.deposit(amount);
            
            // Save both accounts
            accountManager.saveAccount(senderAccount);
            accountManager.saveAccount(targetAccount);
            
            context.sendMessage(EconomyConfig.PAYMENT_SENT.getFormattedMessage(
                Placeholder.amount(NumberUtil.formatCurrency(amount)),
                new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString()),
                Placeholder.target(targetName)
            ));
            
            // Notify target player if online
            Player onlineTargetPlayer = plugin.getServer().getPlayer(targetAccount.getUuid());
            if (targetPlayer != null) {
                targetPlayer.sendMessage(EconomyConfig.PAYMENT_RECEIVED.getFormattedMessage(
                    Placeholder.amount(NumberUtil.formatCurrency(amount)),
                    new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString()),
                    Placeholder.player(sender.getName())
                ));
            }
            
        } catch (NumberFormatException e) {
            context.sendMessage(EconomyConfig.INVALID_AMOUNT.getFormattedMessage());
        }
    }
    
    @AuraCommand(
        name = "eco",
        description = "Economy admin commands",
        permission = "candoreco.admin"
    )
    public void ecoAdminCommand(CommandContext context) {
        // Main command - shows help when no subcommand is provided
        context.sendMessage(EconomyConfig.ADMIN_HELP_HEADER.getFormattedMessage());
        context.sendMessage(EconomyConfig.ADMIN_HELP_GIVE.getFormattedMessage());
        context.sendMessage(EconomyConfig.ADMIN_HELP_TAKE.getFormattedMessage());
        context.sendMessage(EconomyConfig.ADMIN_HELP_SET.getFormattedMessage());
        context.sendMessage(EconomyConfig.ADMIN_HELP_RELOAD.getFormattedMessage());
    }
    
    @AuraSubCommand(
        parent = "eco",
        name = "give",
        description = "Give money to a player",
        permission = "candoreco.admin.give",
        usage = "/eco give <player> <amount>"
    )
    public void ecoGiveCommand(CommandContext context) {
        AccountManager accountManager = plugin.getAccountManager();
        if (accountManager == null) {
            context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                new Placeholder("{error}", "Economy system not ready")));
            return;
        }
        
        if (context.getArgCount() < 2) {
            context.sendMessage(EconomyConfig.COMMAND_USAGE.getFormattedMessage(
                new Placeholder("{usage}", "/eco give <player> <amount>")));
            return;
        }
        
        String targetName = context.getArg(0);
        String amountStr = context.getArg(1);
        
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                context.sendMessage(EconomyConfig.INVALID_AMOUNT.getFormattedMessage());
                return;
            }
            
            PlayerAccount targetAccount = accountManager.getAccountByUsername(targetName);
            if (targetAccount == null) {
                // Try to find offline player by UUID
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(targetName);
                if (offlinePlayer.hasPlayedBefore()) {
                    // Check if they have an account in the database
                    accountManager.getOrLoadAccountByUUID(offlinePlayer.getUniqueId()).thenAccept(offlineAccount -> {
                        if (offlineAccount == null) {
                            context.sendMessage(EconomyConfig.PLAYER_NOT_FOUND.getFormattedMessage(
                                new Placeholder("{player}", targetName)));
                            return;
                        }
                        
                        // Give money to offline player
                        offlineAccount.deposit(amount);
                        
                        // Save the account
                        accountManager.saveOfflineAccount(offlineAccount).thenRun(() -> {
                            context.sendMessage(EconomyConfig.ADMIN_ADD_BALANCE.getFormattedMessage(
                                new Placeholder("{target}", offlineAccount.getUsername()),
                                new Placeholder("{amount}", NumberUtil.formatCurrency(amount)),
                                new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString())
                            ));
                            
                            plugin.getLogger().info("Admin " + context.getSender().getName() + " gave " + 
                                NumberUtil.formatCurrency(amount) + " to offline player " + offlineAccount.getUsername());
                        });
                    }).exceptionally(throwable -> {
                        context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                            new Placeholder("{error}", "Failed to process admin give to offline player")));
                        plugin.getLogger().log(Level.SEVERE, "Error processing offline admin give", throwable);
                        return null;
                    });
                } else {
                    context.sendMessage(EconomyConfig.PLAYER_NOT_FOUND.getFormattedMessage(
                        new Placeholder("{player}", targetName)));
                }
                return;
            }
            
            // Online player transaction (existing logic)
            targetAccount.deposit(amount);
            accountManager.saveAccount(targetAccount);
            
            context.sendMessage(EconomyConfig.ADMIN_ADD_BALANCE.getFormattedMessage(
                Placeholder.amount(NumberUtil.formatCurrency(amount)),
                new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString()),
                Placeholder.target(targetAccount.getUsername())
            ));
            
            // Notify target player if online
            Player targetPlayer = plugin.getServer().getPlayer(targetAccount.getUuid());
            if (targetPlayer != null) {
                targetPlayer.sendMessage(EconomyConfig.PAYMENT_RECEIVED.getFormattedMessage(
                    Placeholder.amount(NumberUtil.formatCurrency(amount)),
                    new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString()),
                    Placeholder.player("an administrator")
                ));
            }
            
        } catch (NumberFormatException e) {
            context.sendMessage(EconomyConfig.INVALID_AMOUNT.getFormattedMessage());
        }
    }
    
    @AuraSubCommand(
        parent = "eco",
        name = "take",
        description = "Take money from a player",
        permission = "candoreco.admin.take",
        usage = "/eco take <player> <amount>",
        aliases = {"remove"}
    )
    public void ecoTakeCommand(CommandContext context) {
        AccountManager accountManager = plugin.getAccountManager();
        if (accountManager == null) {
            context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                new Placeholder("{error}", "Economy system not ready")));
            return;
        }
        
        if (context.getArgCount() < 2) {
            context.sendMessage(EconomyConfig.COMMAND_USAGE.getFormattedMessage(
                new Placeholder("{usage}", "/eco take <player> <amount>")));
            return;
        }
        
        String targetName = context.getArg(0);
        String amountStr = context.getArg(1);
        
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                context.sendMessage(EconomyConfig.INVALID_AMOUNT.getFormattedMessage());
                return;
            }
            
            PlayerAccount targetAccount = accountManager.getAccountByUsername(targetName);
            if (targetAccount == null) {
                // Try to find offline player by UUID
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(targetName);
                if (offlinePlayer.hasPlayedBefore()) {
                    // Check if they have an account in the database
                    accountManager.getOrLoadAccountByUUID(offlinePlayer.getUniqueId()).thenAccept(offlineAccount -> {
                        if (offlineAccount == null) {
                            context.sendMessage(EconomyConfig.PLAYER_NOT_FOUND.getFormattedMessage(
                                new Placeholder("{player}", targetName)));
                            return;
                        }
                        
                        if (!offlineAccount.hasBalance(amount)) {
                            context.sendMessage(EconomyConfig.INSUFFICIENT_FUNDS.getFormattedMessage(
                                new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString()),
                                new Placeholder("{balance}", NumberUtil.formatCurrency(offlineAccount.getBalance())),
                                new Placeholder("{player}", offlineAccount.getUsername())
                            ).replace("You don't", offlineAccount.getUsername() + " doesn't"));
                            return;
                        }
                        
                        // Take money from offline player
                        offlineAccount.withdraw(amount);
                        
                        // Save the account
                        accountManager.saveOfflineAccount(offlineAccount).thenRun(() -> {
                            context.sendMessage(EconomyConfig.ADMIN_REMOVE_BALANCE.getFormattedMessage(
                                new Placeholder("{target}", offlineAccount.getUsername()),
                                new Placeholder("{amount}", NumberUtil.formatCurrency(amount)),
                                new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString())
                            ));
                            
                            plugin.getLogger().info("Admin " + context.getSender().getName() + " took " + 
                                NumberUtil.formatCurrency(amount) + " from offline player " + offlineAccount.getUsername());
                        });
                    }).exceptionally(throwable -> {
                        context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                            new Placeholder("{error}", "Failed to process admin take from offline player")));
                        plugin.getLogger().log(Level.SEVERE, "Error processing offline admin take", throwable);
                        return null;
                    });
                } else {
                    context.sendMessage(EconomyConfig.PLAYER_NOT_FOUND.getFormattedMessage(
                        new Placeholder("{player}", targetName)));
                }
                return;
            }
            
            // Online player transaction (existing logic)
            if (!targetAccount.hasBalance(amount)) {
                context.sendMessage(EconomyConfig.INSUFFICIENT_FUNDS.getFormattedMessage(
                    new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString()),
                    new Placeholder("{balance}", NumberUtil.formatCurrency(targetAccount.getBalance())),
                    Placeholder.player(targetAccount.getUsername())
                ).replace("You don't", targetAccount.getUsername() + " doesn't"));
                return;
            }
            
            targetAccount.withdraw(amount);
            accountManager.saveAccount(targetAccount);
            
            context.sendMessage(EconomyConfig.ADMIN_REMOVE_BALANCE.getFormattedMessage(
                Placeholder.amount(NumberUtil.formatCurrency(amount)),
                new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString()),
                Placeholder.target(targetAccount.getUsername())
            ));
            
            // Notify target player if online
            Player targetPlayer = plugin.getServer().getPlayer(targetAccount.getUuid());
            if (targetPlayer != null) {
                targetPlayer.sendMessage(EconomyConfig.BALANCE_UPDATED.getFormattedMessage(
                    Placeholder.balance(NumberUtil.formatCurrency(targetAccount.getBalance())),
                    new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString())
                ));
            }
            
        } catch (NumberFormatException e) {
            context.sendMessage(EconomyConfig.INVALID_AMOUNT.getFormattedMessage());
        }
    }
    
    @AuraSubCommand(
        parent = "eco",
        name = "set",
        description = "Set a player's balance",
        permission = "candoreco.admin.set",
        usage = "/eco set <player> <amount>"
    )
    public void ecoSetCommand(CommandContext context) {
        AccountManager accountManager = plugin.getAccountManager();
        if (accountManager == null) {
            context.sendMessage(EconomyConfig.ECONOMY_NOT_READY.getFormattedMessage());
            return;
        }
        
        if (context.getArgCount() < 2) {
            context.sendMessage(EconomyConfig.ADMIN_SET_USAGE.getFormattedMessage());
            return;
        }
        
        String targetName = context.getArg(0);
        String amountStr = context.getArg(1);
        
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount < 0) {
                context.sendMessage(EconomyConfig.AMOUNT_CANNOT_BE_NEGATIVE.getFormattedMessage());
                return;
            }
            
            PlayerAccount targetAccount = accountManager.getAccountByUsername(targetName);
            if (targetAccount == null) {
                // Try to find offline player by UUID
                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(targetName);
                if (offlinePlayer.hasPlayedBefore()) {
                    // Check if they have an account in the database
                    accountManager.getOrLoadAccountByUUID(offlinePlayer.getUniqueId()).thenAccept(offlineAccount -> {
                        if (offlineAccount == null) {
                            context.sendMessage(EconomyConfig.PLAYER_NOT_FOUND.getFormattedMessage(
                                new Placeholder("{player}", targetName)));
                            return;
                        }
                        
                        double oldBalance = offlineAccount.getBalanceAsDouble();
                        offlineAccount.setBalance(amount);
                        
                        // Save the account
                        accountManager.saveOfflineAccount(offlineAccount).thenRun(() -> {
                            context.sendMessage(EconomyConfig.ADMIN_SET_BALANCE_DETAILED.getFormattedMessage(
                                new Placeholder("{target}", offlineAccount.getUsername()),
                                new Placeholder("{amount}", NumberUtil.formatCurrency(amount)),
                                new Placeholder("{old_balance}", NumberUtil.formatCurrency(oldBalance)),
                                new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString())
                            ));
                            
                            plugin.getLogger().info("Admin " + context.getSender().getName() + " set " + 
                                offlineAccount.getUsername() + "'s balance to " + NumberUtil.formatCurrency(amount));
                        });
                    }).exceptionally(throwable -> {
                        context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                            new Placeholder("{error}", "Failed to process admin set for offline player")));
                        plugin.getLogger().log(Level.SEVERE, "Error processing offline admin set", throwable);
                        return null;
                    });
                } else {
                    context.sendMessage(EconomyConfig.PLAYER_NOT_FOUND.getFormattedMessage(
                        new Placeholder("{player}", targetName)));
                }
                return;
            }
            
            // Online player transaction (existing logic)
            double oldBalance = targetAccount.getBalanceAsDouble();
            targetAccount.setBalance(amount);
            accountManager.saveAccount(targetAccount);
            
            context.sendMessage(EconomyConfig.ADMIN_SET_BALANCE_DETAILED.getFormattedMessage(
                new Placeholder("{target}", targetAccount.getUsername()),
                new Placeholder("{amount}", NumberUtil.formatCurrency(amount)),
                new Placeholder("{old_balance}", NumberUtil.formatCurrency(oldBalance)),
                new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString())
            ));
            
            // Notify target player if online
            Player targetPlayer = plugin.getServer().getPlayer(targetAccount.getUuid());
            if (targetPlayer != null) {
                targetPlayer.sendMessage(EconomyConfig.ADMIN_BALANCE_UPDATED_NOTIFICATION.getFormattedMessage(
                    new Placeholder("{amount}", NumberUtil.formatCurrency(amount)),
                    new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString())
                ));
            }
            
        } catch (NumberFormatException e) {
            context.sendMessage(EconomyConfig.INVALID_AMOUNT_DETAILED.getFormattedMessage(
                new Placeholder("{amount}", amountStr)));
        }
    }
    
    @AuraSubCommand(
        parent = "eco",
        name = "reload",
        description = "Reload the plugin configuration",
        permission = "candoreco.admin.reload",
        aliases = {"rl"}
    )
    public void ecoReloadCommand(CommandContext context) {
        AccountManager accountManager = plugin.getAccountManager();
        if (accountManager != null) {
            // Save all accounts before reload
            accountManager.saveAllAccounts().thenRun(() -> {
                context.sendMessage(EconomyConfig.RELOAD_SUCCESS.getFormattedMessage());
            }).exceptionally(throwable -> {
                context.sendMessage(EconomyConfig.RELOAD_ERROR.getFormattedMessage(
                    new Placeholder("{error}", throwable.getMessage())));
                return null;
            });
        } else {
            context.sendMessage(EconomyConfig.ECONOMY_NOT_READY_SHORT.getFormattedMessage());
        }
    }
    
    @AuraSubCommand(
        parent = "eco",
        name = "freeze",
        description = "Freeze or unfreeze a player",
        permission = "candoreco.admin.freeze",
        usage = "/eco freeze <player> [true/false]"
    )
    public void ecoFreezeCommand(CommandContext context) {
        if (context.getArgs().length < 1) {
            context.sendMessage(EconomyConfig.COMMAND_USAGE.getFormattedMessage(
                new Placeholder("{usage}", "/eco freeze <player> [true/false]")));
            return;
        }
        
        AccountManager accountManager = plugin.getAccountManager();
        if (accountManager == null) {
            context.sendMessage(EconomyConfig.ECONOMY_NOT_READY_SHORT.getFormattedMessage());
            return;
        }
        
        String targetName = context.getArg(0);
        final boolean[] freezeState = {true}; // Default to freeze, using array for mutability in lambda
        
        // Check if freeze state is specified
        if (context.getArgs().length >= 2) {
            String freezeArg = context.getArg(1).toLowerCase();
            if (freezeArg.equals("false") || freezeArg.equals("unfreeze") || freezeArg.equals("0")) {
                freezeState[0] = false;
            }
        }
        
        // Try to find online player first
        Player targetPlayer = plugin.getServer().getPlayer(targetName);
        if (targetPlayer != null) {
            PlayerAccount account = accountManager.getAccount(targetPlayer.getUniqueId());
            if (account != null) {
                // Toggle or set freeze state
                boolean finalFreezeState = freezeState[0];
                if (context.getArgs().length < 2) {
                    finalFreezeState = !account.isFrozen(); // Toggle if no state specified
                }
                
                account.setFrozen(finalFreezeState);
                accountManager.saveAccount(account);
                
                // Send messages
                if (finalFreezeState) {
                    context.sendMessage(EconomyConfig.PLAYER_FROZEN.getFormattedMessage(
                        new Placeholder("{player}", targetPlayer.getName())));
                    targetPlayer.sendMessage(EconomyConfig.FREEZE_NOTIFICATION.getFormattedMessage());
                } else {
                    context.sendMessage(EconomyConfig.PLAYER_UNFROZEN.getFormattedMessage(
                        new Placeholder("{player}", targetPlayer.getName())));
                    targetPlayer.sendMessage(EconomyConfig.UNFREEZE_NOTIFICATION.getFormattedMessage());
                }
                return;
            }
        }
        
        // Try offline player
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(targetName);
        if (offlinePlayer.hasPlayedBefore()) {
            accountManager.getOrLoadAccountByUUID(offlinePlayer.getUniqueId()).thenAccept(offlineAccount -> {
                if (offlineAccount == null) {
                    context.sendMessage(EconomyConfig.PLAYER_NOT_FOUND.getFormattedMessage(
                        new Placeholder("{player}", targetName)));
                    return;
                }
                
                // Toggle or set freeze state
                boolean finalFreezeState = (context.getArgs().length < 2) ? 
                    !offlineAccount.isFrozen() : freezeState[0]; // Toggle if no state specified, otherwise use specified state
                
                offlineAccount.setFrozen(finalFreezeState);
                accountManager.saveOfflineAccount(offlineAccount).thenRun(() -> {
                    if (finalFreezeState) {
                        context.sendMessage(EconomyConfig.PLAYER_FROZEN.getFormattedMessage(
                            new Placeholder("{player}", targetName)));
                    } else {
                        context.sendMessage(EconomyConfig.PLAYER_UNFROZEN.getFormattedMessage(
                            new Placeholder("{player}", targetName)));
                    }
                });
            }).exceptionally(throwable -> {
                context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                    new Placeholder("{error}", "Failed to load player account")));
                plugin.getLogger().log(Level.SEVERE, "Error loading account for freeze command", throwable);
                return null;
            });
        } else {
            context.sendMessage(EconomyConfig.PLAYER_NOT_FOUND.getFormattedMessage(
                new Placeholder("{player}", targetName)));
        }
    }
    
    @AuraCommand(
        name = "earn",
        description = "Earn money with a cooldown period",
        permission = "candoreco.earn",
        usage = "/earn"
    )
    public void earnCommand(CommandContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            context.sendMessage(EconomyConfig.COMMAND_USAGE.getFormattedMessage(
                new Placeholder("{usage}", "This command can only be used by players")));
            return;
        }
        
        // Check if earn command is enabled
        if (!EconomyConfig.EARN_ENABLED.getBoolean()) {
            context.sendMessage(EconomyConfig.EARN_DISABLED.getFormattedMessage());
            return;
        }
        
        AccountManager accountManager = plugin.getAccountManager();
        if (accountManager == null) {
            context.sendMessage(EconomyConfig.ECONOMY_NOT_READY_SHORT.getFormattedMessage());
            return;
        }
        
        PlayerAccount account = accountManager.getAccount(player.getUniqueId());
        if (account == null) {
            context.sendMessage(EconomyConfig.DATABASE_ERROR.getFormattedMessage(
                new Placeholder("{error}", "Your account is not loaded")));
            return;
        }
        
        // Check if player is frozen
        if (account.isFrozen()) {
            context.sendMessage(EconomyConfig.EARN_FROZEN.getFormattedMessage());
            return;
        }
        
        // Check cooldown
        int cooldownHours = EconomyConfig.EARN_COOLDOWN.getInt();
        long cooldownMs = cooldownHours * 60L * 60L * 1000L; // Convert hours to milliseconds
        
        if (!account.canEarn(cooldownMs)) {
            long timeLeft = (account.getLastEarn() + cooldownMs) - System.currentTimeMillis();
            String timeLeftFormatted = formatTime(timeLeft);
            
            context.sendMessage(EconomyConfig.EARN_COOLDOWN_MESSAGE.getFormattedMessage(
                new Placeholder("{time}", timeLeftFormatted)
            ));
            return;
        }
        
        // Give money to player
        double earnAmount = EconomyConfig.EARN_AMOUNT.getDouble();
        account.deposit(earnAmount);
        account.updateLastEarn();
        
        // Save account
        accountManager.saveAccount(account);
        
        context.sendMessage(EconomyConfig.EARN_SUCCESS.getFormattedMessage(
            Placeholder.amount(NumberUtil.formatCurrency(earnAmount)),
            new Placeholder("{currency}", EconomyConfig.CURRENCY_NAME.getString())
        ));
        
        plugin.getLogger().info(player.getName() + " earned " + NumberUtil.formatCurrency(earnAmount));
    }
    
    /**
     * Format milliseconds into a human-readable time string
     */
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            hours = hours % 24;
            return days + "d " + hours + "h";
        } else if (hours > 0) {
            minutes = minutes % 60;
            return hours + "h " + minutes + "m";
        } else if (minutes > 0) {
            seconds = seconds % 60;
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }
}