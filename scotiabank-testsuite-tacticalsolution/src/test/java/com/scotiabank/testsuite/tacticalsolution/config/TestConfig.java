package com.scotiabank.testsuite.tacticalsolution.config;

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
        String envValue = envFor(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
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
        String envValue = envFor(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        String value = PROPERTIES.getProperty(key, defaultValue);
        return value == null ? defaultValue : value.trim();
    }

    private static String envFor(String key) {
        return switch (key) {
            case "api.base.uri" -> System.getenv("API_BASE_URI");
            case "api.access.token" -> System.getenv("API_ACCESS_TOKEN");
            case "passport.base.uri" -> System.getenv("PASSPORT_BASE_URL");
            case "passport.token.path" -> System.getenv("PASSPORT_TOKEN_PATH");
            case "customer.summary.api.base.uri" -> System.getenv("CUSTOMER_SUMMARY_API_BASE_URI");
            case "customer.summary.api.access.token" -> System.getenv("CUSTOMER_SUMMARY_API_ACCESS_TOKEN");
            case "customer.summary.passport.base.uri" -> System.getenv("CUSTOMER_SUMMARY_PASSPORT_BASE_URL");
            case "customer.summary.passport.token.path" -> System.getenv("CUSTOMER_SUMMARY_PASSPORT_TOKEN_PATH");
            case "customer.lookup.api.base.uri" -> System.getenv("CUSTOMER_LOOKUP_API_BASE_URI");
            case "customer.lookup.api.access.token" -> System.getenv("CUSTOMER_LOOKUP_API_ACCESS_TOKEN");
            case "customer.lookup.passport.base.uri" -> System.getenv("CUSTOMER_LOOKUP_PASSPORT_BASE_URL");
            case "customer.lookup.passport.token.path" -> System.getenv("CUSTOMER_LOOKUP_PASSPORT_TOKEN_PATH");
            case "keymaster.base.uri" -> System.getenv("KEYMASTER_BASE_URI");
            case "keymaster.surrogate.path" -> System.getenv("KEYMASTER_SURROGATE_PATH");
            default -> null;
        };
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
