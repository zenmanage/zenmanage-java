package com.zenmanage.sdk.errors;

/**
 * Base runtime exception for the Zenmanage Java SDK.
 */
public class ZenmanageException extends RuntimeException {
    public ZenmanageException(String message) {
        super(message);
    }

    public ZenmanageException(String message, Throwable cause) {
        super(message, cause);
    }
}
