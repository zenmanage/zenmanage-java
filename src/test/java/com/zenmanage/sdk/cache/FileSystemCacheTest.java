package com.zenmanage.sdk.cache;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSystemCacheTest {
    @Test
    void writesAndReadsValue() throws IOException {
        Path dir = Files.createTempDirectory("zm-cache");
        FileSystemCache cache = new FileSystemCache(dir);

        cache.set("key", "value", 20);
        assertTrue(cache.get("key").isPresent());
        assertEquals("value", cache.get("key").orElseThrow());

        cache.clear();
        assertFalse(cache.has("key"));
    }
}
