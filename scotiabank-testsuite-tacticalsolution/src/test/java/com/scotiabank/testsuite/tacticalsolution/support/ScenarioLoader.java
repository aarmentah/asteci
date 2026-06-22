package com.scotiabank.testsuite.tacticalsolution.support;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scotiabank.testsuite.tacticalsolution.model.ApiScenario;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ScenarioLoader {

    private static final String SCENARIOS_ROOT = "scenarios/";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Map<String, ApiScenario> CACHE = new ConcurrentHashMap<>();

    private ScenarioLoader() {
    }

    public static ApiScenario load(String scenarioPath) {
        return CACHE.computeIfAbsent(scenarioPath, ScenarioLoader::readScenario);
    }

    public static ApiScenario loadAndOverride(String scenarioPath, Map<String, String> headerOverrides, Object bodyOverride) {
        ApiScenario scenario = load(scenarioPath).copy();
        if (headerOverrides != null && !headerOverrides.isEmpty()) {
            scenario.withHeaders(headerOverrides);
        }
        if (bodyOverride != null) {
            scenario.setBody(bodyOverride);
        }
        return scenario;
    }

    private static ApiScenario readScenario(String scenarioPath) {
        JsonNode rawScenario = readJsonNode(scenarioPath);
        String parentPath = textOrNull(rawScenario, "extends");

        ApiScenario scenario;
        if (parentPath != null) {
            ApiScenario parent = readScenario(parentPath);
            JsonNode merged = JsonMergeUtils.deepMerge(MAPPER.valueToTree(parent), rawScenario);
            scenario = MAPPER.convertValue(merged, ApiScenario.class);
        } else {
            scenario = MAPPER.convertValue(rawScenario, ApiScenario.class);
        }

        scenario.setParentScenario(null);
        return scenario;
    }

    private static JsonNode readJsonNode(String scenarioPath) {
        String resourcePath = SCENARIOS_ROOT + scenarioPath + ".json";
        try (InputStream input = ScenarioLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalArgumentException("Escenario no encontrado: " + resourcePath);
            }
            return MAPPER.readTree(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Error al leer escenario: " + resourcePath, exception);
        }
    }

    private static String textOrNull(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        return field.asText();
    }
}
