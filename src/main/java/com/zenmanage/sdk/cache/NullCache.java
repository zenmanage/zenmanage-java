package com.zenmanage.sdk.cache;

import java.util.Optional;

/**
 * No-op cache backend.
 */
public final class NullCache implements Cache {
    @Override
    public Optional<String> get(String key) {
        return Optional.empty();
    }

    @Override
    public void set(String key, String value, long ttlSeconds) {
        // no-op
    }

    @Override
    public boolean has(String key) {
        return false;
    }

    @Override
    public void delete(String key) {
        // no-op
    }

    @Override
    public void clear() {
        // no-op
    }
}
