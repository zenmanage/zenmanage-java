package com.zenmanage.sdk.flags;

import com.zenmanage.sdk.api.ApiClient;
import com.zenmanage.sdk.cache.InMemoryCache;
import com.zenmanage.sdk.config.Logger;
import com.zenmanage.sdk.context.Attribute;
import com.zenmanage.sdk.context.Context;
import com.zenmanage.sdk.errors.EvaluationException;
import com.zenmanage.sdk.rules.RuleEngine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlagManagerTest {
    @Test
    void returnsInlineDefaultWhenFlagMissing() {
        FlagManager manager = new FlagManager(new StubApiClient(emptyRules()), new InMemoryCache(), new RuleEngine(), 3600, new TestLogger());

        Flag flag = manager.single("missing", true);
        assertEquals(true, flag.getValue());
    }

    @Test
    void throwsWhenFlagMissingAndNoDefault() {
        FlagManager manager = new FlagManager(new StubApiClient(emptyRules()), new InMemoryCache(), new RuleEngine(), 3600, new TestLogger());
        assertThrows(EvaluationException.class, () -> manager.single("missing"));
    }

    @Test
    void evaluatesRuleAgainstContext() {
        FlagData data = baseBooleanFlagData("premium-feature", false);

        RuleCondition condition = new RuleCondition();
        condition.setAttribute("country");
        condition.setOperator("equal");
        condition.setValue("US");

        RawFlagValue ruleRaw = new RawFlagValue();
        ruleRaw.setBooleanValue(true);
        RuleValue ruleValue = new RuleValue();
        ruleValue.setValue(ruleRaw);

        Rule rule = new Rule();
        rule.setClauses(List.of(condition));
        rule.setValue(ruleValue);

        data.setRules(List.of(rule));

        RulesResponse response = new RulesResponse();
        response.setVersion("1");
        response.setFlags(List.of(data));

        FlagManager manager = new FlagManager(new StubApiClient(response), new InMemoryCache(), new RuleEngine(), 3600, new TestLogger());

        Context context = Context.single("user", "u-1").addAttribute(new Attribute("country", List.of("US")));
        Flag flag = manager.withContext(context).single("premium-feature", false);

        assertEquals(true, flag.getValue());
    }

    @Test
    void reportUsageDoesNotThrow() {
        FlagData data = baseBooleanFlagData("feature", true);
        RulesResponse response = new RulesResponse();
        response.setVersion("1");
        response.setFlags(List.of(data));

        FlagManager manager = new FlagManager(new StubApiClient(response), new InMemoryCache(), new RuleEngine(), 3600, new TestLogger());
        assertDoesNotThrow(() -> manager.reportUsage("feature", Context.single("user", "u")));
    }

    @Test
    void reportsUsageWithInlineDefaultValueWhenFlagMissing() {
        StubApiClient stub = new StubApiClient(emptyRules());
        FlagManager manager = new FlagManager(stub, new InMemoryCache(), new RuleEngine(), 3600, new TestLogger());

        manager.single("missing", true);

        assertEquals("missing", stub.lastReportedKey);
        assertEquals(true, stub.lastDefaultValue);
    }

    @Test
    void reportsUsageWithDefaultsCollectionValueWhenFlagMissing() {
        StubApiClient stub = new StubApiClient(emptyRules());
        FlagManager manager = new FlagManager(stub, new InMemoryCache(), new RuleEngine(), 3600, new TestLogger())
            .withDefaults(new DefaultsCollection().set("missing", "fallback"));

        manager.single("missing");

        assertEquals("missing", stub.lastReportedKey);
        assertEquals("fallback", stub.lastDefaultValue);
    }

    @Test
    void reportsUsageWithoutDefaultValueWhenFlagFound() {
        FlagData data = baseBooleanFlagData("feature", true);
        RulesResponse response = new RulesResponse();
        response.setVersion("1");
        response.setFlags(List.of(data));

        StubApiClient stub = new StubApiClient(response);
        FlagManager manager = new FlagManager(stub, new InMemoryCache(), new RuleEngine(), 3600, new TestLogger());

        manager.single("feature");

        assertEquals("feature", stub.lastReportedKey);
        assertNull(stub.lastDefaultValue);
    }

    private static RulesResponse emptyRules() {
        RulesResponse response = new RulesResponse();
        response.setVersion("1");
        response.setFlags(List.of());
        return response;
    }

    private static FlagData baseBooleanFlagData(String key, boolean value) {
        RawFlagValue raw = new RawFlagValue();
        raw.setBooleanValue(value);

        TargetValue targetValue = new TargetValue();
        targetValue.setValue(raw);

        FlagTarget target = new FlagTarget();
        target.setValue(targetValue);

        FlagData data = new FlagData();
        data.setVersion("1");
        data.setType(FlagType.BOOLEAN);
        data.setKey(key);
        data.setName(key);
        data.setTarget(target);
        data.setRules(List.of());
        return data;
    }

    private static final class StubApiClient extends ApiClient {
        private final RulesResponse response;
        private String lastReportedKey;
        private Object lastDefaultValue;

        private StubApiClient(RulesResponse response) {
            super("srv_test", "https://example.com", new TestLogger(), false, "0.1.0", "zenmanage-java");
            this.response = response;
        }

        @Override
        public RulesResponse getRules() {
            return response;
        }

        @Override
        public void reportUsage(String key, Context context, Object defaultValue) {
            lastReportedKey = key;
            lastDefaultValue = defaultValue;
        }
    }

    private static final class TestLogger implements Logger {
        @Override
        public void debug(String message) {
        }

        @Override
        public void info(String message) {
        }

        @Override
        public void warn(String message) {
        }

        @Override
        public void error(String message, Throwable throwable) {
        }
    }
}
