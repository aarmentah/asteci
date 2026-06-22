package com.testsuite.login.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestConfig {

    private static final Properties PROPERTIES = load();

    private TestConfig() {
    }

    public static String get(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Propiedad no definida: " + key);
        }
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }
        return PROPERTIES.getProperty(key, defaultValue).trim();
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream input = TestConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("No se encontró application.properties en classpath");
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Error al cargar application.properties", exception);
        }
    }
}
