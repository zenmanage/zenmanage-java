package com.zenmanage.sdk.config;

import com.zenmanage.sdk.cache.Cache;

/**
 * Immutable SDK configuration.
 */
public final class Config {
    private final String environmentToken;
    private final int cacheTtlSeconds;
    private final CacheBackend cacheBackend;
    private final String cacheDirectory;
    private final boolean usageReportingEnabled;
    private final String apiEndpoint;
    private final Logger logger;
    private final Cache customCache;
    private final String sdkVersion;
    private final String clientAgent;

    Config(
        String environmentToken,
        int cacheTtlSeconds,
        CacheBackend cacheBackend,
        String cacheDirectory,
        boolean usageReportingEnabled,
        String apiEndpoint,
        Logger logger,
        Cache customCache,
        String sdkVersion,
        String clientAgent
    ) {
        this.environmentToken = environmentToken;
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.cacheBackend = cacheBackend;
        this.cacheDirectory = cacheDirectory;
        this.usageReportingEnabled = usageReportingEnabled;
        this.apiEndpoint = apiEndpoint;
        this.logger = logger;
        this.customCache = customCache;
        this.sdkVersion = sdkVersion;
        this.clientAgent = clientAgent;
    }

    public String getEnvironmentToken() {
        return environmentToken;
    }

    public int getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public CacheBackend getCacheBackend() {
        return cacheBackend;
    }

    public String getCacheDirectory() {
        return cacheDirectory;
    }

    public boolean isUsageReportingEnabled() {
        return usageReportingEnabled;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public Logger getLogger() {
        return logger;
    }

    public Cache getCustomCache() {
        return customCache;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public String getClientAgent() {
        return clientAgent;
    }
}
