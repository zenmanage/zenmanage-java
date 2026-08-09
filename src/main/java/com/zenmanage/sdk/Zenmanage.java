package com.zenmanage.sdk;

import com.zenmanage.sdk.api.ApiClient;
import com.zenmanage.sdk.cache.Cache;
import com.zenmanage.sdk.cache.FileSystemCache;
import com.zenmanage.sdk.cache.InMemoryCache;
import com.zenmanage.sdk.cache.NullCache;
import com.zenmanage.sdk.config.CacheBackend;
import com.zenmanage.sdk.config.Config;
import com.zenmanage.sdk.errors.ConfigurationException;
import com.zenmanage.sdk.flags.FlagManager;
import com.zenmanage.sdk.rules.RuleEngine;

/**
 * Main SDK entry point.
 */
public final class Zenmanage {
    private final FlagManager flagManager;

    public Zenmanage(Config config) {
        Cache cache = createCache(config);

        ApiClient apiClient = new ApiClient(
            config.getEnvironmentToken(),
            config.getApiEndpoint(),
            config.getLogger(),
            config.isUsageReportingEnabled(),
            config.getSdkVersion(),
            config.getClientAgent()
        );

        RuleEngine ruleEngine = new RuleEngine();

        this.flagManager = new FlagManager(
            apiClient,
            cache,
            ruleEngine,
            config.getCacheTtlSeconds(),
            config.getLogger()
        );
    }

    public FlagManager flags() {
        return flagManager;
    }

    private Cache createCache(Config config) {
        if (config.getCustomCache() != null) {
            return config.getCustomCache();
        }

        if (config.getCacheBackend() == CacheBackend.MEMORY) {
            return new InMemoryCache();
        }

        if (config.getCacheBackend() == CacheBackend.NULL) {
            return new NullCache();
        }

        if (config.getCacheBackend() == CacheBackend.FILESYSTEM) {
            if (config.getCacheDirectory() == null || config.getCacheDirectory().isBlank()) {
                throw new ConfigurationException("Cache directory is required for filesystem cache");
            }
            return new FileSystemCache(config.getCacheDirectory());
        }

        throw new ConfigurationException("Invalid cache backend: " + config.getCacheBackend());
    }
}
