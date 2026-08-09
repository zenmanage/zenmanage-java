package com.zenmanage.sdk.spring;

import com.zenmanage.sdk.Zenmanage;
import com.zenmanage.sdk.cache.Cache;
import com.zenmanage.sdk.config.Config;
import com.zenmanage.sdk.config.ConfigBuilder;
import com.zenmanage.sdk.config.Logger;
import com.zenmanage.sdk.flags.FlagManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for the Zenmanage SDK.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Zenmanage.class)
@ConditionalOnProperty(prefix = "zenmanage", name = "environment-token")
@EnableConfigurationProperties(ZenmanageProperties.class)
public class ZenmanageAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(Logger.class)
    public Logger zenmanageLogger() {
        return new SpringLoggerAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(Config.class)
    public Config zenmanageConfig(
        ZenmanageProperties properties,
        ObjectProvider<Cache> cacheProvider,
        ObjectProvider<Logger> loggerProvider
    ) {
        ConfigBuilder builder = ConfigBuilder.create()
            .withEnvironmentToken(properties.getEnvironmentToken())
            .withCacheTtlSeconds(properties.getCacheTtlSeconds())
            .withCacheBackend(properties.getCacheBackend())
            .withUsageReporting(properties.isUsageReporting())
            .withApiEndpoint(properties.getApiEndpoint());

        if (properties.getCacheDirectory() != null && !properties.getCacheDirectory().isBlank()) {
            builder.withCacheDirectory(properties.getCacheDirectory());
        }

        Cache cache = cacheProvider.getIfAvailable();
        if (cache != null) {
            builder.withCache(cache);
        }

        Logger logger = loggerProvider.getIfAvailable();
        if (logger != null) {
            builder.withLogger(logger);
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Zenmanage zenmanage(Config config) {
        return new Zenmanage(config);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlagManager zenmanageFlagManager(Zenmanage zenmanage) {
        return zenmanage.flags();
    }
}
