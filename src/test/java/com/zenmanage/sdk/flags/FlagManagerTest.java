package com.zenmanage.sdk.flags;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zenmanage.sdk.api.ApiClient;
import com.zenmanage.sdk.cache.InMemoryCache;
import com.zenmanage.sdk.config.Logger;
import com.zenmanage.sdk.context.Attribute;
import com.zenmanage.sdk.context.Context;
import com.zenmanage.sdk.errors.EvaluationException;
import com.zenmanage.sdk.rules.RuleEngine;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void reportsUsageWithInlineDefaultValueWhenFlagFound() {
        FlagData data = baseBooleanFlagData("feature", true);
        RulesResponse response = new RulesResponse();
        response.setVersion("1");
        response.setFlags(List.of(data));

        StubApiClient stub = new StubApiClient(response);
        FlagManager manager = new FlagManager(stub, new InMemoryCache(), new RuleEngine(), 3600, new TestLogger());

        manager.single("feature", false);

        assertEquals("feature", stub.lastReportedKey);
        assertEquals(false, stub.lastDefaultValue);
    }

    @Test
    void reportsUsageWithDefaultsCollectionValueWhenFlagFound() {
        FlagData data = baseBooleanFlagData("feature", true);
        RulesResponse response = new RulesResponse();
        response.setVersion("1");
        response.setFlags(List.of(data));

        StubApiClient stub = new StubApiClient(response);
        FlagManager manager = new FlagManager(stub, new InMemoryCache(), new RuleEngine(), 3600, new TestLogger())
            .withDefaults(new DefaultsCollection().set("feature", "fallback"));

        manager.single("feature");

        assertEquals("feature", stub.lastReportedKey);
        assertEquals("fallback", stub.lastDefaultValue);
    }

    @Test
    void unknownFlagTypeInWirePayloadDoesNotBreakOtherFlagsAndFallsBackToDefault() throws Exception {
        // Simulates a rules payload from the CDN that already includes a flag type this
        // SDK release does not know about (e.g. a type added on the platform after this
        // SDK shipped), mixed in with ordinary boolean/string/number/json flags. The whole
        // document must still parse, the known flags must still evaluate normally, and
        // looking up the unknown-typed flag must behave like a missing flag rather than
        // throwing.
        String json = "{"
            + "\"version\":\"1\","
            + "\"flags\":["
            + "  {"
            + "    \"version\":\"1\",\"type\":\"boolean\",\"key\":\"bool-flag\",\"name\":\"bool-flag\","
            + "    \"target\":{\"value\":{\"value\":{\"boolean\":true}}},\"rules\":[]"
            + "  },"
            + "  {"
            + "    \"version\":\"1\",\"type\":\"string\",\"key\":\"string-flag\",\"name\":\"string-flag\","
            + "    \"target\":{\"value\":{\"value\":{\"string\":\"control\"}}},\"rules\":[]"
            + "  },"
            + "  {"
            + "    \"version\":\"1\",\"type\":\"number\",\"key\":\"number-flag\",\"name\":\"number-flag\","
            + "    \"target\":{\"value\":{\"value\":{\"number\":42}}},\"rules\":[]"
            + "  },"
            + "  {"
            + "    \"version\":\"1\",\"type\":\"json\",\"key\":\"json-flag\",\"name\":\"json-flag\","
            + "    \"target\":{\"value\":{\"value\":{\"json\":{\"nested\":true}}}},\"rules\":[]"
            + "  },"
            + "  {"
            + "    \"version\":\"1\",\"type\":\"future-type\",\"key\":\"future-flag\",\"name\":\"future-flag\","
            + "    \"target\":{\"value\":{\"value\":{\"future-type\":\"whatever\"}}},\"rules\":[]"
            + "  }"
            + "]"
            + "}";

        RulesResponse response = assertDoesNotThrow(() -> parseRules(json));

        FlagManager manager = new FlagManager(new StubApiClient(response), new InMemoryCache(), new RuleEngine(), 3600, new TestLogger());

        assertEquals(true, manager.single("bool-flag", false).getValue());
        assertEquals("control", manager.single("string-flag", "fallback").getValue());
        assertEquals(42.0, manager.single("number-flag", 0).getValue());
        assertEquals(true, manager.single("json-flag", Map.of()).asJson().get("nested").asBoolean());

        Flag futureFlag = assertDoesNotThrow(() -> manager.single("future-flag", "caller-default"));
        assertEquals("caller-default", futureFlag.getValue());
    }

    @Test
    void unknownFlagTypeWithoutCallerDefaultThrowsLikeMissingFlag() throws Exception {
        String json = "{"
            + "\"version\":\"1\","
            + "\"flags\":["
            + "  {"
            + "    \"version\":\"1\",\"type\":\"future-type\",\"key\":\"future-flag\",\"name\":\"future-flag\","
            + "    \"target\":{\"value\":{\"value\":{\"future-type\":\"whatever\"}}},\"rules\":[]"
            + "  }"
            + "]"
            + "}";

        RulesResponse response = parseRules(json);
        FlagManager manager = new FlagManager(new StubApiClient(response), new InMemoryCache(), new RuleEngine(), 3600, new TestLogger());

        assertThrows(EvaluationException.class, () -> manager.single("future-flag"));
    }

    @Test
    void jsonFlagFromWirePayloadDecodesObjectAndArrayValues() throws Exception {
        String json = "{"
            + "\"version\":\"1\","
            + "\"flags\":["
            + "  {"
            + "    \"version\":\"1\",\"type\":\"json\",\"key\":\"json-object-flag\",\"name\":\"json-object-flag\","
            + "    \"target\":{\"value\":{\"value\":{\"json\":{\"mode\":\"dark\",\"limit\":5}}}},\"rules\":[]"
            + "  },"
            + "  {"
            + "    \"version\":\"1\",\"type\":\"json\",\"key\":\"json-array-flag\",\"name\":\"json-array-flag\","
            + "    \"target\":{\"value\":{\"value\":{\"json\":[1,2,3]}}},\"rules\":[]"
            + "  }"
            + "]"
            + "}";

        RulesResponse response = parseRules(json);
        FlagManager manager = new FlagManager(new StubApiClient(response), new InMemoryCache(), new RuleEngine(), 3600, new TestLogger());

        JsonNode objectValue = manager.single("json-object-flag").asJson();
        assertTrue(objectValue.isObject());
        assertEquals("dark", objectValue.get("mode").asText());
        assertEquals(5, objectValue.get("limit").asInt());

        JsonNode arrayValue = manager.single("json-array-flag").asJson();
        assertTrue(arrayValue.isArray());
        assertEquals(3, arrayValue.size());
        assertEquals(2, arrayValue.get(1).asInt());
    }

    @Test
    void mapDefaultValueIsTypedAsJsonNotStringified() {
        StubApiClient stub = new StubApiClient(emptyRules());
        FlagManager manager = new FlagManager(stub, new InMemoryCache(), new RuleEngine(), 3600, new TestLogger());

        Map<String, Object> defaultValue = Map.of("mode", "light", "accent", "#4f46e5");
        Flag flag = manager.single("theme-config", defaultValue);

        assertEquals(FlagType.JSON, flag.getType());
        assertEquals("light", flag.asJson().get("mode").asText());
        assertEquals("#4f46e5", flag.asJson().get("accent").asText());
        // The default must be typed json, not coerced into a string wrapper.
        assertEquals("", flag.asString());
    }

    @Test
    void listDefaultValueIsTypedAsJsonNotStringified() {
        StubApiClient stub = new StubApiClient(emptyRules());
        FlagManager manager = new FlagManager(stub, new InMemoryCache(), new RuleEngine(), 3600, new TestLogger());

        Flag flag = manager.single("rollout-plan", List.of("phase-1", "phase-2"));

        assertEquals(FlagType.JSON, flag.getType());
        JsonNode value = flag.asJson();
        assertTrue(value.isArray());
        assertEquals("phase-1", value.get(0).asText());
        assertEquals("phase-2", value.get(1).asText());
    }

    private static RulesResponse parseRules(String json) throws Exception {
        // Mirrors ApiClient's ObjectMapper configuration (FAIL_ON_UNKNOWN_PROPERTIES disabled)
        // so this test exercises the same deserialization path as a real CDN response.
        ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(json, RulesResponse.class);
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
