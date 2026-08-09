package com.zenmanage.sdk.rules;

import com.zenmanage.sdk.context.Attribute;
import com.zenmanage.sdk.context.Context;
import com.zenmanage.sdk.flags.Rule;
import com.zenmanage.sdk.flags.RuleCondition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Evaluates targeting rules against a context.
 */
public final class RuleEngine {
    public Rule evaluate(List<Rule> rules, Context context) {
        for (Rule rule : rules) {
            if (evaluateRule(rule, context)) {
                return rule;
            }
        }
        return null;
    }

    private boolean evaluateRule(Rule rule, Context context) {
        if (rule.getClauses() != null && !rule.getClauses().isEmpty()) {
            for (RuleCondition clause : rule.getClauses()) {
                if (!evaluateClause(clause, context)) {
                    return false;
                }
            }
            return true;
        }

        if (rule.getCriteria() != null) {
            return evaluateClause(rule.getCriteria(), context);
        }

        return true;
    }

    private boolean evaluateClause(RuleCondition clause, Context context) {
        if ("context".equals(clause.getAttribute()) || "segment".equals(clause.getAttribute())) {
            return evaluateContextClause(clause, context);
        }

        Attribute attribute = context.getAttribute(clause.getAttribute());
        if (attribute == null) {
            String op = clause.getOperator();
            return "isnull".equals(op) || "notequal".equals(op) || "notin".equals(op)
                || "notcontains".equals(op) || "notstartswith".equals(op) || "notendswith".equals(op);
        }

        List<String> attributeValues = attribute.getValues();
        String operator = clause.getOperator();
        if ("isnull".equals(operator)) {
            return attributeValues.stream().allMatch(String::isEmpty);
        }
        if ("notnull".equals(operator)) {
            return attributeValues.stream().anyMatch(v -> !v.isEmpty());
        }
        List<String> clauseValues = toStringList(clause.getValue());
        return evaluateByOperator(operator, attributeValues, clauseValues);
    }

    @SuppressWarnings("unchecked")
    private boolean evaluateContextClause(RuleCondition clause, Context context) {
        String identifier = context.getIdentifier();
        if (identifier == null || identifier.isBlank()) {
            return false;
        }

        List<String> matchingIdentifiers = new ArrayList<>();
        Object value = clause.getValue();

        if (value instanceof String) {
            matchingIdentifiers.add((String) value);
        } else if (value instanceof List) {
            for (Object item : (List<Object>) value) {
                if (item instanceof String) {
                    matchingIdentifiers.add((String) item);
                } else if (item instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) item;
                    Object targetId = map.get("identifier");
                    Object targetType = map.get("type");

                    if (targetId != null) {
                        if (targetType == null || String.valueOf(targetType).equals(context.getType())) {
                            matchingIdentifiers.add(String.valueOf(targetId));
                        }
                    }
                }
            }
        } else if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            Object targetId = map.get("identifier");
            Object targetType = map.get("type");
            if (targetId != null && (targetType == null || String.valueOf(targetType).equals(context.getType()))) {
                matchingIdentifiers.add(String.valueOf(targetId));
            }
        }

        if (matchingIdentifiers.isEmpty()) {
            return false;
        }

        return evaluateByOperator(clause.getOperator(), List.of(identifier), matchingIdentifiers);
    }

    private boolean evaluateByOperator(String operator, List<String> values, List<String> clauseValues) {
        switch (operator) {
            case "equal":
                return evaluateEquals(values, clauseValues);
            case "notequal":
                return !evaluateEquals(values, clauseValues);
            case "contains":
                return evaluateContains(values, clauseValues);
            case "notcontains":
                return !evaluateContains(values, clauseValues);
            case "in":
                return evaluateIn(values, clauseValues);
            case "notin":
                return !evaluateIn(values, clauseValues);
            case "startswith":
                return evaluateStartsWith(values, clauseValues);
            case "notstartswith":
                return !evaluateStartsWith(values, clauseValues);
            case "endswith":
                return evaluateEndsWith(values, clauseValues);
            case "notendswith":
                return !evaluateEndsWith(values, clauseValues);
            case "gt":
                return evaluateGreaterThan(values, clauseValues);
            case "gte":
                return evaluateGreaterThanOrEqual(values, clauseValues);
            case "lt":
                return evaluateLessThan(values, clauseValues);
            case "lte":
                return evaluateLessThanOrEqual(values, clauseValues);
            default:
                return false;
        }
    }

    private List<String> toStringList(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }

        if (value instanceof String) {
            return List.of((String) value);
        }

        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) value) {
                if (item instanceof String) {
                    result.add((String) item);
                }
            }
            return result;
        }

        return Collections.emptyList();
    }

    private boolean evaluateEquals(List<String> values, List<String> targets) {
        if (targets.isEmpty()) {
            return false;
        }

        String target = targets.get(0);
        for (String value : values) {
            if (value.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateContains(List<String> values, List<String> targets) {
        if (targets.isEmpty()) {
            return false;
        }

        String target = targets.get(0);
        for (String value : values) {
            if (value.contains(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateIn(List<String> values, List<String> targets) {
        for (String value : values) {
            if (targets.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateStartsWith(List<String> values, List<String> targets) {
        if (targets.isEmpty()) {
            return false;
        }

        String target = targets.get(0);
        for (String value : values) {
            if (value.startsWith(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateEndsWith(List<String> values, List<String> targets) {
        if (targets.isEmpty()) {
            return false;
        }

        String target = targets.get(0);
        for (String value : values) {
            if (value.endsWith(target)) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateGreaterThan(List<String> values, List<String> targets) {
        Double target = parseDouble(targets);
        if (target == null) {
            return false;
        }

        for (String value : values) {
            Double parsed = safeParseDouble(value);
            if (parsed != null && parsed > target) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateGreaterThanOrEqual(List<String> values, List<String> targets) {
        Double target = parseDouble(targets);
        if (target == null) {
            return false;
        }

        for (String value : values) {
            Double parsed = safeParseDouble(value);
            if (parsed != null && parsed >= target) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateLessThan(List<String> values, List<String> targets) {
        Double target = parseDouble(targets);
        if (target == null) {
            return false;
        }

        for (String value : values) {
            Double parsed = safeParseDouble(value);
            if (parsed != null && parsed < target) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateLessThanOrEqual(List<String> values, List<String> targets) {
        Double target = parseDouble(targets);
        if (target == null) {
            return false;
        }

        for (String value : values) {
            Double parsed = safeParseDouble(value);
            if (parsed != null && parsed <= target) {
                return true;
            }
        }
        return false;
    }

    private Double parseDouble(List<String> values) {
        if (values.isEmpty()) {
            return null;
        }
        return safeParseDouble(values.get(0));
    }

    private Double safeParseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
