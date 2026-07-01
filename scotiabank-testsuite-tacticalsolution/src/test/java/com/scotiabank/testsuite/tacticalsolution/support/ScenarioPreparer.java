package com.scotiabank.testsuite.tacticalsolution.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.scotiabank.testsuite.tacticalsolution.config.TestConfig;
import com.scotiabank.testsuite.tacticalsolution.model.ApiScenario;
import com.scotiabank.testsuite.tacticalsolution.model.ScenarioSetup;
import io.qameta.allure.Allure;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ScenarioPreparer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern SURROGATE_PLACEHOLDER =
            Pattern.compile("\\{\\{surrogate:([a-zA-Z0-9_.-]+)}}");

    private ScenarioPreparer() {
    }

    public static ApiScenario prepare(ApiScenario scenario) {
        ApiScenario prepared = scenario.copy();
        ScenarioSetup setup = prepared.getSetup();
        if (setup == null) {
            return prepared;
        }

        TokenProfile tokenProfile = TokenProfile.fromId(setup.getTokenProfile());
        String accessToken = Allure.step(
                "Obtener token (" + tokenProfile.id() + ")",
                () -> AccessTokenProvider.resolve(tokenProfile));

        Map<String, String> resolvedSurrogates = resolveSurrogates(setup, accessToken);
        prepared.setHeaders(replacePlaceholders(prepared.getHeaders(), resolvedSurrogates));
        prepared.setQueryParams(replacePlaceholders(prepared.getQueryParams(), resolvedSurrogates));
        prepared.setPathParams(replacePlaceholders(prepared.getPathParams(), resolvedSurrogates));
        prepared.setPath(replacePlaceholders(prepared.getPath(), resolvedSurrogates));
        prepared.setBody(replacePlaceholders(prepared.getBody(), resolvedSurrogates));
        prepared.setBaseUri(resolveBaseUri(prepared.getBaseUri()));
        return prepared;
    }

    private static Map<String, String> resolveSurrogates(ScenarioSetup setup, String accessToken) {
        Map<String, String> resolved = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : setup.getSurrogateKeys().entrySet()) {
            String alias = entry.getKey();
            String naturalKey = entry.getValue();
            String surrogateKey = Allure.step(
                    "Resolver surrogate " + alias + " (" + naturalKey + ")",
                    () -> SurrogateKeyProvider.resolve(naturalKey, accessToken));
            resolved.put(alias, surrogateKey);
        }
        return resolved;
    }

    private static String resolveBaseUri(String configuredBaseUri) {
        if (configuredBaseUri == null || configuredBaseUri.isBlank()) {
            return configuredBaseUri;
        }
        if (!configuredBaseUri.startsWith("${")) {
            return configuredBaseUri;
        }
        String propertyKey = configuredBaseUri.substring(2, configuredBaseUri.length() - 1);
        String resolved = TestConfig.get(propertyKey, "");
        if (resolved.isBlank()) {
            throw new IllegalStateException(
                    "Configura " + propertyKey + " en application.properties o vía -D/" + envName(propertyKey));
        }
        return resolved;
    }

    private static String envName(String propertyKey) {
        return switch (propertyKey) {
            case "customer.summary.api.base.uri" -> "CUSTOMER_SUMMARY_API_BASE_URI";
            case "customer.lookup.api.base.uri" -> "CUSTOMER_LOOKUP_API_BASE_URI";
            default -> propertyKey.toUpperCase().replace('.', '_');
        };
    }

  @SuppressWarnings("unchecked")
    private static <T> T replacePlaceholders(T value, Map<String, String> resolvedSurrogates) {
        if (value == null || resolvedSurrogates.isEmpty()) {
            return value;
        }
        JsonNode node = OBJECT_MAPPER.valueToTree(value);
        JsonNode replaced = replaceInNode(node, resolvedSurrogates);
        return (T) OBJECT_MAPPER.convertValue(replaced, value.getClass());
    }

    private static String replacePlaceholders(String value, Map<String, String> resolvedSurrogates) {
        if (value == null || value.isBlank() || resolvedSurrogates.isEmpty()) {
            return value;
        }
        return replaceInText(value, resolvedSurrogates);
    }

    private static Map<String, String> replacePlaceholders(
            Map<String, String> values,
            Map<String, String> resolvedSurrogates) {
        if (values == null || values.isEmpty() || resolvedSurrogates.isEmpty()) {
            return values;
        }
        Map<String, String> replaced = new LinkedHashMap<>();
        values.forEach((key, value) -> replaced.put(key, replaceInText(value, resolvedSurrogates)));
        return replaced;
    }

    private static JsonNode replaceInNode(JsonNode node, Map<String, String> resolvedSurrogates) {
        if (node == null || node.isNull()) {
            return node;
        }
        if (node.isTextual()) {
            return new TextNode(replaceInText(node.asText(), resolvedSurrogates));
        }
        if (node.isArray()) {
            ArrayNode arrayNode = OBJECT_MAPPER.createArrayNode();
            node.forEach(item -> arrayNode.add(replaceInNode(item, resolvedSurrogates)));
            return arrayNode;
        }
        if (node.isObject()) {
            ObjectNode objectNode = OBJECT_MAPPER.createObjectNode();
            node.fields().forEachRemaining(field ->
                    objectNode.set(field.getKey(), replaceInNode(field.getValue(), resolvedSurrogates)));
            return objectNode;
        }
        return node;
    }

    static String applyPlaceholders(String text, Map<String, String> resolvedSurrogates) {
        return replaceInText(text, resolvedSurrogates);
    }

    private static String replaceInText(String text, Map<String, String> resolvedSurrogates) {
        Matcher matcher = SURROGATE_PLACEHOLDER.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String alias = matcher.group(1);
            String surrogate = resolvedSurrogates.get(alias);
            if (surrogate == null) {
                throw new IllegalStateException("No hay surrogate resuelto para alias: " + alias);
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(surrogate));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
