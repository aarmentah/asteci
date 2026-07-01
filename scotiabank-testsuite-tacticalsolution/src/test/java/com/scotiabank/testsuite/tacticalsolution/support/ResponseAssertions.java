package com.scotiabank.testsuite.tacticalsolution.support;

import com.scotiabank.testsuite.tacticalsolution.model.BodyAssertion;
import com.scotiabank.testsuite.tacticalsolution.model.ExpectedData;
import com.scotiabank.testsuite.tacticalsolution.model.ExpectedResponse;
import com.scotiabank.testsuite.tacticalsolution.model.ExpectedStatus;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public final class ResponseAssertions {

    private ResponseAssertions() {
    }

    public static ValidatableResponse assertExpected(ValidatableResponse response, ExpectedResponse expected) {
        if (expected == null) {
            return response;
        }

        if (expected.getStatusCodeAnyOf() != null && !expected.getStatusCodeAnyOf().isEmpty()) {
            List<Matcher<Integer>> statusMatchers = new ArrayList<>();
            for (Integer code : expected.getStatusCodeAnyOf()) {
                statusMatchers.add(equalTo(code));
            }
            @SuppressWarnings("unchecked")
            Matcher<Integer>[] array = statusMatchers.toArray(Matcher[]::new);
            response.statusCode(anyOf(array));
        } else {
            response.statusCode(expected.getStatusCode());
        }

        if (expected.isExpectJson()) {
            response.contentType(ContentType.JSON);
        }

        ExpectedStatus status = expected.getStatus();
        if (status != null) {
            response.body("status.status_code", equalTo(status.getStatusCode()));
        }

        if (expected.getStatusMessage() != null) {
            response.body("status.message", equalTo(expected.getStatusMessage()));
        }

        if (expected.getStatusDescriptionContains() != null) {
            response.body("status.description", containsString(expected.getStatusDescriptionContains()));
        }

        ExpectedData data = expected.getData();
        if (data != null) {
            if (data.getTypeCase() != null) {
                response.body("data.type_case", equalTo(data.getTypeCase()));
            }
            if (data.isSubcaseNull()) {
                response.body("data.subcase", nullValue());
            }
            if (data.isUserIdNotEmpty()) {
                response.body("data.user_id", not(emptyOrNullString()));
            }
        }

        for (BodyAssertion assertion : expected.getBodyAssertions()) {
            applyAssertion(response, assertion);
        }

        return response;
    }

    private static void applyAssertion(ValidatableResponse response, BodyAssertion assertion) {
        String path = assertion.getPath();
        if (path == null || path.isBlank()) {
            return;
        }

        if (assertion.getEquals() != null) {
            response.body(path, equalTo(assertion.getEquals()));
        }
        if (assertion.getContains() != null) {
            response.body(path, containsString(assertion.getContains()));
        }
        if (assertion.getContainsIgnoreCase() != null) {
            response.body(path, containsStringIgnoringCase(assertion.getContainsIgnoreCase()));
        }
        if (Boolean.TRUE.equals(assertion.getNotEmpty())) {
            response.body(path, not(empty()));
        }
        if (assertion.getGreaterThan() != null) {
            response.body(path, greaterThan(assertion.getGreaterThan().intValue()));
        }
        if (Boolean.TRUE.equals(assertion.getIsNull())) {
            response.body(path, nullValue());
        }
        if (assertion.getAnyOfEquals() != null && !assertion.getAnyOfEquals().isEmpty()) {
            response.body(path, anyOfEquals(assertion.getAnyOfEquals()));
        }
        if (assertion.getAnyOfContains() != null && !assertion.getAnyOfContains().isEmpty()) {
            response.body(path, anyOfContains(assertion.getAnyOfContains()));
        }
    }

    @SuppressWarnings("unchecked")
    private static Matcher<Object> anyOfEquals(List<String> values) {
        List<Matcher<Object>> matchers = new ArrayList<>();
        for (String value : values) {
            matchers.add((Matcher<Object>) (Matcher<?>) equalTo(value));
        }
        return anyOf(matchers.toArray(Matcher[]::new));
    }

    @SuppressWarnings("unchecked")
    private static Matcher<Object> anyOfContains(List<String> values) {
        List<Matcher<Object>> matchers = new ArrayList<>();
        for (String value : values) {
            matchers.add((Matcher<Object>) (Matcher<?>) containsString(value));
        }
        return anyOf(matchers.toArray(Matcher[]::new));
    }
}
