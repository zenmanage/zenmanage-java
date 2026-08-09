package com.zenmanage.sdk.config;

/**
 * Logger abstraction used by the SDK.
 */
public interface Logger {
    void debug(String message);

    void info(String message);

    void warn(String message);

    void error(String message, Throwable throwable);
}
