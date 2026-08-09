package com.zenmanage.sdk.context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluation context used for targeted rules.
 */
public final class Context {
    private final String type;
    private final String name;
    private final String identifier;
    private final Map<String, Attribute> attributes;

    public Context(String type) {
        this(type, null, null, List.of());
    }

    public Context(String type, String name, String identifier, List<Attribute> attributes) {
        this.type = type;
        this.name = name;
        this.identifier = identifier;
        this.attributes = new LinkedHashMap<>();
        attributes.forEach(this::addAttribute);
    }

    public static Context single(String type, String identifier) {
        return new Context(type, null, identifier, List.of());
    }

    public static Context single(String type, String identifier, String name) {
        return new Context(type, name, identifier, List.of());
    }

    @SuppressWarnings("unchecked")
    public static Context fromMap(Map<String, Object> data) {
        String type = (String) data.get("type");
        String name = (String) data.get("name");
        String identifier = (String) data.get("identifier");

        List<Attribute> attrs = new ArrayList<>();
        Object rawAttrs = data.get("attributes");
        if (rawAttrs instanceof List) {
            for (Object raw : (List<Object>) rawAttrs) {
                if (!(raw instanceof Map)) {
                    continue;
                }

                Map<String, Object> attrMap = (Map<String, Object>) raw;
                String key = (String) attrMap.get("key");
                Attribute attribute = new Attribute(key);

                Object rawValues = attrMap.get("values");
                if (rawValues instanceof List) {
                    for (Object valueObject : (List<Object>) rawValues) {
                        if (valueObject instanceof Map) {
                            Object value = ((Map<String, Object>) valueObject).get("value");
                            if (value != null) {
                                attribute.addValue(String.valueOf(value));
                            }
                        }
                    }
                }

                attrs.add(attribute);
            }
        }

        return new Context(type, name, identifier, attrs);
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Context addAttribute(Attribute attribute) {
        attributes.put(attribute.getKey(), attribute);
        return this;
    }

    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    public Attribute getAttribute(String key) {
        return attributes.get(key);
    }

    public List<Attribute> getAttributes() {
        return new ArrayList<>(attributes.values());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", type);

        if (name != null) {
            result.put("name", name);
        }

        if (identifier != null) {
            result.put("identifier", identifier);
        }

        if (!attributes.isEmpty()) {
            List<Map<String, Object>> attributeMaps = new ArrayList<>();
            for (Attribute attribute : attributes.values()) {
                attributeMaps.add(attribute.toMap());
            }
            result.put("attributes", attributeMaps);
        }

        return result;
    }
}
