package com.scotiabank.testsuite.tacticalsolution.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Prueba aislada del flujo de token (manual o Passport).
 * Ejecutar: mvn test -Dtest=AccessTokenProviderIntegrationTest -Dpassport.base.uri=https://...
 */
@Tag("token")
class AccessTokenProviderIntegrationTest {

    @BeforeEach
    void resetCache() {
        AccessTokenProvider.resetCache();
    }

    @Test
    void fetchToken() {
        String token = AccessTokenProvider.resolve();

        assertFalse(
                token.isBlank(),
                "No se obtuvo token. Configura -Dpassport.base.uri=... o -Dapi.access.token=...");
    }
}
