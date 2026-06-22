package com.testsuite.login.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpectedResponse {

    private int statusCode;

    @JsonProperty("status_code_any_of")
    private List<Integer> statusCodeAnyOf = new ArrayList<>();

    @JsonProperty("expect_json")
    private boolean expectJson = true;

    private ExpectedStatus status;
    private ExpectedData data;

    @JsonProperty("status_message")
    private String statusMessage;

    @JsonProperty("status_description_contains")
    private String statusDescriptionContains;

    @JsonProperty("body_assertions")
    private List<BodyAssertion> bodyAssertions = new ArrayList<>();

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public List<Integer> getStatusCodeAnyOf() {
        return statusCodeAnyOf;
    }

    public void setStatusCodeAnyOf(List<Integer> statusCodeAnyOf) {
        this.statusCodeAnyOf = statusCodeAnyOf != null ? statusCodeAnyOf : new ArrayList<>();
    }

    public boolean isExpectJson() {
        return expectJson;
    }

    public void setExpectJson(boolean expectJson) {
        this.expectJson = expectJson;
    }

    public ExpectedStatus getStatus() {
        return status;
    }

    public void setStatus(ExpectedStatus status) {
        this.status = status;
    }

    public ExpectedData getData() {
        return data;
    }

    public void setData(ExpectedData data) {
        this.data = data;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getStatusDescriptionContains() {
        return statusDescriptionContains;
    }

    public void setStatusDescriptionContains(String statusDescriptionContains) {
        this.statusDescriptionContains = statusDescriptionContains;
    }

    public List<BodyAssertion> getBodyAssertions() {
        return bodyAssertions;
    }

    public void setBodyAssertions(List<BodyAssertion> bodyAssertions) {
        this.bodyAssertions = bodyAssertions != null ? bodyAssertions : new ArrayList<>();
    }
}
