package com.zenmanage.sdk.flags;

import java.util.List;

/**
 * Raw flag payload from API.
 */
public final class FlagData {
    private String version;
    private FlagType type;
    private String key;
    private String name;
    private FlagTarget target;
    private List<Rule> rules;
    private RolloutData rollout;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public FlagType getType() {
        return type;
    }

    public void setType(FlagType type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FlagTarget getTarget() {
        return target;
    }

    public void setTarget(FlagTarget target) {
        this.target = target;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public RolloutData getRollout() {
        return rollout;
    }

    public void setRollout(RolloutData rollout) {
        this.rollout = rollout;
    }
}
