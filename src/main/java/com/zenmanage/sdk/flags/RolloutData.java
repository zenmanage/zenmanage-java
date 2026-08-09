package com.zenmanage.sdk.flags;

import java.util.List;

/**
 * Percentage rollout payload.
 */
public final class RolloutData {
    private FlagTarget target;
    private List<Rule> rules;
    private double percentage;
    private String salt;
    private String status;

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

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
