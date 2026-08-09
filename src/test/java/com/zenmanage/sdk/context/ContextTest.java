package com.zenmanage.sdk.context;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextTest {
    @Test
    void singleContextAndAttributesWork() {
        Context context = Context.single("user", "user-1", "Alice")
            .addAttribute(new Attribute("country", List.of("US")));

        assertEquals("user", context.getType());
        assertEquals("user-1", context.getIdentifier());
        assertTrue(context.hasAttribute("country"));
        assertEquals("US", context.getAttribute("country").getValues().get(0));
    }

    @Test
    void fromMapParsesStructure() {
        Context context = Context.fromMap(Map.of(
            "type", "user",
            "identifier", "u-2",
            "attributes", List.of(
                Map.of("key", "plan", "values", List.of(Map.of("value", "pro")))
            )
        ));

        assertEquals("u-2", context.getIdentifier());
        assertEquals("pro", context.getAttribute("plan").getValues().get(0));
    }
}
