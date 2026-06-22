package com.testsuite.login.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiScenario {

    @JsonProperty("extends")
    private String parentScenario;

    private String name;
    private String method = "POST";
    private String path;
    private String baseUri;
    private Map<String, String> headers = new LinkedHashMap<>();
    private Map<String, String> queryParams = new LinkedHashMap<>();
    private Map<String, String> pathParams = new LinkedHashMap<>();
    private Object body;
    private ExpectedResponse expected;

    public ApiScenario copy() {
        ApiScenario copy = new ApiScenario();
        copy.parentScenario = parentScenario;
        copy.name = name;
        copy.method = method;
        copy.path = path;
        copy.baseUri = baseUri;
        copy.headers = new LinkedHashMap<>(headers);
        copy.queryParams = new LinkedHashMap<>(queryParams);
        copy.pathParams = new LinkedHashMap<>(pathParams);
        copy.body = body;
        copy.expected = expected;
        return copy;
    }

    public String getParentScenario() {
        return parentScenario;
    }

    public void setParentScenario(String parentScenario) {
        this.parentScenario = parentScenario;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams != null ? new LinkedHashMap<>(queryParams) : new LinkedHashMap<>();
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams != null ? new LinkedHashMap<>(pathParams) : new LinkedHashMap<>();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers != null ? new LinkedHashMap<>(headers) : new LinkedHashMap<>();
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public ExpectedResponse getExpected() {
        return expected;
    }

    public void setExpected(ExpectedResponse expected) {
        this.expected = expected;
    }

    @JsonIgnore
    public ApiScenario withHeader(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
        return this;
    }

    @JsonIgnore
    public ApiScenario withHeaders(Map<String, String> additionalHeaders) {
        headers.putAll(additionalHeaders);
        return this;
    }
}
