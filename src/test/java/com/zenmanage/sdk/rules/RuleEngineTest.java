package com.zenmanage.sdk.rules;

import com.zenmanage.sdk.context.Attribute;
import com.zenmanage.sdk.context.Context;
import com.zenmanage.sdk.flags.Rule;
import com.zenmanage.sdk.flags.RuleCondition;
import com.zenmanage.sdk.flags.RuleValue;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleEngineTest {
    private final RuleEngine engine = new RuleEngine();

    @Test
    void evaluatesEqualsClause() {
        Rule matched = engine.evaluate(List.of(rule("country", "equal", "US")), contextWith("country", "US"));
        assertNotNull(matched);
    }

    @Test
    void returnsNullWhenNoRuleMatches() {
        Rule matched = engine.evaluate(List.of(rule("country", "equal", "CA")), contextWith("country", "US"));
        assertNull(matched);
    }

    @Test
    void evaluatesCriteriaWhenClausesAreAbsent() {
        Rule rule = new Rule();
        rule.setCriteria(condition("email", "contains", "@example.com"));
        rule.setValue(new RuleValue());

        Rule matched = engine.evaluate(List.of(rule), contextWith("email", "owner@example.com"));

        assertNotNull(matched);
    }

    @Test
    void matchesRuleWithoutCriteriaOrClauses() {
        Rule rule = new Rule();
        rule.setValue(new RuleValue());

        Rule matched = engine.evaluate(List.of(rule), Context.single("user", "u-1"));

        assertNotNull(matched);
    }

    @Test
    void supportsContextClausesWithStringListAndMapInputs() {
        Context context = Context.single("user", "u-1");

        Rule stringRule = rule("context", "equal", "u-1");
        Rule listRule = rule("context", "in", List.of("u-0", "u-1"));
        Rule mapRule = rule("segment", "equal", Map.of("identifier", "u-1", "type", "user"));

        assertNotNull(engine.evaluate(List.of(stringRule), context));
        assertNotNull(engine.evaluate(List.of(listRule), context));
        assertNotNull(engine.evaluate(List.of(mapRule), context));
    }

    @Test
    void ignoresContextMapEntriesWithWrongType() {
        Context context = Context.single("user", "u-1");
        Rule rule = rule(
            "context",
            "equal",
            List.of(Map.of("identifier", "u-1", "type", "account"))
        );

        Rule matched = engine.evaluate(List.of(rule), context);

        assertNull(matched);
    }

    @Test
    void returnsNullForContextClauseWhenIdentifierIsBlank() {
        Rule matched = engine.evaluate(
            List.of(rule("context", "equal", "u-1")),
            new Context("user", null, "   ", List.of())
        );

        assertNull(matched);
    }

    @Test
    void supportsNegativeStringOperators() {
        Context context = contextWith("email", "owner@example.com");

        assertNotNull(engine.evaluate(List.of(rule("email", "notequal", "other@example.com")), context));
        assertNotNull(engine.evaluate(List.of(rule("email", "notcontains", "@internal")), context));
        assertNotNull(engine.evaluate(List.of(rule("email", "notin", List.of("admin@example.com"))), context));
    }

    @Test
    void supportsCollectionAndPrefixOperators() {
        Context context = Context.single("user", "u-1")
            .addAttribute(new Attribute("country", List.of("US", "CA")))
            .addAttribute(new Attribute("email", List.of("owner@example.com")));

        assertNotNull(engine.evaluate(List.of(rule("country", "in", List.of("CA", "GB"))), context));
        assertNotNull(engine.evaluate(List.of(rule("email", "startswith", "owner")), context));
        assertNotNull(engine.evaluate(List.of(rule("email", "notstartswith", "admin")), context));
        assertNull(engine.evaluate(List.of(rule("email", "notstartswith", "owner")), context));
        assertNotNull(engine.evaluate(List.of(rule("email", "endswith", ".com")), context));
        assertNotNull(engine.evaluate(List.of(rule("email", "notendswith", ".org")), context));
        assertNull(engine.evaluate(List.of(rule("email", "notendswith", ".com")), context));
    }

    @Test
    void supportsNumericComparisons() {
        Context context = contextWith("plan_score", "42");

        assertNotNull(engine.evaluate(List.of(rule("plan_score", "gt", "10")), context));
        assertNotNull(engine.evaluate(List.of(rule("plan_score", "gte", "42")), context));
        assertNotNull(engine.evaluate(List.of(rule("plan_score", "lt", "100")), context));
        assertNotNull(engine.evaluate(List.of(rule("plan_score", "lte", "42")), context));
    }

    @Test
    void rejectsInvalidOperatorsAndValues() {
        Context context = contextWith("plan_score", "forty-two");

        assertNull(engine.evaluate(List.of(rule("plan_score", "gt", "10")), context));
        assertNull(engine.evaluate(List.of(rule("plan_score", "unknown", "10")), context));
        assertNull(engine.evaluate(List.of(rule("missing", "equal", "value")), context));
    }

    @Test
    void requiresAllClausesToMatch() {
        Rule rule = new Rule();
        rule.setClauses(List.of(
            condition("country", "equal", "US"),
            condition("email", "contains", "@example.com")
        ));
        rule.setValue(new RuleValue());

        Rule matched = engine.evaluate(
            List.of(rule),
            Context.single("user", "u-1")
                .addAttribute(new Attribute("country", List.of("US")))
                .addAttribute(new Attribute("email", List.of("owner@company.com")))
        );

        assertNull(matched);
    }

    @Test
    void stopsAtTheFirstMatchingRule() {
        Rule first = rule("country", "equal", "US");
        Rule second = rule("country", "equal", "US");

        Rule matched = engine.evaluate(List.of(first, second), contextWith("country", "US"));

        assertEquals(first, matched);
    }

    @Test
    void helperConversionsBehaveAsExpectedThroughEvaluation() {
        Context context = contextWith("country", "US");

        assertNull(engine.evaluate(List.of(rule("country", "equal", null)), context));
        assertNull(engine.evaluate(List.of(rule("country", "equal", List.of(1, 2, 3))), context));
        assertNotNull(engine.evaluate(List.of(rule("country", "in", List.of("CA", "US"))), context));
        assertFalse(context.getAttributes().isEmpty());
        assertTrue(context.toMap().containsKey("attributes"));
    }

    @Test
    void isNullDoesNotMatchWhenAttributeIsAbsent() {
        // Matches the reference SDK: an attribute missing from the context entirely never
        // matches any operator, including isnull — isnull only matches a *present*
        // attribute whose values are empty strings (see isNullMatchesWhenAttributeValueIsEmpty).
        assertNull(engine.evaluate(List.of(rule("tag", "isnull", null)), Context.single("user", "u-1")));
    }

    @Test
    void isNullMatchesWhenAttributeValueIsEmpty() {
        assertNotNull(engine.evaluate(List.of(rule("tag", "isnull", null)), contextWith("tag", "")));
    }

    @Test
    void isNullDoesNotMatchWhenAttributeHasValue() {
        assertNull(engine.evaluate(List.of(rule("tag", "isnull", null)), contextWith("tag", "active")));
    }

    @Test
    void notNullMatchesWhenAttributeHasValue() {
        assertNotNull(engine.evaluate(List.of(rule("tag", "notnull", null)), contextWith("tag", "active")));
    }

    @Test
    void notNullDoesNotMatchWhenAttributeIsAbsent() {
        assertNull(engine.evaluate(List.of(rule("tag", "notnull", null)), Context.single("user", "u-1")));
    }

    @Test
    void negativeOperatorsDoNotMatchWhenAttributeIsAbsent() {
        // Matches the reference SDK (zenmanage-php's AttributeConditionEvaluator): an
        // attribute the context doesn't carry at all never matches any operator, negated
        // or not — "notcontains" etc. only evaluate once the attribute is present.
        Context context = Context.single("user", "u-1");
        for (String op : new String[]{"notequal", "notin", "notcontains", "notstartswith", "notendswith"}) {
            assertNull(engine.evaluate(List.of(rule("missing", op, "x")), context),
                op + " should not match when attribute is absent");
        }
    }

    private static Context contextWith(String attributeName, String value) {
        return Context.single("user", "u-1").addAttribute(new Attribute(attributeName, List.of(value)));
    }

    private static Rule rule(String attribute, String operator, Object value) {
        Rule rule = new Rule();
        rule.setClauses(List.of(condition(attribute, operator, value)));
        rule.setValue(new RuleValue());
        return rule;
    }

    private static RuleCondition condition(String attribute, String operator, Object value) {
        RuleCondition clause = new RuleCondition();
        clause.setAttribute(attribute);
        clause.setOperator(operator);
        clause.setValue(value);
        return clause;
    }
}
