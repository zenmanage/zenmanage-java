package com.zenmanage.sdk.flags;

import java.util.List;

/**
 * Rule definition used for targeted evaluation.
 */
public final class Rule {
    private String version;
    private String description;
    private RuleCondition criteria;
    private List<RuleCondition> clauses;
    private Integer position;
    private RuleValue value;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RuleCondition getCriteria() {
        return criteria;
    }

    public void setCriteria(RuleCondition criteria) {
        this.criteria = criteria;
    }

    public List<RuleCondition> getClauses() {
        return clauses;
    }

    public void setClauses(List<RuleCondition> clauses) {
        this.clauses = clauses;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public RuleValue getValue() {
        return value;
    }

    public void setValue(RuleValue value) {
        this.value = value;
    }
}
