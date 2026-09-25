package com.zenmanage.sdk.flags;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import java.util.List;

/**
 * Runtime flag wrapper with type-safe accessors.
 */
public final class Flag {
    private final String version;
    private final FlagType type;
    private final String key;
    private final String name;
    private final FlagTarget target;
    private final List<Rule> rules;
    private final RolloutData rollout;

    public Flag(
        String version,
        FlagType type,
        String key,
        String name,
        FlagTarget target,
        List<Rule> rules,
        RolloutData rollout
    ) {
        this.version = version;
        this.type = type;
        this.key = key;
        this.name = name;
        this.target = target;
        this.rules = rules;
        this.rollout = rollout;
    }

    public static Flag fromData(FlagData data) {
        return new Flag(
            data.getVersion(),
            data.getType(),
            data.getKey(),
            data.getName(),
            data.getTarget(),
            data.getRules() == null ? List.of() : data.getRules(),
            data.getRollout()
        );
    }

    public String getVersion() {
        return version;
    }

    public FlagType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public FlagTarget getTarget() {
        return target;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public RolloutData getRollout() {
        return rollout;
    }

    public boolean isEnabled() {
        if (type != FlagType.BOOLEAN) {
            return false;
        }

        Object value = getValue();
        return (value instanceof Boolean) && (Boolean) value;
    }

    /**
     * Get the flag value as a boolean.
     *
     * <p>Per the cross-SDK coercion contract (mirroring the reference PHP SDK), this
     * returns the underlying value only for a {@code boolean}-typed flag. For every
     * other recognized wrapper ({@code string}/{@code number}/{@code json}) it returns
     * {@code true} unconditionally — regardless of the underlying value, including a
     * {@code number} flag set to {@code 0} or a {@code string} flag set to {@code ""} —
     * since a present non-boolean wrapper is always "truthy". It only returns
     * {@code false} for a non-boolean type when there is no value wrapper at all.</p>
     */
    public boolean asBool() {
        RawFlagValue raw = rawValue();
        if (raw == null) {
            return false;
        }

        if (raw.getBooleanValue() != null) {
            return raw.getBooleanValue();
        }

        return raw.getStringValue() != null || raw.getNumberValue() != null || raw.getJsonValue() != null;
    }

    public String asString() {
        Object value = getValue();
        return value == null ? "" : String.valueOf(value);
    }

    public double asNumber() {
        Object value = getValue();
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? 1.0 : 0.0;
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return 0.0;
        }
    }

    /**
     * Get the flag value as a decoded JSON node.
     *
     * <p>Only recognizes the {@code json} value wrapper — a boolean/string/number flag
     * (or a json flag whose wrapper is missing/malformed) safely falls back to
     * {@link MissingNode#getInstance()} rather than attempting a lossy conversion.
     * Both JSON objects and JSON arrays decode to their natural {@link JsonNode}
     * subtype ({@code ObjectNode}/{@code ArrayNode}), so both are handled uniformly.</p>
     */
    public JsonNode asJson() {
        RawFlagValue raw = rawValue();
        if (raw == null) {
            return MissingNode.getInstance();
        }

        JsonNode value = raw.getJsonValue();
        return value == null ? MissingNode.getInstance() : value;
    }

    public Object getValue() {
        RawFlagValue raw = rawValue();
        return raw == null ? "" : raw.toJavaValue();
    }

    /**
     * Return the wire-level value wrapper for this flag's target, or {@code null} if
     * the flag has no value at all (missing target/value chain).
     */
    private RawFlagValue rawValue() {
        if (target == null || target.getValue() == null || target.getValue().getValue() == null) {
            return null;
        }

        return target.getValue().getValue();
    }
}
