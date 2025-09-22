package tech.aurasoftware.candoreco.configuration;

import tech.aurasoftware.candoreco.util.Placeholder;
import tech.aurasoftware.candoreco.util.Text;

/**
 * Configuration enum for CandorEco plugin settings.
 * Implements Configuration interface to work with the configuration system.
 */
public enum EconomyConfig implements Configuration {
    
    DATABASE("database", new DatabaseConfig()),
    CURRENCY_NAME("currency.name", "Coins"),
    CURRENCY_PLURAL("currency.plural", "Coins"),
    CURRENCY_SYMBOL("currency.symbol", "$"),
    DEFAULT_BALANCE("economy.default-balance", 100.0),
    MAX_BALANCE("economy.max-balance", 1000000000.0),
    MIN_BALANCE("economy.min-balance", 0.0),
    FRACTIONAL_DIGITS("economy.fractional-digits", 2),
    ENABLE_LOGGING("economy.enable-logging", true),
    OFFLINE_PAYMENTS("economy.offline-payments", true),
    
    // Earn command configuration
    EARN_AMOUNT("economy.earn.amount", 50.0),
    EARN_COOLDOWN("economy.earn.cooldown-hours", 24),
    EARN_ENABLED("economy.earn.enabled", true),
    
    // Interest system configuration
    INTEREST_ENABLED("economy.interest.enabled", true),
    INTEREST_RATE("economy.interest.rate", 2.5),
    INTEREST_INTERVAL_MINUTES("economy.interest.interval-minutes", 20),
    
    // Message configurations
    PREFIX("messages.prefix", "&6[CandorEco] &f"),
    INSUFFICIENT_FUNDS("messages.insufficient-funds", "{prefix}&cYou don't have enough {currency}!"),
    BALANCE_UPDATED("messages.balance-updated", "{prefix}&aYour balance has been updated to &6{balance} {currency}&a!"),
    BALANCE_DISPLAY("messages.balance-display", "{prefix}&aYour balance: &6{balance} {currency}"),
    PAYMENT_SENT("messages.payment-sent", "{prefix}&aYou sent &6{amount} {currency} &ato &6{target}&a!"),
    PAYMENT_RECEIVED("messages.payment-received", "{prefix}&aYou received &6{amount} {currency} &afrom &6{player}&a!"),
    PLAYER_NOT_FOUND("messages.player-not-found", "{prefix}&cPlayer &6{player} &cnot found!"),
    INVALID_AMOUNT("messages.invalid-amount", "{prefix}&cInvalid amount! Please enter a valid number."),
    CANNOT_PAY_SELF("messages.cannot-pay-self", "{prefix}&cYou cannot send money to yourself!"),
    COMMAND_USAGE("messages.command-usage", "{prefix}&cUsage: {usage}"),
    NO_PERMISSION("messages.no-permission", "{prefix}&cYou don't have permission to do that!"),
    
    // Help messages
    ADMIN_HELP_HEADER("messages.help.admin-header", "{prefix}&6=== CandorEco Admin Commands ==="),
    ADMIN_HELP_GIVE("messages.help.admin-give", "{prefix}&e/eco give <player> <amount> &7- Give money to a player"),
    ADMIN_HELP_TAKE("messages.help.admin-take", "{prefix}&e/eco take <player> <amount> &7- Take money from a player"),
    ADMIN_HELP_SET("messages.help.admin-set", "{prefix}&e/eco set <player> <amount> &7- Set a player's balance"),
    ADMIN_HELP_RELOAD("messages.help.admin-reload", "{prefix}&e/eco reload &7- Reload the plugin configuration"),
    
    // Admin messages
    ADMIN_SET_BALANCE("messages.admin.set-balance", "{prefix}&aSet &6{target}'s &abalance to &6{amount} {currency}&a!"),
    ADMIN_ADD_BALANCE("messages.admin.add-balance", "{prefix}&aAdded &6{amount} {currency} &ato &6{target}'s &aaccount!"),
    ADMIN_REMOVE_BALANCE("messages.admin.remove-balance", "{prefix}&aRemoved &6{amount} {currency} &afrom &6{target}'s &aaccount!"),
    ADMIN_RESET_BALANCE("messages.admin.reset-balance", "{prefix}&aReset &6{target}'s &abalance to default!"),
    
    // Economy status messages
    ECONOMY_ENABLED("messages.economy.enabled", "{prefix}&aEconomy system enabled!"),
    ECONOMY_DISABLED("messages.economy.disabled", "{prefix}&cEconomy system disabled!"),
    DATABASE_CONNECTED("messages.database.connected", "{prefix}&aDatabase connected successfully!"),
    DATABASE_ERROR("messages.database.error", "{prefix}&cDatabase connection error: {error}"),
    ACCOUNT_CREATED("messages.account.created", "{prefix}&aAccount created for &6{player}&a!"),
    ACCOUNT_LOADED("messages.account.loaded", "{prefix}&aAccount loaded for &6{player}&a!"),
    
    // Admin error messages
    ECONOMY_NOT_READY("messages.admin.economy-not-ready", "{prefix}&cEconomy system not ready yet. Please try again later."),
    ADMIN_SET_USAGE("messages.admin.set-usage", "{prefix}&cUsage: /eco set <player> <amount>"),
    AMOUNT_CANNOT_BE_NEGATIVE("messages.admin.amount-negative", "{prefix}&cAmount cannot be negative!"),
    ADMIN_SET_BALANCE_DETAILED("messages.admin.set-balance-detailed", "{prefix}&aSet &6{target}'s &abalance to &6{amount} &a(was &6{old_balance}&a)"),
    ADMIN_BALANCE_UPDATED_NOTIFICATION("messages.admin.balance-updated-notification", "{prefix}&aYour balance has been set to &6{amount} &aby an administrator"),
    INVALID_AMOUNT_DETAILED("messages.admin.invalid-amount-detailed", "{prefix}&cInvalid amount: &6{amount}"),
    RELOAD_SUCCESS("messages.admin.reload-success", "{prefix}&aAll economy data saved and configuration reloaded successfully!"),
    RELOAD_ERROR("messages.admin.reload-error", "{prefix}&cError during reload: &6{error}"),
    ECONOMY_NOT_READY_SHORT("messages.admin.economy-not-ready-short", "{prefix}&cEconomy system not ready yet."),
    
    // Earn command messages
    EARN_SUCCESS("messages.earn.success", "{prefix}&aYou earned &6{amount} {currency}&a!"),
    EARN_COOLDOWN_MESSAGE("messages.earn.cooldown", "{prefix}&cYou must wait &6{time} &cbefore earning again!"),
    EARN_DISABLED("messages.earn.disabled", "{prefix}&cThe earn command is currently disabled!"),
    EARN_FROZEN("messages.earn.frozen", "{prefix}&cYou cannot earn money while frozen!"),
    
    // Freeze command messages  
    PLAYER_FROZEN("messages.freeze.player-frozen", "{prefix}&6{player} &ahas been frozen!"),
    PLAYER_UNFROZEN("messages.freeze.player-unfrozen", "{prefix}&6{player} &ahas been unfrozen!"),
    PLAYER_FREEZE_STATUS("messages.freeze.status", "{prefix}&6{player} &ais currently &6{status}&a!"),
    FREEZE_NOTIFICATION("messages.freeze.notification", "{prefix}&cYou have been frozen by an administrator!"),
    UNFREEZE_NOTIFICATION("messages.freeze.unfreeze-notification", "{prefix}&aYou have been unfrozen by an administrator!"),
    SENDER_FROZEN("messages.freeze.sender-frozen", "{prefix}&cYou cannot send money while frozen!"),
    RECEIVER_FROZEN("messages.freeze.receiver-frozen", "{prefix}&cYou cannot send money to a frozen player!"),
    
    // Interest system messages
    INTEREST_EARNED("messages.interest.earned", "{prefix}&aYou earned &6{amount} {currency} &ain interest!"),
    INTEREST_DISABLED("messages.interest.disabled", "{prefix}&cInterest system is currently disabled!");

    private final String path;
    private Object value;

    EconomyConfig(String path, Object defaultValue) {
        this.path = path;
        this.value = defaultValue;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Gets the database configuration
     * @return DatabaseConfig object
     */
    public DatabaseConfig getDatabaseConfig() {
        return (DatabaseConfig) getValue();
    }

    /**
     * Gets the currency name (singular)
     * @return currency name
     */
    public String getCurrencyName() {
        return getString();
    }

    /**
     * Gets the currency name (plural)
     * @return currency plural name
     */
    public String getCurrencyPlural() {
        return getString();
    }

    /**
     * Gets the currency symbol
     * @return currency symbol
     */
    public String getCurrencySymbol() {
        return getString();
    }

    /**
     * Gets the default starting balance for new players
     * @return default balance
     */
    public double getDefaultBalance() {
        return getDouble();
    }

    /**
     * Gets the maximum allowed balance
     * @return maximum balance
     */
    public double getMaxBalance() {
        return getDouble();
    }

    /**
     * Gets the minimum allowed balance
     * @return minimum balance
     */
    public double getMinBalance() {
        return getDouble();
    }

    /**
     * Gets the number of fractional digits for currency display
     * @return fractional digits
     */
    public int getFractionalDigits() {
        return getInt();
    }

    /**
     * Checks if economy logging is enabled
     * @return true if logging is enabled
     */
    public boolean isLoggingEnabled() {
        return getBoolean();
    }

    /**
     * Gets a formatted message with the prefix
     * @return formatted message
     */
    public String getFormattedMessage(Placeholder... placeholders) {
        String message = getString();
        String prefix = PREFIX.getString();
        return Text.c(Placeholder.apply(message.replace("{prefix}", prefix), placeholders));
    }
}