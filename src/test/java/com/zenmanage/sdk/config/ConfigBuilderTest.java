package com.zenmanage.sdk.config;

import com.zenmanage.sdk.errors.ConfigurationException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigBuilderTest {
    @Test
    void buildRequiresToken() {
        assertThrows(ConfigurationException.class, () -> ConfigBuilder.create().build());
    }

    @Test
    void buildRejectsClientToken() {
        ConfigurationException ex = assertThrows(
            ConfigurationException.class,
            () -> ConfigBuilder.create().withEnvironmentToken("cli_abc").build()
        );
        assertTrue(ex.getMessage().contains("server key"));
    }

    @Test
    void fromEnvironmentMapsValues() {
        Config config = ConfigBuilder.fromEnvironment(Map.of(
            "ZENMANAGE_ENVIRONMENT_TOKEN", "srv_env",
            "ZENMANAGE_CACHE_TTL", "123",
            "ZENMANAGE_CACHE_BACKEND", "null",
            "ZENMANAGE_ENABLE_USAGE_REPORTING", "false",
            "ZENMANAGE_API_ENDPOINT", "https://example.com"
        )).build();

        assertEquals("srv_env", config.getEnvironmentToken());
        assertEquals(123, config.getCacheTtlSeconds());
        assertEquals(CacheBackend.NULL, config.getCacheBackend());
        assertEquals("https://example.com", config.getApiEndpoint());
    }
}
