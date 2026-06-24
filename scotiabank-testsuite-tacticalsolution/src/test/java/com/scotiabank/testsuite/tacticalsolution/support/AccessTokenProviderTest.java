package com.scotiabank.testsuite.tacticalsolution.support;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AccessTokenProviderTest {

    @Test
    void extractToken_fromPassportOAuthResponse() {
        String token = "eyJraWQiOiIyZGZQTWttcUwwLWJXRENDVjAzOXRpNjljRWdubHMweS1DZkhydTFULXc4.test.signature";
        String body = """
                {
                    "access_token": "%s",
                    "scope": "customer.validation.ib.bcs.read customer:mx:customerblacklistms:read",
                    "token_type": "Bearer",
                    "expires_in": 3600
                }
                """.formatted(token);

        assertThat(AccessTokenProvider.extractToken(body), is(token));
    }
}
