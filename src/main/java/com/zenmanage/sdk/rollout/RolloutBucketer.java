package com.zenmanage.sdk.rollout;

import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

/**
 * Deterministic bucket assignment for percentage rollouts.
 */
public final class RolloutBucketer {
    private RolloutBucketer() {
    }

    public static long crc32b(String input) {
        CRC32 crc32 = new CRC32();
        crc32.update(input.getBytes(StandardCharsets.UTF_8));
        return crc32.getValue();
    }

    public static boolean isInBucket(String salt, String contextIdentifier, double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }

        if (contextIdentifier == null) {
            return false;
        }

        long hash = crc32b(salt + ":" + contextIdentifier);
        long bucket = hash % 100;
        return bucket < percentage;
    }
}
