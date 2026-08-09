package com.zenmanage.sdk.cache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Filesystem-backed cache backend.
 */
public final class FileSystemCache implements Cache {
    private final Path cacheDirectory;

    public FileSystemCache(String cacheDirectory) {
        this(Path.of(cacheDirectory));
    }

    public FileSystemCache(Path cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
        ensureDirectoryExists();
    }

    @Override
    public Optional<String> get(String key) {
        Path filePath = cacheFilePath(key);
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }

        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            int separator = content.indexOf('\n');
            if (separator < 0) {
                delete(key);
                return Optional.empty();
            }

            long expiresAtEpochMillis = Long.parseLong(content.substring(0, separator));
            String value = content.substring(separator + 1);

            if (expiresAtEpochMillis < Instant.now().toEpochMilli()) {
                delete(key);
                return Optional.empty();
            }

            return Optional.of(value);
        } catch (IOException | NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void set(String key, String value, long ttlSeconds) {
        Path filePath = cacheFilePath(key);
        long expiresAtEpochMillis = Instant.now().toEpochMilli() + (ttlSeconds * 1000L);

        try {
            Files.writeString(filePath, expiresAtEpochMillis + "\n" + value, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            // Best effort cache.
        }
    }

    @Override
    public boolean has(String key) {
        return get(key).isPresent();
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(cacheFilePath(key));
        } catch (IOException exception) {
            // Best effort cache.
        }
    }

    @Override
    public void clear() {
        if (!Files.exists(cacheDirectory)) {
            return;
        }

        try {
            Files.list(cacheDirectory)
                .filter(path -> path.getFileName().toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                        // Best effort cache.
                    }
                });
        } catch (IOException exception) {
            // Best effort cache.
        }
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(cacheDirectory);
        } catch (IOException ignored) {
            // Best effort cache.
        }
    }

    private Path cacheFilePath(String key) {
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(key.getBytes(StandardCharsets.UTF_8));
        return cacheDirectory.resolve(encoded + ".json");
    }
}
