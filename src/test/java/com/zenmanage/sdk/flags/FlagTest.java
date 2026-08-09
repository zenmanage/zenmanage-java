package com.zenmanage.sdk.flags;

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
}
