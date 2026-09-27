package com.zenmanage.sdk.flags;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlagTest {
    @Test
    void booleanConversionsWork() {
        RawFlagValue raw = new RawFlagValue();
        raw.setBooleanValue(true);

        TargetValue value = new TargetValue();
        value.setValue(raw);

        FlagTarget target = new FlagTarget();
        target.setValue(value);

        Flag flag = new Flag("1", FlagType.BOOLEAN, "flag", "flag", target, List.of(), null);

        assertTrue(flag.isEnabled());
        assertTrue(flag.asBool());
        // Cross-type coercion contract: asNumber()/asString() only recognize their own
        // value wrapper, so calling them on a boolean flag falls back to the safe
        // zero value (0 / "") rather than stringifying/parsing the boolean.
        assertEquals(0.0, flag.asNumber());
        assertEquals("", flag.asString());
    }

    @Test
    void nonBooleanIsNotEnabled() {
        RawFlagValue raw = new RawFlagValue();
        raw.setStringValue("on");

        TargetValue value = new TargetValue();
        value.setValue(raw);

        FlagTarget target = new FlagTarget();
        target.setValue(value);

        Flag flag = new Flag("1", FlagType.STRING, "flag", "flag", target, List.of(), null);

        assertFalse(flag.isEnabled());
    }

    @Test
    void asBoolReturnsTrueForJsonFlagRegardlessOfValue() throws Exception {
        // ZEN-1755: asBool() must return true for every non-boolean type, regardless
        // of the underlying value, per the cross-SDK coercion contract.
        JsonNode node = new ObjectMapper().readTree("{\"mode\":\"dark\"}");

        RawFlagValue raw = new RawFlagValue();
        raw.setJsonValue(node);

        TargetValue value = new TargetValue();
        value.setValue(raw);

        FlagTarget target = new FlagTarget();
        target.setValue(value);

        Flag flag = new Flag("1", FlagType.JSON, "flag", "flag", target, List.of(), null);

        assertTrue(flag.asBool());
    }

    @Test
    void asBoolReturnsTrueForNumberFlagEvenWhenValueIsZero() {
        RawFlagValue raw = new RawFlagValue();
        raw.setNumberValue(0.0);

        TargetValue value = new TargetValue();
        value.setValue(raw);

        FlagTarget target = new FlagTarget();
        target.setValue(value);

        Flag flag = new Flag("1", FlagType.NUMBER, "flag", "flag", target, List.of(), null);

        assertTrue(flag.asBool());
    }

    @Test
    void asBoolReturnsTrueForStringFlagEvenWhenValueIsEmptyOrFalse() {
        RawFlagValue emptyStringRaw = new RawFlagValue();
        emptyStringRaw.setStringValue("");
        TargetValue emptyStringValue = new TargetValue();
        emptyStringValue.setValue(emptyStringRaw);
        FlagTarget emptyStringTarget = new FlagTarget();
        emptyStringTarget.setValue(emptyStringValue);
        Flag emptyStringFlag = new Flag("1", FlagType.STRING, "flag", "flag", emptyStringTarget, List.of(), null);
        assertTrue(emptyStringFlag.asBool());

        RawFlagValue falseStringRaw = new RawFlagValue();
        falseStringRaw.setStringValue("false");
        TargetValue falseStringValue = new TargetValue();
        falseStringValue.setValue(falseStringRaw);
        FlagTarget falseStringTarget = new FlagTarget();
        falseStringTarget.setValue(falseStringValue);
        Flag falseStringFlag = new Flag("1", FlagType.STRING, "flag", "flag", falseStringTarget, List.of(), null);
        assertTrue(falseStringFlag.asBool());
    }

    @Test
    void asBoolReturnsFalseForBooleanFlagSetToFalse() {
        RawFlagValue raw = new RawFlagValue();
        raw.setBooleanValue(false);

        TargetValue value = new TargetValue();
        value.setValue(raw);

        FlagTarget target = new FlagTarget();
        target.setValue(value);

        Flag flag = new Flag("1", FlagType.BOOLEAN, "flag", "flag", target, List.of(), null);

        assertFalse(flag.asBool());
    }

    @Test
    void asJsonDecodesJsonObjectValue() throws Exception {
        JsonNode node = new ObjectMapper().readTree("{\"mode\":\"dark\",\"limit\":5}");

        RawFlagValue raw = new RawFlagValue();
        raw.setJsonValue(node);

        TargetValue value = new TargetValue();
        value.setValue(raw);

        FlagTarget target = new FlagTarget();
        target.setValue(value);

        Flag flag = new Flag("1", FlagType.JSON, "flag", "flag", target, List.of(), null);

        JsonNode result = flag.asJson();
        assertTrue(result.isObject());
        assertEquals("dark", result.get("mode").asText());
        assertEquals(5, result.get("limit").asInt());
    }

    @Test
    void asJsonDecodesJsonArrayValue() throws Exception {
        JsonNode node = new ObjectMapper().readTree("[1,2,3]");

        RawFlagValue raw = new RawFlagValue();
        raw.setJsonValue(node);

        TargetValue value = new TargetValue();
        value.setValue(raw);

        FlagTarget target = new FlagTarget();
        target.setValue(value);

        Flag flag = new Flag("1", FlagType.JSON, "flag", "flag", target, List.of(), null);

        JsonNode result = flag.asJson();
        assertTrue(result.isArray());
        assertEquals(3, result.size());
        assertEquals(2, result.get(1).asInt());
    }

    @Test
    void asJsonOnNonJsonFlagFallsBackToMissingNodeSafely() {
        RawFlagValue raw = new RawFlagValue();
        raw.setBooleanValue(true);

        TargetValue value = new TargetValue();
        value.setValue(raw);

        FlagTarget target = new FlagTarget();
        target.setValue(value);

        Flag booleanFlag = new Flag("1", FlagType.BOOLEAN, "flag", "flag", target, List.of(), null);

        JsonNode result = booleanFlag.asJson();
        assertTrue(result.isMissingNode());
        assertEquals(MissingNode.getInstance(), result);

        // Same "safe zero value" fallback for string and number flags.
        RawFlagValue stringRaw = new RawFlagValue();
        stringRaw.setStringValue("control");
        TargetValue stringValue = new TargetValue();
        stringValue.setValue(stringRaw);
        FlagTarget stringTarget = new FlagTarget();
        stringTarget.setValue(stringValue);
        Flag stringFlag = new Flag("1", FlagType.STRING, "flag", "flag", stringTarget, List.of(), null);
        assertTrue(stringFlag.asJson().isMissingNode());

        RawFlagValue numberRaw = new RawFlagValue();
        numberRaw.setNumberValue(42.0);
        TargetValue numberValue = new TargetValue();
        numberValue.setValue(numberRaw);
        FlagTarget numberTarget = new FlagTarget();
        numberTarget.setValue(numberValue);
        Flag numberFlag = new Flag("1", FlagType.NUMBER, "flag", "flag", numberTarget, List.of(), null);
        assertTrue(numberFlag.asJson().isMissingNode());
    }
}
