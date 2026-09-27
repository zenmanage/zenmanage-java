package com.zenmanage.sdk.config;

import com.zenmanage.sdk.cache.Cache;
import com.zenmanage.sdk.errors.ConfigurationException;
import java.util.Map;

/**
 * Fluent builder for SDK configuration.
 */
public final class ConfigBuilder {
    private static final String DEFAULT_API_ENDPOINT = "https://api.zenmanage.com";
    private static final String SERVER_KEY_PREFIX = "srv_";
    private static final String CLIENT_KEY_PREFIX = "cli_";
    private static final String MOBILE_KEY_PREFIX = "mob_";

    private String environmentToken;
    private int cacheTtlSeconds = 3600;
    private CacheBackend cacheBackend = CacheBackend.MEMORY;
    private String cacheDirectory;
    private boolean usageReportingEnabled = true;
    private String apiEndpoint = DEFAULT_API_ENDPOINT;
    private Logger logger;
    private Cache customCache;
    private String sdkVersion = "1.0.0";
    private String clientAgent = "zenmanage-java";

    private ConfigBuilder() {
    }

    public static ConfigBuilder create() {
        return new ConfigBuilder();
    }

    public static ConfigBuilder fromEnvironment() {
        return fromEnvironment(System.getenv());
    }

    static ConfigBuilder fromEnvironment(Map<String, String> env) {
        ConfigBuilder builder = create();

        if (env.containsKey("ZENMANAGE_ENVIRONMENT_TOKEN")) {
            builder.withEnvironmentToken(env.get("ZENMANAGE_ENVIRONMENT_TOKEN"));
        }

        if (env.containsKey("ZENMANAGE_CACHE_TTL")) {
            try {
                builder.withCacheTtlSeconds(Integer.parseInt(env.get("ZENMANAGE_CACHE_TTL")));
            } catch (NumberFormatException ignored) {
                // Ignore invalid numeric env input.
            }
        }

        if (env.containsKey("ZENMANAGE_CACHE_BACKEND")) {
            builder.withCacheBackend(env.get("ZENMANAGE_CACHE_BACKEND"));
        }

        if (env.containsKey("ZENMANAGE_CACHE_DIR")) {
            builder.withCacheDirectory(env.get("ZENMANAGE_CACHE_DIR"));
        }

        if (env.containsKey("ZENMANAGE_ENABLE_USAGE_REPORTING")) {
            String value = env.get("ZENMANAGE_ENABLE_USAGE_REPORTING");
            builder.withUsageReporting("true".equalsIgnoreCase(value) || "1".equals(value));
        }

        if (env.containsKey("ZENMANAGE_API_ENDPOINT")) {
            builder.withApiEndpoint(env.get("ZENMANAGE_API_ENDPOINT"));
        }

        return builder;
    }

    public ConfigBuilder withEnvironmentToken(String token) {
        this.environmentToken = token;
        return this;
    }

    public ConfigBuilder withCacheTtlSeconds(int ttlSeconds) {
        this.cacheTtlSeconds = ttlSeconds;
        return this;
    }

    public ConfigBuilder withCacheBackend(String backend) {
        if (backend == null) {
            throw new ConfigurationException("Cache backend cannot be null");
        }

        switch (backend.toLowerCase()) {
            case "memory":
                this.cacheBackend = CacheBackend.MEMORY;
                break;
            case "filesystem":
                this.cacheBackend = CacheBackend.FILESYSTEM;
                break;
            case "null":
                this.cacheBackend = CacheBackend.NULL;
                break;
            default:
                throw new ConfigurationException("Invalid cache backend: " + backend);
        }
        return this;
    }

    public ConfigBuilder withCacheDirectory(String cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
        return this;
    }

    public ConfigBuilder withUsageReporting(boolean enabled) {
        this.usageReportingEnabled = enabled;
        return this;
    }

    public ConfigBuilder withApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
        return this;
    }

    public ConfigBuilder withLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public ConfigBuilder withCache(Cache cache) {
        this.customCache = cache;
        return this;
    }

    public ConfigBuilder withSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
        return this;
    }

    public ConfigBuilder withClientAgent(String clientAgent) {
        this.clientAgent = clientAgent;
        return this;
    }

    public Config build() {
        if (environmentToken == null || environmentToken.isBlank()) {
            throw new ConfigurationException("Environment token is required");
        }

        validateEnvironmentToken(environmentToken);

        if (cacheTtlSeconds <= 0) {
            throw new ConfigurationException("Cache TTL must be greater than zero");
        }

        if (cacheBackend == CacheBackend.FILESYSTEM && customCache == null
            && (cacheDirectory == null || cacheDirectory.isBlank())) {
            throw new ConfigurationException("Cache directory is required for filesystem cache");
        }

        Logger configLogger = logger == null ? new NullLogger() : logger;

        return new Config(
            environmentToken,
            cacheTtlSeconds,
            cacheBackend,
            cacheDirectory,
            usageReportingEnabled,
            apiEndpoint,
            configLogger,
            customCache,
            sdkVersion,
            clientAgent
        );
    }

    private void validateEnvironmentToken(String token) {
        if (token.startsWith(CLIENT_KEY_PREFIX)) {
            throw new ConfigurationException(
                "Invalid environment token for Java runtime: client key provided. Use a server key (srv_...)"
            );
        }

        if (token.startsWith(MOBILE_KEY_PREFIX)) {
            throw new ConfigurationException(
                "Invalid environment token for Java runtime: mobile key provided. Use a server key (srv_...)"
            );
        }

        if (!token.startsWith(SERVER_KEY_PREFIX)) {
            throw new ConfigurationException(
                "Invalid environment token format. Expected one of: srv_, cli_, or mob_."
            );
        }
    }
}
