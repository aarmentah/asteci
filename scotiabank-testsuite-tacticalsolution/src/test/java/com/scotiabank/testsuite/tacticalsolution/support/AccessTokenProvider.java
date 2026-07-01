package com.scotiabank.testsuite.tacticalsolution.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scotiabank.testsuite.tacticalsolution.config.TestConfig;
import io.restassured.response.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.restassured.RestAssured.given;

public final class AccessTokenProvider {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String PLACEHOLDER_TOKEN = "replace-with-valid-bearer-token";
    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    private AccessTokenProvider() {
    }

    public static String resolve() {
        return resolve(TokenProfile.DEFAULT);
    }

    public static String resolve(TokenProfile profile) {
        String cacheKey = profile.id();
        String cached = CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        String manualToken = TestConfig.get(profile.manualTokenKey(), "");
        if (isUsableToken(manualToken)) {
            CACHE.put(cacheKey, manualToken);
            logResolvedToken(profile.id(), profile.manualTokenKey(), manualToken);
            return manualToken;
        }

        String passportBaseUri = TestConfig.get(profile.passportBaseUriKey(), "");
        if (passportBaseUri.isBlank()) {
            System.out.println("[AccessTokenProvider] Sin token para perfil "
                    + profile.id() + ": configura " + profile.manualTokenKey()
                    + " o " + profile.passportBaseUriKey());
            return "";
        }

        String tokenPath = TestConfig.get(profile.passportTokenPathKey(), "");
        if (tokenPath.isBlank()) {
            throw new IllegalStateException("Propiedad requerida vacía: " + profile.passportTokenPathKey());
        }

        String tokenUrl = joinUrl(passportBaseUri, tokenPath);
        System.out.println("[AccessTokenProvider] Solicitando token (" + profile.id() + "): " + tokenUrl);

        Response response = given()
                .redirects()
                .follow(true)
                .when()
                .get(tokenUrl)
                .then()
                .statusCode(200)
                .extract()
                .response();

        String responseBody = response.asString();
        logPassportResponse(responseBody);

        String token = extractToken(responseBody);
        if (!isUsableToken(token)) {
            throw new IllegalStateException(
                    "Passport no devolvió un token válido para perfil " + profile.id() + " desde: " + tokenUrl);
        }
        CACHE.put(cacheKey, token);
        logResolvedToken(profile.id(), "Passport", token);
        return token;
    }

    private static void logResolvedToken(String profileId, String source, String token) {
        System.out.println("[AccessTokenProvider] Token recuperado ("
                + profileId + ", " + source + "): " + maskToken(token));
    }

    private static void logPassportResponse(String body) {
        try {
            Object json = OBJECT_MAPPER.readValue(body, Object.class);
            String pretty = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            System.out.println("[AccessTokenProvider] Response Passport:\n" + pretty);
        } catch (Exception ignored) {
            System.out.println("[AccessTokenProvider] Response Passport: " + body);
        }
    }

    private static String maskToken(String token) {
        if (token.length() <= 20) {
            return "***";
        }
        return token.substring(0, 12) + "..." + token.substring(token.length() - 8);
    }

    static void resetCache() {
        CACHE.clear();
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
