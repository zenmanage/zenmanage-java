package com.zenmanage.sdk.errors;

/**
 * Thrown when configuration values are invalid.
 */
public class ConfigurationException extends ZenmanageException {
    public ConfigurationException(String message) {
        super(message);
    }
}
