package com.scotiabank.testsuite.tacticalsolution.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BodyAssertion {

    private String path;
    private Object equals;
    private String contains;

    @JsonProperty("contains_ignore_case")
    private String containsIgnoreCase;

    @JsonProperty("not_empty")
    private Boolean notEmpty;

    @JsonProperty("greater_than")
    private Number greaterThan;

    @JsonProperty("is_null")
    private Boolean isNull;

    @JsonProperty("any_of_equals")
    private java.util.List<String> anyOfEquals;

    @JsonProperty("any_of_contains")
    private java.util.List<String> anyOfContains;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Object getEquals() {
        return equals;
    }

    public void setEquals(Object equals) {
        this.equals = equals;
    }

    public String getContains() {
        return contains;
    }

    public void setContains(String contains) {
        this.contains = contains;
    }

    public String getContainsIgnoreCase() {
        return containsIgnoreCase;
    }

    public void setContainsIgnoreCase(String containsIgnoreCase) {
        this.containsIgnoreCase = containsIgnoreCase;
    }

    public Boolean getNotEmpty() {
        return notEmpty;
    }

    public void setNotEmpty(Boolean notEmpty) {
        this.notEmpty = notEmpty;
    }

    public Number getGreaterThan() {
        return greaterThan;
    }

    public void setGreaterThan(Number greaterThan) {
        this.greaterThan = greaterThan;
    }

    public Boolean getIsNull() {
        return isNull;
    }

    public void setIsNull(Boolean isNull) {
        this.isNull = isNull;
    }

    public java.util.List<String> getAnyOfEquals() {
        return anyOfEquals;
    }

    public void setAnyOfEquals(java.util.List<String> anyOfEquals) {
        this.anyOfEquals = anyOfEquals;
    }

    public java.util.List<String> getAnyOfContains() {
        return anyOfContains;
    }

    public void setAnyOfContains(java.util.List<String> anyOfContains) {
        this.anyOfContains = anyOfContains;
    }
}
