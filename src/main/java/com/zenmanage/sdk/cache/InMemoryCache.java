package com.zenmanage.sdk.cache;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-process cache backend.
 */
public final class InMemoryCache implements Cache {
    private final Map<String, CacheItem> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<String> get(String key) {
        CacheItem item = storage.get(key);
        if (item == null) {
            return Optional.empty();
        }

        if (item.expiresAtEpochMillis != null && item.expiresAtEpochMillis < Instant.now().toEpochMilli()) {
            storage.remove(key);
            return Optional.empty();
        }

        return Optional.of(item.value);
    }

    @Override
    public void set(String key, String value, long ttlSeconds) {
        Long expires = Instant.now().toEpochMilli() + (ttlSeconds * 1000L);
        storage.put(key, new CacheItem(value, expires));
    }

    @Override
    public boolean has(String key) {
        return get(key).isPresent();
    }

    @Override
    public void delete(String key) {
        storage.remove(key);
    }

    @Override
    public void clear() {
        storage.clear();
    }

    private static final class CacheItem {
        private final String value;
        private final Long expiresAtEpochMillis;

        private CacheItem(String value, Long expiresAtEpochMillis) {
            this.value = value;
            this.expiresAtEpochMillis = expiresAtEpochMillis;
        }
    }
}
