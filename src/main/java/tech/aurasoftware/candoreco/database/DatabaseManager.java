package tech.aurasoftware.candoreco.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import tech.aurasoftware.candoreco.CandorEco;
import tech.aurasoftware.candoreco.configuration.DatabaseConfig;
import tech.aurasoftware.candoreco.configuration.EconomyConfig;

/**
 * Database manager for MySQL connections and operations
 */
public class DatabaseManager {
    
    private final CandorEco plugin;
    private Connection connection;
    private DatabaseConfig dbConfig;
    
    public DatabaseManager(CandorEco plugin) {
        this.plugin = plugin;
        loadConfiguration();
    }
    
    /**
     * Load database configuration from the configuration system
     */
    private void loadConfiguration() {
        this.dbConfig = EconomyConfig.DATABASE.getDatabaseConfig();
        plugin.getLogger().info("Database configuration loaded: " + dbConfig.toString());
    }
    
    /**
     * Initialize database connection and create tables
     */
    public CompletableFuture<Boolean> initialize() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                connect();
                createTables();
                updateSchema(); // Add schema updates for existing tables
                plugin.getLogger().info("Database initialized successfully!");
                return true;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to initialize database", e);
                return false;
            }
        });
    }
    
    /**
     * Establish database connection
     */
    private void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        
        String url = dbConfig.getJdbcUrl();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, dbConfig.getUsername(), dbConfig.getPassword());
            plugin.getLogger().info("Connected to MySQL database: " + dbConfig.getDatabase());
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found", e);
        }
    }
    
    /**
     * Create necessary database tables
     */
    private void createTables() throws SQLException {
        String createAccountsTable = """
            CREATE TABLE IF NOT EXISTS player_accounts (
                uuid VARCHAR(36) PRIMARY KEY,
                balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
                frozen BOOLEAN NOT NULL DEFAULT FALSE,
                last_earn BIGINT NOT NULL DEFAULT 0
            )
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(createAccountsTable)) {
            stmt.executeUpdate();
            plugin.getLogger().info("Player accounts table created/verified successfully");
        }
    }
    
    /**
     * Update database schema for existing installations
     */
    private void updateSchema() throws SQLException {
        try {
            // Add frozen column if it doesn't exist
            addColumnIfNotExists("player_accounts", "frozen", "BOOLEAN NOT NULL DEFAULT FALSE");
            
            // Add last_earn column if it doesn't exist
            addColumnIfNotExists("player_accounts", "last_earn", "BIGINT NOT NULL DEFAULT 0");
            
            plugin.getLogger().info("Database schema updated successfully");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update database schema, some features may not work correctly", e);
            // Don't rethrow - let the plugin continue to work with basic functionality
        }
    }
    
    /**
     * Add a column to a table if it doesn't already exist
     */
    private void addColumnIfNotExists(String tableName, String columnName, String columnDefinition) throws SQLException {
        // Check if column exists
        String checkColumnSql = """
            SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = DATABASE() 
            AND TABLE_NAME = ? 
            AND COLUMN_NAME = ?
            """;
            
        try (PreparedStatement checkStmt = connection.prepareStatement(checkColumnSql)) {
            checkStmt.setString(1, tableName);
            checkStmt.setString(2, columnName);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Column doesn't exist, add it
                    String addColumnSql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition;
                    try (PreparedStatement addStmt = connection.prepareStatement(addColumnSql)) {
                        addStmt.executeUpdate();
                        plugin.getLogger().log(Level.INFO, "Added column {0} to table {1}", new Object[]{columnName, tableName});
                    }
                }
            }
        }
    }
    
    /**
     * Get database connection
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }
    
    /**
     * Close database connection
     */
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Database connection closed");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error closing database connection", e);
            }
        }
    }
    
    /**
     * Execute a query asynchronously
     */
    public CompletableFuture<Void> executeAsync(String sql, Object... params) {
        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Database query error: " + sql, e);
            }
        });
    }
    
    /**
     * Check if the database connection is valid
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

}