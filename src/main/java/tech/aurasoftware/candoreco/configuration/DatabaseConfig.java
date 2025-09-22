package tech.aurasoftware.candoreco.configuration;

import tech.aurasoftware.candoreco.configuration.serialization.Serializable;

/**
 * Database configuration class for MySQL connection settings.
 * Implements Serializable to work with the configuration system.
 */
public class DatabaseConfig implements Serializable {
    
    private String host = "localhost";
    private int port = 3306;
    private String database = "candoreco";
    private String username = "root";
    private String password = "";
    private boolean useSSL = false;
    private int maxPoolSize = 10;
    private int connectionTimeout = 30000; // 30 seconds
    private boolean autoReconnect = true;

    /**
     * Default constructor for serialization
     */
    public DatabaseConfig() {}

    /**
     * Constructor with basic settings
     */
    public DatabaseConfig(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    // Getters
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabase() { return database; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isUseSSL() { return useSSL; }
    public int getMaxPoolSize() { return maxPoolSize; }
    public int getConnectionTimeout() { return connectionTimeout; }
    public boolean isAutoReconnect() { return autoReconnect; }

    // Setters
    public void setHost(String host) { this.host = host; }
    public void setPort(int port) { this.port = port; }
    public void setDatabase(String database) { this.database = database; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setUseSSL(boolean useSSL) { this.useSSL = useSSL; }
    public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    public void setAutoReconnect(boolean autoReconnect) { this.autoReconnect = autoReconnect; }

    /**
     * Builds the JDBC URL from the configuration
     * @return the complete JDBC URL
     */
    public String getJdbcUrl() {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:mysql://")
           .append(host).append(":").append(port)
           .append("/").append(database)
           .append("?useSSL=").append(useSSL)
           .append("&autoReconnect=").append(autoReconnect)
           .append("&useUnicode=true")
           .append("&characterEncoding=UTF-8")
           .append("&allowPublicKeyRetrieval=true")
           .append("&serverTimezone=UTC");
        return url.toString();
    }

    @Override
    public String toString() {
        return "DatabaseConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", database='" + database + '\'' +
                ", username='" + username + '\'' +
                ", useSSL=" + useSSL +
                ", maxPoolSize=" + maxPoolSize +
                ", connectionTimeout=" + connectionTimeout +
                ", autoReconnect=" + autoReconnect +
                '}';
    }
}