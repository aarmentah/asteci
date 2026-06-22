package com.testsuite.login;

import com.testsuite.login.config.TestConfig;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseTest {

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.baseURI = TestConfig.get("api.base.uri");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
