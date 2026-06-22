package com.testsuite.login.hu;

import com.testsuite.login.BaseTest;
import com.testsuite.login.support.ScenarioCatalog;
import com.testsuite.login.support.ScenarioTestCase;
import com.testsuite.login.support.ScenarioTestRunner;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@Epic("HU-006103_Autenticacion_Contraseña")
@Feature("POST /api/v1/authentication/password")
@DisplayName("HU-006103_Autenticacion_Contraseña")
@Owner("aquiles.armenta")
class HU006103AutenticacionContrasena extends BaseTest {

    private static final String CATALOG = "scenarios/catalog/password-scenarios.txt";

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
