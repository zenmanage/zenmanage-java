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
        assertEquals(1.0, flag.asNumber());
        assertEquals("true", flag.asString());
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
