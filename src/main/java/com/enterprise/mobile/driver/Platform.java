package com.enterprise.mobile.driver;

import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.config.FrameworkConstants;
import com.enterprise.mobile.exceptions.FrameworkException;

import java.util.Locale;

/**
 * Supported mobile platforms.
 */
public enum Platform {

    ANDROID("android"),
    IOS("ios");

    private final String value;

    Platform(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public boolean isAndroid() {
        return this == ANDROID;
    }

    public boolean isIos() {
        return this == IOS;
    }

    public static Platform from(String raw) {
        if (raw == null || raw.isBlank()) {
            return ANDROID;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (Platform platform : values()) {
            if (platform.value.equals(normalized) || platform.name().equalsIgnoreCase(normalized)) {
                return platform;
            }
        }
        throw new FrameworkException("Unsupported platform '%s'. Supported values: android, ios".formatted(raw));
    }

    public static Platform current() {
        return from(ConfigManager.get(FrameworkConstants.PLATFORM, FrameworkConstants.DEFAULT_PLATFORM));
    }
}
