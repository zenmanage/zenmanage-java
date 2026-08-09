package com.zenmanage.sdk.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryCacheTest {
    @Test
    void storesAndRetrievesValue() {
        InMemoryCache cache = new InMemoryCache();
        cache.set("k", "v", 10);

        assertTrue(cache.get("k").isPresent());
        assertTrue(cache.has("k"));
    }

    @Test
    void expiresValue() {
        InMemoryCache cache = new InMemoryCache();
        cache.set("k", "v", -1);

        assertFalse(cache.get("k").isPresent());
    }
}
