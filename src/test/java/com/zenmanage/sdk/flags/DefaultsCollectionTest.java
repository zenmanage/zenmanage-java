package com.zenmanage.sdk.flags;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultsCollectionTest {
    @Test
    void supportsFullCollectionLifecycle() {
        DefaultsCollection defaults = DefaultsCollection.fromMap(Map.of(
            "feature-a", true,
            "feature-b", "control"
        ));

        defaults.set("feature-c", 10);

        assertEquals(3, defaults.size());
        assertTrue(defaults.has("feature-a"));
        assertEquals("control", defaults.get("feature-b"));
        assertIterableEquals(defaults.keys(), defaults.keys());
        assertTrue(defaults.delete("feature-c"));
        assertFalse(defaults.delete("missing"));

        defaults.clear();

        assertEquals(0, defaults.size());
        assertFalse(defaults.has("feature-a"));
        assertNull(defaults.get("feature-a"));
    }
}