package com.zenmanage.sdk.flags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Single condition/criterion in a rule.
 * Handles both legacy format (attribute/operator/value) and CDN wire format
 * (selector/selector_subtype/comparer/values).
 */
@JsonDeserialize(using = RuleCondition.Deserializer.class)
public final class RuleCondition {
    private String attribute;
    private String operator;
    private Object value;

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public static final class Deserializer extends StdDeserializer<RuleCondition> {
        public Deserializer() {
            super(RuleCondition.class);
        }

        @Override
        public RuleCondition deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            RuleCondition rc = new RuleCondition();

            if (node.has("selector") && !node.get("selector").isNull() && !node.get("selector").asText().isEmpty()) {
                // CDN wire format
                rc.setOperator(node.has("comparer") ? node.get("comparer").asText() : "");
                String selector = node.get("selector").asText();
                if ("context".equals(selector) || "segment".equals(selector)) {
                    rc.setAttribute(selector);
                } else {
                    // "attribute"
                    rc.setAttribute(node.has("selector_subtype") && !node.get("selector_subtype").isNull()
                            ? node.get("selector_subtype").asText() : "");
                }
                // Parse values array
                if (node.has("values") && node.get("values").isArray()) {
                    List<Object> values = new ArrayList<>();
                    for (JsonNode v : node.get("values")) {
                        if (v.isTextual()) {
                            values.add(v.asText());
                        } else if (v.isNumber()) {
                            values.add(v.numberValue());
                        } else if (v.isBoolean()) {
                            values.add(v.booleanValue());
                        } else if (v.isObject()) {
                            // context target object
                            values.add(p.getCodec().treeToValue(v, Object.class));
                        } else {
                            values.add(null);
                        }
                    }
                    rc.setValue(values);
                }
            } else {
                // Legacy format
                rc.setAttribute(node.has("attribute") ? node.get("attribute").asText() : "");
                rc.setOperator(node.has("operator") ? node.get("operator").asText() : "");
                if (node.has("value")) {
                    rc.setValue(p.getCodec().treeToValue(node.get("value"), Object.class));
                }
            }
            return rc;
        }
    }
}
