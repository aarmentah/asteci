package com.testsuite.login.support;

import com.testsuite.login.model.ApiScenario;
import io.qameta.allure.Allure;

public final class ScenarioTestRunner {

    private ScenarioTestRunner() {
    }

    public static void run(String scenarioPath) {
        ApiScenario scenario = ScenarioLoader.load(scenarioPath);

        Allure.label("scenario", scenarioPath);
        Allure.label("tag", scenario.getName());
        Allure.description("Archivo: src/test/resources/scenarios/" + scenarioPath + ".json");

        Allure.step("Ejecutar " + scenario.getMethod() + " " + scenario.getPath(), () ->
                ResponseAssertions.assertExpected(
                        RequestExecutor.execute(scenario),
                        scenario.getExpected()));
    }
}
