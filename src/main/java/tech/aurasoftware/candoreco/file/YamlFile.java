package tech.aurasoftware.candoreco.file;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for managing YAML configuration files.
 * Handles automatic file creation, resource saving, and configuration loading.
 */
public class YamlFile {

    private final String name;
    private final String path;
    private final String folder;
    private final Plugin plugin;
    private final File file;
    private final YamlConfiguration config;

    /**
     * Creates a new YAML file handler
     * 
     * @param name the filename (e.g., "config.yml")
     * @param path the directory path
     * @param folder subdirectory within the path (can be null)
     * @param plugin the plugin instance for resource handling
     * @throws IOException if file operations fail
     */
    public YamlFile(String name, String path, String folder, Plugin plugin) throws IOException {
        this.plugin = plugin;
        this.name = name;
        this.path = path;
        this.folder = folder;

        // Create directory structure
        if (this.folder == null) {
            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            this.file = new File(directory, this.name);
        } else {
            File folderDir = new File(this.path + File.separator + folder);
            if (!folderDir.exists()) {
                folderDir.mkdirs();
            }
            this.file = new File(folderDir, this.name);
        }

        // Create file if it doesn't exist
        if (!this.file.exists()) {
            try {
                this.saveResource();
            } catch (IllegalArgumentException e) {
                // Resource doesn't exist in plugin jar, create empty file
                this.file.createNewFile();
            }
        }

        // Load configuration
        this.config = YamlConfiguration.loadConfiguration(this.file);
        this.config.options().copyDefaults(true);
        this.saveConfig();
    }

    /**
     * Saves the configuration to disk
     */
    public void saveConfig() {
        try {
            this.config.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a resource from the plugin jar to the file system
     */
    private void saveResource() {
        if (this.folder != null) {
            plugin.saveResource(this.folder + File.separator + this.name, false);
        } else {
            plugin.saveResource(this.name, false);
        }
    }

    /**
     * Gets the filename
     * @return the filename
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the directory path
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the subfolder name
     * @return the folder name or null
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Gets the plugin instance
     * @return the plugin
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the file object
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Gets the YAML configuration
     * @return the configuration
     */
    public YamlConfiguration getConfig() {
        return config;
    }
}