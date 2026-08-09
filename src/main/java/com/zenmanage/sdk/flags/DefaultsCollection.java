package com.zenmanage.sdk.flags;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Collection of fallback values used when flags are missing.
 */
public final class DefaultsCollection {
    private final Map<String, Object> defaults = new LinkedHashMap<>();

    public static DefaultsCollection fromMap(Map<String, Object> values) {
        DefaultsCollection collection = new DefaultsCollection();
        values.forEach(collection::set);
        return collection;
    }

    public DefaultsCollection set(String key, Object value) {
        defaults.put(key, value);
        return this;
    }

    public boolean has(String key) {
        return defaults.containsKey(key);
    }

    public Object get(String key) {
        return defaults.get(key);
    }

    public boolean delete(String key) {
        return defaults.remove(key) != null;
    }

    public void clear() {
        defaults.clear();
    }

    public int size() {
        return defaults.size();
    }

    public List<String> keys() {
        return new ArrayList<>(defaults.keySet());
    }
}
