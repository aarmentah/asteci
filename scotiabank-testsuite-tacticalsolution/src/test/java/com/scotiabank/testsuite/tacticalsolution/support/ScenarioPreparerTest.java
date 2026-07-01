package com.scotiabank.testsuite.tacticalsolution.support;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ScenarioPreparerTest {

    @Test
    void applyPlaceholders_replacesSurrogateAliases() {
        Map<String, String> resolved = new LinkedHashMap<>();
        resolved.put("client_id", "surrogate-client-123");
        resolved.put("card_id", "surrogate-card-456");

        assertThat(
                ScenarioPreparer.applyPlaceholders("{{surrogate:client_id}}", resolved),
                is("surrogate-client-123"));
        assertThat(
                ScenarioPreparer.applyPlaceholders("/cards/{{surrogate:card_id}}/summary", resolved),
                is("/cards/surrogate-card-456/summary"));
    }
}
