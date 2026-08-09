package com.zenmanage.sdk.spring;

import com.zenmanage.sdk.Zenmanage;
import com.zenmanage.sdk.cache.Cache;
import com.zenmanage.sdk.cache.InMemoryCache;
import com.zenmanage.sdk.config.Config;
import com.zenmanage.sdk.flags.FlagManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ZenmanageAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ZenmanageAutoConfiguration.class));

    @Test
    void createsZenmanageBeansWhenTokenProvided() {
        contextRunner
            .withPropertyValues(
                "zenmanage.environment-token=srv_test_token",
                "zenmanage.cache-ttl-seconds=120",
                "zenmanage.cache-backend=memory",
                "zenmanage.usage-reporting=false",
                "zenmanage.api-endpoint=https://example.com"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(Zenmanage.class);
                assertThat(context).hasSingleBean(Config.class);
                assertThat(context).hasSingleBean(FlagManager.class);

                Config config = context.getBean(Config.class);
                assertThat(config.getEnvironmentToken()).isEqualTo("srv_test_token");
                assertThat(config.getCacheTtlSeconds()).isEqualTo(120);
                assertThat(config.getApiEndpoint()).isEqualTo("https://example.com");
                assertThat(config.isUsageReportingEnabled()).isFalse();
            });
    }

    @Test
    void backsOffWithoutRequiredToken() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(Zenmanage.class);
            assertThat(context).doesNotHaveBean(Config.class);
        });
    }

    @Test
    void prefersUserProvidedCacheBean() {
        contextRunner
            .withBean(Cache.class, InMemoryCache::new)
            .withPropertyValues("zenmanage.environment-token=srv_test_token")
            .run(context -> {
                Config config = context.getBean(Config.class);
                assertThat(config.getCustomCache()).isSameAs(context.getBean(Cache.class));
            });
    }
}