package com.scotiabank.testsuite.tacticalsolution.support;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SurrogateKeyProviderTest {

    @Test
    void extractSurrogateKey_fromObjectMap() {
        String body = """
                {
                  "surrogate_keys": {
                    "505330355": "surrogate-client-123"
                  }
                }
                """;

        assertThat(
                SurrogateKeyProvider.extractSurrogateKey(body, "505330355"),
                is("surrogate-client-123"));
    }

    @Test
    void extractSurrogateKey_fromArray() {
        String body = """
                {
                  "surrogate_keys": [
                    {
                      "natural_key": "4918719086012802",
                      "surrogate_key": "surrogate-card-456"
                    }
                  ]
                }
                """;

        assertThat(
                SurrogateKeyProvider.extractSurrogateKey(body, "4918719086012802"),
                is("surrogate-card-456"));
    }
}
