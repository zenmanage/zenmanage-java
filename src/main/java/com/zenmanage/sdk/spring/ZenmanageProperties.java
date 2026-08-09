package com.zenmanage.sdk.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot configuration properties for the Zenmanage SDK.
 */
@ConfigurationProperties(prefix = "zenmanage")
public final class ZenmanageProperties {
    private String environmentToken;
    private int cacheTtlSeconds = 3600;
    private String cacheBackend = "memory";
    private String cacheDirectory;
    private boolean usageReporting = true;
    private String apiEndpoint = "https://api.zenmanage.com";

    public String getEnvironmentToken() {
        return environmentToken;
    }

    public void setEnvironmentToken(String environmentToken) {
        this.environmentToken = environmentToken;
    }

    public int getCacheTtlSeconds() {
        return cacheTtlSeconds;
    }

    public void setCacheTtlSeconds(int cacheTtlSeconds) {
        this.cacheTtlSeconds = cacheTtlSeconds;
    }

    public String getCacheBackend() {
        return cacheBackend;
    }

    public void setCacheBackend(String cacheBackend) {
        this.cacheBackend = cacheBackend;
    }

    public String getCacheDirectory() {
        return cacheDirectory;
    }

    public void setCacheDirectory(String cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    public boolean isUsageReporting() {
        return usageReporting;
    }

    public void setUsageReporting(boolean usageReporting) {
        this.usageReporting = usageReporting;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }
}
