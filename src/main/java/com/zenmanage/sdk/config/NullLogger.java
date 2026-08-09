package com.zenmanage.sdk.config;

/**
 * Logger implementation that drops all log lines.
 */
public final class NullLogger implements Logger {
    @Override
    public void debug(String message) {
        // no-op
    }

    @Override
    public void info(String message) {
        // no-op
    }

    @Override
    public void warn(String message) {
        // no-op
    }

    @Override
    public void error(String message, Throwable throwable) {
        // no-op
    }
}
