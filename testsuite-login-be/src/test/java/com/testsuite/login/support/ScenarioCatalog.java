package com.testsuite.login.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public final class ScenarioCatalog {

    private ScenarioCatalog() {
    }

    public static Stream<String> lines(String catalogResourcePath) {
        InputStream input = ScenarioCatalog.class.getClassLoader().getResourceAsStream(catalogResourcePath);
        if (input == null) {
            throw new IllegalArgumentException("Catálogo no encontrado: " + catalogResourcePath);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        return reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"));
    }
}
