package com.zenmanage.sdk.cache;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

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

    @Test
    void clearClosesTheDirectoryStream() throws IOException {
        // Regression test for a file-descriptor leak: clear() previously called
        // Files.list() without try-with-resources, so the returned stream (and the
        // directory handle backing it) was never closed. Stream.onClose() lets us
        // observe closure without depending on JDK-internal stream implementation
        // details.
        Path dir = Files.createTempDirectory("zm-cache");
        FileSystemCache cache = new FileSystemCache(dir);

        AtomicBoolean streamClosed = new AtomicBoolean(false);
        Stream<Path> trackedStream = Stream.<Path>of(dir.resolve("nonexistent.json")).onClose(() -> streamClosed.set(true));

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class, CALLS_REAL_METHODS)) {
            mockedFiles.when(() -> Files.list(dir)).thenReturn(trackedStream);
            cache.clear();
        }

        assertTrue(streamClosed.get(), "Files.list() stream should be closed after clear()");
    }
}
