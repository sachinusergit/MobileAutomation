package com.enterprise.mobile.config;

import java.util.Arrays;
import java.util.Locale;

/**
 * Supported execution environments. Each environment maps to a
 * {@code config/<environment>.properties} file on the classpath.
 */
public enum Environment {

    DEV("dev"),
    QA("qa"),
    STAGING("staging"),
    PROD("prod"),
    CI("ci"),
    IOS("ios"),
    SETTINGS("settings");

    private final String value;

    Environment(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Environment from(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEV;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(environment -> environment.value.equals(normalized) || environment.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown environment '%s'. Supported values: dev, qa, staging, prod, ci, ios, settings"
                                .formatted(raw)));
    }
}
