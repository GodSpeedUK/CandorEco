package tech.aurasoftware.candoreco.configuration;

import org.bukkit.configuration.file.YamlConfiguration;
import tech.aurasoftware.candoreco.configuration.serialization.Serializable;
import tech.aurasoftware.candoreco.configuration.serialization.Serialization;
import tech.aurasoftware.candoreco.file.YamlFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration interface for managing configurable values in the CandorEco plugin.
 * Provides automatic loading and saving of configuration values with support for 
 * serialization of custom objects.
 */
public interface Configuration {
    
    /**
     * Gets the configuration path for this value
     * @return the path in the configuration file
     */
    String getPath();

    /**
     * Gets the current value of this configuration
     * @return the configuration value
     */
    Object getValue();

    /**
     * Sets the value of this configuration
     * @param value the new value
     */
    void setValue(Object value);

    /**
     * Loads configuration values from a YAML file.
     * Creates default values if they don't exist in the config.
     * 
     * @param yamlFile the YAML file to load from
     * @param values the configuration values to load
     */
    static void loadConfig(YamlFile yamlFile, Configuration... values) {
        boolean saveConfig = false;
        YamlConfiguration config = yamlFile.getConfig();

        for (Configuration configuration : values) {
            if (config.get(configuration.getPath()) == null) {
                saveConfig = true;
                
                // Handle serializable objects
                if (configuration.getValue() instanceof Serializable) {
                    Serializable serializable = (Serializable) configuration.getValue();
                    Map<String, Object> serialized = serializable.serialize();
                    for (String key : serialized.keySet()) {
                        config.set(configuration.getPath() + "." + key, serialized.get(key));
                    }
                    continue;
                }
                
                // Handle lists
                if (configuration.getValue() instanceof List) {
                    List<?> list = (List<?>) configuration.getValue();
                    if (list.isEmpty()) {
                        continue;
                    }

                    if (list.get(0) instanceof Serializable) {
                        for (int i = 0; i < list.size(); i++) {
                            Serializable serializable = (Serializable) list.get(i);
                            Map<String, Object> serialized = serializable.serialize();
                            for (String key : serialized.keySet()) {
                                config.set(configuration.getPath() + "." + i + "." + key, serialized.get(key));
                            }
                        }
                        continue;
                    }
                    config.set(configuration.getPath(), list);
                    continue;
                }
                
                config.set(configuration.getPath(), configuration.getValue());
                continue;
            }

            // Load values from config
            if (configuration.getValue() instanceof Serializable) {
                Serializable serializable = (Serializable) Serialization.deserialize(
                    configuration.getValue().getClass(), config, configuration.getPath());
                configuration.setValue(serializable);
                continue;
            }

            if (configuration.getValue() instanceof List) {
                List<?> list = (List<?>) configuration.getValue();

                if(list.isEmpty()){
                    throw new IllegalArgumentException("Default list is empty!, Path: " + configuration.getPath());
                }
                
                Class<?> type = list.get(0).getClass();

                // Check if type implements Serializable
                if(Serializable.class.isAssignableFrom(type)){
                    List<Object> newList = new ArrayList<>();
                    for(int i = 0; i < list.size(); i++){
                        Serializable serializable = (Serializable) Serialization.deserialize(
                            type, config, configuration.getPath() + "." + i);
                        newList.add(serializable);
                    }
                    configuration.setValue(newList);
                }
                continue;
            }

            configuration.setValue(config.get(configuration.getPath()));
        }

        if(saveConfig){
            yamlFile.saveConfig();
        }
    }

    /**
     * Gets the value as a string list
     * @return list of strings
     */
    default List<String> getStringList() {
        return (List<String>) getValue();
    }

    /**
     * Gets the value as a string
     * @return string value
     */
    default String getString() {
        return (String) getValue();
    }

    /**
     * Gets the value as an integer
     * @return integer value
     */
    default int getInt() {
        return (Integer) getValue();
    }

    /**
     * Gets the value as a double
     * @return double value
     */
    default double getDouble() {
        return (Double) getValue();
    }

    /**
     * Gets the value as a boolean
     * @return boolean value
     */
    default boolean getBoolean() {
        return (Boolean) getValue();
    }

    /**
     * Saves this configuration value to the YAML file
     * @param yamlFile the file to save to
     */
    default void saveValue(YamlFile yamlFile) {
        if (getValue() instanceof Serializable) {
            Serializable serializable = (Serializable) getValue();
            Map<String, Object> serialized = serializable.serialize();
            for (String key : serialized.keySet()) {
                yamlFile.getConfig().set(getPath() + "." + key, serialized.get(key));
            }
        } else {
            yamlFile.getConfig().set(getPath(), getValue());
        }
        yamlFile.saveConfig();
    }
}