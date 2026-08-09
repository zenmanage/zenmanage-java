package com.zenmanage.sdk.cache;

import java.util.Optional;

/**
 * Generic cache contract used to store rules payloads.
 */
public interface Cache {
    Optional<String> get(String key);

    void set(String key, String value, long ttlSeconds);

    boolean has(String key);

    void delete(String key);

    void clear();
}
