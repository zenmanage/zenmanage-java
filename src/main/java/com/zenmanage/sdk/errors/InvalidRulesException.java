package com.zenmanage.sdk.errors;

/**
 * Thrown when the rules response format is invalid.
 */
public class InvalidRulesException extends ZenmanageException {
    public InvalidRulesException(String message) {
        super(message);
    }
}
