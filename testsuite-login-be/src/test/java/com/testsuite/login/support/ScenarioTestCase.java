package com.testsuite.login.support;

public record ScenarioTestCase(String path, String displayName, String tag) {

    public static ScenarioTestCase parse(String line) {
        String[] parts = line.split("\\|", -1);
        String path = parts[0].trim();
        String displayName = parts.length > 1 ? parts[1].trim() : path;
        String tag = parts.length > 2 && !parts[2].isBlank() ? parts[2].trim() : "regression";
        return new ScenarioTestCase(path, displayName, tag);
    }
}
