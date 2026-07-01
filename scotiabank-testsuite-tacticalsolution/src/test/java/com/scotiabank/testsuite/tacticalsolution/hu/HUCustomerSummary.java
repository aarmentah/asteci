package com.scotiabank.testsuite.tacticalsolution.hu;

import com.scotiabank.testsuite.tacticalsolution.BaseTest;
import com.scotiabank.testsuite.tacticalsolution.support.ScenarioCatalog;
import com.scotiabank.testsuite.tacticalsolution.support.ScenarioTestCase;
import com.scotiabank.testsuite.tacticalsolution.support.ScenarioTestRunner;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@Epic("customer-summary")
@Feature("GET /api/v1/summary/customers | GET /api/v1/summary/cards")
@DisplayName("customer-summary")
@Owner("aquiles.armenta")
class HUCustomerSummary extends BaseTest {

    private static final String CATALOG = "scenarios/catalog/customer-summary-scenarios.txt";

    static Stream<ScenarioTestCase> allScenarios() {
        return ScenarioCatalog.lines(CATALOG).map(ScenarioTestCase::parse);
    }

    static Stream<ScenarioTestCase> smokeScenarios() {
        return allScenarios().filter(testCase -> "smoke".equals(testCase.tag()));
    }

    static Stream<ScenarioTestCase> regressionScenarios() {
        return allScenarios().filter(testCase -> !"smoke".equals(testCase.tag()));
    }

    @Tag("smoke")
    @ParameterizedTest(name = "[{0}] {1}")
    @MethodSource("smokeScenarios")
    void smoke(ScenarioTestCase testCase) {
        ScenarioTestRunner.run(testCase.path());
    }

    @Tag("regression")
    @ParameterizedTest(name = "[{0}] {1}")
    @MethodSource("regressionScenarios")
    void regression(ScenarioTestCase testCase) {
        ScenarioTestRunner.run(testCase.path());
    }
}
