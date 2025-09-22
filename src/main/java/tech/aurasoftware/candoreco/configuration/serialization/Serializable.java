package tech.aurasoftware.candoreco.configuration.serialization;

import java.util.Map;

/**
 * Interface for objects that can be serialized to and from configuration files.
 * Implementing classes can be automatically converted to YAML configuration
 * and reconstructed from configuration data.
 */
public interface Serializable {

    /**
     * Serializes this object to a map of configuration values.
     * Uses the Serialization utility class to handle reflection-based serialization.
     * 
     * @return a map containing the object's serialized data
     */
    default Map<String, Object> serialize() {
        return Serialization.serialize(this);
    }
}