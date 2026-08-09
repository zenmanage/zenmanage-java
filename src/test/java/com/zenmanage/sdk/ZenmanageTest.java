package com.zenmanage.sdk;

import com.zenmanage.sdk.cache.Cache;
import com.zenmanage.sdk.config.CacheBackend;
import com.zenmanage.sdk.config.ConfigBuilder;
import com.zenmanage.sdk.config.Logger;
import com.zenmanage.sdk.errors.ConfigurationException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZenmanageTest {
    @TempDir
    Path tempDir;

    @Test
    void createsSdkWithMemoryCache() {
        Zenmanage sdk = new Zenmanage(baseConfigBuilder().withCacheBackend("memory").build());

        assertNotNull(sdk.flags());
    }

    @Test
    void createsSdkWithNullCache() {
        Zenmanage sdk = new Zenmanage(baseConfigBuilder().withCacheBackend("null").build());

        assertNotNull(sdk.flags());
    }

    @Test
    void createsSdkWithFilesystemCacheWhenDirectoryIsConfigured() {
        Zenmanage sdk = new Zenmanage(
            baseConfigBuilder()
                .withCacheBackend("filesystem")
                .withCacheDirectory(tempDir.resolve("cache").toString())
                .build()
        );

        assertNotNull(sdk.flags());
    }

    @Test
    void prefersCustomCacheWhenProvided() {
        Zenmanage sdk = new Zenmanage(baseConfigBuilder().withCache(new NoOpCache()).build());

        assertNotNull(sdk.flags());
    }

    @Test
    void rejectsFilesystemCacheWithoutDirectory() {
        assertThrows(ConfigurationException.class, () -> baseConfigBuilder()
            .withCacheBackend("filesystem")
            .withCacheDirectory("   ")
            .build());
    }

    private static ConfigBuilder baseConfigBuilder() {
        return ConfigBuilder.create()
            .withEnvironmentToken("srv_test")
            .withLogger(new TestLogger())
            .withSdkVersion("0.1.0")
            .withClientAgent("zenmanage-java-test");
    }

    private static final class NoOpCache implements Cache {
        @Override
        public Optional<String> get(String key) {
            return Optional.empty();
        }

        @Override
        public void set(String key, String value, long ttlSeconds) {
        }

        @Override
        public boolean has(String key) {
            return false;
        }

        @Override
        public void delete(String key) {
        }

        @Override
        public void clear() {
        }
    }

    private static final class TestLogger implements Logger {
        @Override
        public void debug(String message) {
        }

        @Override
        public void info(String message) {
        }

        @Override
        public void warn(String message) {
        }

        @Override
        public void error(String message, Throwable throwable) {
        }
    }
}