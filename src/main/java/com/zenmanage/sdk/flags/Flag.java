package com.zenmanage.sdk.flags;

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

    public boolean asBool() {
        Object value = getValue();
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0.0;
        }

        return Boolean.parseBoolean(String.valueOf(value));
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

    public Object getValue() {
        if (target == null || target.getValue() == null || target.getValue().getValue() == null) {
            return "";
        }

        return target.getValue().getValue().toJavaValue();
    }
}
