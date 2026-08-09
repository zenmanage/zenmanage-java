package com.zenmanage.sdk.spring;

import com.zenmanage.sdk.config.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridges SDK logging into the application's SLF4J logger pipeline.
 */
public final class SpringLoggerAdapter implements Logger {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger("com.zenmanage.sdk");

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
