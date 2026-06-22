package com.testsuite.login.support;

import com.testsuite.login.model.ApiScenario;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public final class RequestExecutor {

    private RequestExecutor() {
    }

    public static ValidatableResponse execute(ApiScenario scenario) {
        RequestSpecification request = given()
                .filter(new AllureRestAssured())
                .headers(scenario.getHeaders())
                .log().all();

        if (scenario.getBaseUri() != null && !scenario.getBaseUri().isBlank()) {
            request.baseUri(scenario.getBaseUri());
        }

        if (!scenario.getQueryParams().isEmpty()) {
            request.queryParams(scenario.getQueryParams());
        }

        scenario.getPathParams().forEach(request::pathParam);

        if (scenario.getBody() != null) {
            request.body(scenario.getBody());
        }

        return request
                .when()
                .request(scenario.getMethod(), scenario.getPath())
                .then()
                .log().all();
    }
}
