package com.enterprise.mobile.config;

import com.enterprise.mobile.exceptions.FrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Single entry point for every configuration value used by the framework.
 *
 * <p>Resolution precedence for a given key (highest first):</p>
 * <ol>
 *     <li>JVM system property, e.g. {@code -Dplatform=ios}</li>
 *     <li>Environment variable, e.g. {@code PLATFORM=ios} (dots become underscores)</li>
 *     <li>{@code config/<env>.properties} on the classpath</li>
 *     <li>{@code config/common.properties} on the classpath</li>
 *     <li>Caller supplied default</li>
 * </ol>
 *
 * <p>The environment itself is selected with {@code -Denv=qa} or the
 * {@code ENV} environment variable and defaults to {@code dev}.</p>
 */
public final class ConfigManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_DIR = "config/";
    private static final String COMMON_PROPERTIES = CONFIG_DIR + "common.properties";

    private static final ConfigManager INSTANCE = new ConfigManager();

    private final Properties properties = new Properties();
    private final Environment environment;

    private ConfigManager() {
        this.environment = Environment.from(resolveRaw(FrameworkConstants.ENV, System.getenv("ENV")));
        load(COMMON_PROPERTIES);
        load(CONFIG_DIR + environment.value() + ".properties");
        LOG.info("Configuration initialised for environment '{}' ({} keys)", environment.value(), properties.size());
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public static Environment environment() {
        return INSTANCE.environment;
    }

    public static String get(String key) {
        return INSTANCE.resolve(key);
    }

    public static String get(String key, String defaultValue) {
        String value = INSTANCE.resolve(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static String getRequired(String key) {
        String value = INSTANCE.resolve(key);
        if (value == null || value.isBlank()) {
            throw new FrameworkException("Required configuration property '%s' is missing".formatted(key));
        }
        return value;
    }

    public static int getInt(String key, int defaultValue) {
        String value = INSTANCE.resolve(key);
        return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value.trim());
    }

    public static long getLong(String key, long defaultValue) {
        String value = INSTANCE.resolve(key);
        return value == null || value.isBlank() ? defaultValue : Long.parseLong(value.trim());
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = INSTANCE.resolve(key);
        return value == null || value.isBlank() ? defaultValue : Boolean.parseBoolean(value.trim());
    }

    /**
     * Returns all properties whose key starts with the supplied prefix. The
     * prefix is removed from the returned keys. System properties and
     * environment variables are merged on top of the file based values.
     */
    public static Map<String, String> getPropertiesWithPrefix(String prefix) {
        Map<String, String> result = new LinkedHashMap<>();
        Properties merged = new Properties();
        merged.putAll(INSTANCE.properties);

        System.getProperties().forEach((key, value) -> {
            String stringKey = String.valueOf(key);
            if (stringKey.startsWith(prefix)) {
                merged.put(stringKey, String.valueOf(value));
            }
        });
        System.getenv().forEach((key, value) -> {
            String asPropertyKey = key.toLowerCase().replace('_', '.');
            // Environment variables cannot contain dots; only include a match when
            // the transformed key already exists as a known property.
            if (asPropertyKey.startsWith(prefix) && merged.containsKey(asPropertyKey)) {
                merged.put(asPropertyKey, value);
            }
        });

        for (String name : merged.stringPropertyNames()) {
            if (name.startsWith(prefix)) {
                result.put(name.substring(prefix.length()), merged.getProperty(name));
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private String resolve(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        String envValue = System.getenv(toEnvironmentVariable(key));
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return properties.getProperty(key);
    }

    private static String resolveRaw(String key, String environmentFallback) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        String envValue = System.getenv(toEnvironmentVariable(key));
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return environmentFallback;
    }

    private static String toEnvironmentVariable(String key) {
        return key.replace('.', '_').replace('-', '_').toUpperCase();
    }

    private void load(String resourcePath) {
        try (InputStream stream = ConfigManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                LOG.warn("Configuration resource '{}' not found on classpath", resourcePath);
                return;
            }
            Properties loaded = new Properties();
            loaded.load(stream);
            // Files are loaded common -> environment specific, later files win.
            properties.putAll(loaded);
        } catch (IOException e) {
            throw new FrameworkException("Unable to read configuration resource '%s'".formatted(resourcePath), e);
        }
    }
}
