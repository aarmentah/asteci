package com.scotiabank.testsuite.tacticalsolution.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scotiabank.testsuite.tacticalsolution.config.TestConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.restassured.RestAssured.given;

public final class SurrogateKeyProvider {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private SurrogateKeyProvider() {
    }

    public static String resolve(String naturalKey, String accessToken) {
        if (naturalKey == null || naturalKey.isBlank()) {
            throw new IllegalArgumentException("natural_key requerido para surrogate");
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("Token vacío al resolver surrogate para: " + naturalKey);
        }

        return CACHE.computeIfAbsent(cacheKey(naturalKey, accessToken), ignored -> fetchSurrogateKey(naturalKey, accessToken));
    }

    static void resetCache() {
        CACHE.clear();
    }

    static String extractSurrogateKey(String body, String naturalKey) {
        if (body == null || body.isBlank()) {
            return "";
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(body);
            String fromObject = extractFromObject(root, naturalKey);
            if (!fromObject.isBlank()) {
                return fromObject;
            }
            String fromArray = extractFromArray(root, naturalKey);
            if (!fromArray.isBlank()) {
                return fromArray;
            }
        } catch (Exception ignored) {
            // Respuesta no JSON.
        }
        return "";
    }

    private static String fetchSurrogateKey(String naturalKey, String accessToken) {
        String baseUri = TestConfig.get("keymaster.base.uri", "");
        String path = TestConfig.get("keymaster.surrogate.path", "/v1/keymaster/surrogate_keys");
        String url = joinUrl(baseUri, path);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("create_if_absent", true);
        requestBody.put("natural_keys", new String[]{naturalKey});

        System.out.println("[SurrogateKeyProvider] Resolviendo surrogate para natural_key=" + naturalKey);

        Response response = given()
                .filter(new AllureRestAssured())
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .log().all()
                .when()
                .post(url)
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .response();

        String responseBody = response.asString();
        logResponse(responseBody);

        String surrogateKey = extractSurrogateKey(responseBody, naturalKey);
        if (surrogateKey.isBlank()) {
            throw new IllegalStateException(
                    "Keymaster no devolvió surrogate para natural_key=" + naturalKey + " desde: " + url);
        }

        System.out.println("[SurrogateKeyProvider] Surrogate resuelto para " + naturalKey + ": " + maskValue(surrogateKey));
        return surrogateKey;
    }

    private static void logResponse(String body) {
        try {
            Object json = OBJECT_MAPPER.readValue(body, Object.class);
            String pretty = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("[SurrogateKeyProvider] Response Keymaster:\n" + pretty);
        } catch (Exception ignored) {
            System.out.println("[SurrogateKeyProvider] Response Keymaster: " + body);
        }
    }

    private static String extractFromObject(JsonNode root, String naturalKey) {
        for (String containerField : new String[]{"surrogate_keys", "data", "mappings", "keys"}) {
            JsonNode container = root.get(containerField);
            if (container == null) {
                continue;
            }
            if (container.isObject()) {
                if (container.hasNonNull(naturalKey)) {
                    return container.get(naturalKey).asText().trim();
                }
                JsonNode entry = container.get("natural_key");
                if (entry != null && naturalKey.equals(entry.asText()) && container.hasNonNull("surrogate_key")) {
                    return container.get("surrogate_key").asText().trim();
                }
            }
        }
        return "";
    }

    private static String extractFromArray(JsonNode root, String naturalKey) {
        for (String arrayField : new String[]{"surrogate_keys", "data", "mappings", "keys"}) {
            JsonNode arrayNode = root.get(arrayField);
            if (arrayNode == null || !arrayNode.isArray()) {
                continue;
            }
            for (JsonNode item : arrayNode) {
                if (!item.isObject()) {
                    continue;
                }
                String itemNaturalKey = textOrEmpty(item, "natural_key");
                if (itemNaturalKey.isBlank()) {
                    itemNaturalKey = textOrEmpty(item, "naturalKey");
                }
                if (!naturalKey.equals(itemNaturalKey)) {
                    continue;
                }
                String surrogate = textOrEmpty(item, "surrogate_key");
                if (surrogate.isBlank()) {
                    surrogate = textOrEmpty(item, "surrogateKey");
                }
                if (!surrogate.isBlank()) {
                    return surrogate;
                }
            }
        }
        return "";
    }

    private static String textOrEmpty(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return "";
        }
        return field.asText().trim();
    }

    private static String cacheKey(String naturalKey, String accessToken) {
        return naturalKey + "|" + accessToken.hashCode();
    }

    private static String maskValue(String value) {
        if (value.length() <= 12) {
            return "***";
        }
        return value.substring(0, 6) + "..." + value.substring(value.length() - 4);
    }

    private static String joinUrl(String baseUri, String path) {
        String normalizedBase = baseUri.endsWith("/")
                ? baseUri.substring(0, baseUri.length() - 1)
                : baseUri;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }
}
