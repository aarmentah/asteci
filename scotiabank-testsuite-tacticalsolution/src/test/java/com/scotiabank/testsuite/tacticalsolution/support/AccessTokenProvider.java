package com.scotiabank.testsuite.tacticalsolution.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scotiabank.testsuite.tacticalsolution.config.TestConfig;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public final class AccessTokenProvider {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PLACEHOLDER_TOKEN = "replace-with-valid-bearer-token";
    private static volatile String cachedToken;

    private AccessTokenProvider() {
    }

    public static String resolve() {
        if (cachedToken != null) {
            return cachedToken;
        }

        String manualToken = TestConfig.get("api.access.token", "");
        if (isUsableToken(manualToken)) {
            cachedToken = manualToken;
            return cachedToken;
        }

        String passportBaseUri = TestConfig.get("passport.base.uri", "");
        if (passportBaseUri.isBlank()) {
            return "";
        }

        String tokenPath = TestConfig.get(
                "passport.token.path",
                "/48cf7cec-2dfe-4695-a3b1-eb423fc6418c");
        String tokenUrl = joinUrl(passportBaseUri, tokenPath);

        Response response = given()
                .redirects()
                .follow(true)
                .when()
                .get(tokenUrl)
                .then()
                .statusCode(200)
                .extract()
                .response();

        cachedToken = extractToken(response.asString());
        if (!isUsableToken(cachedToken)) {
            throw new IllegalStateException(
                    "Passport no devolvió un token válido desde: " + tokenUrl);
        }
        return cachedToken;
    }

    static void resetCache() {
        cachedToken = null;
    }

    private static boolean isUsableToken(String token) {
        return token != null && !token.isBlank() && !PLACEHOLDER_TOKEN.equals(token);
    }

    private static String joinUrl(String baseUri, String path) {
        String normalizedBase = baseUri.endsWith("/")
                ? baseUri.substring(0, baseUri.length() - 1)
                : baseUri;
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedBase + normalizedPath;
    }

    static String extractToken(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }

        String trimmed = body.trim();
        try {
            JsonNode node = OBJECT_MAPPER.readTree(trimmed);
            if (node.isTextual()) {
                return node.asText().trim();
            }
            if (node.hasNonNull("access_token")) {
                return node.get("access_token").asText().trim();
            }
            if (node.hasNonNull("token")) {
                return node.get("token").asText().trim();
            }
            JsonNode data = node.get("data");
            if (data != null) {
                if (data.isTextual()) {
                    return data.asText().trim();
                }
                if (data.hasNonNull("access_token")) {
                    return data.get("access_token").asText().trim();
                }
                if (data.hasNonNull("token")) {
                    return data.get("token").asText().trim();
                }
            }
        } catch (Exception ignored) {
            // Respuesta plana (solo el token en texto).
        }
        return trimmed;
    }
}
