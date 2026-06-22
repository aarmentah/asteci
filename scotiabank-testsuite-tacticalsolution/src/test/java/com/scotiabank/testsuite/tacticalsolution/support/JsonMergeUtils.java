package com.scotiabank.testsuite.tacticalsolution.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

final class JsonMergeUtils {

    private JsonMergeUtils() {
    }

    static JsonNode deepMerge(JsonNode base, JsonNode override) {
        if (override == null || override.isNull()) {
            return base;
        }
        if (base == null || base.isNull()) {
            return override;
        }

        if (base.isObject() && override.isObject()) {
            ObjectNode merged = base.deepCopy();
            override.fields().forEachRemaining(field -> {
                String fieldName = field.getKey();
                JsonNode baseValue = merged.get(fieldName);
                JsonNode overrideValue = field.getValue();
                if (baseValue != null && baseValue.isObject() && overrideValue.isObject()) {
                    merged.set(fieldName, deepMerge(baseValue, overrideValue));
                } else {
                    merged.set(fieldName, overrideValue.deepCopy());
                }
            });
            return merged;
        }

        return override.deepCopy();
    }
}
