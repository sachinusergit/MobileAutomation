package com.enterprise.mobile.utils;

import com.enterprise.mobile.exceptions.FrameworkException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Thin wrapper around Jackson used for device registries and JSON test data.
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private JsonUtils() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new FrameworkException("Unable to parse JSON into %s".formatted(type.getSimpleName()), e);
        }
    }

    public static <T> T fromResource(String resourcePath, Class<T> type) {
        try (InputStream stream = openResource(resourcePath)) {
            return MAPPER.readValue(stream, type);
        } catch (IOException e) {
            throw new FrameworkException("Unable to read JSON resource '%s'".formatted(resourcePath), e);
        }
    }

    public static <T> List<T> listFromResource(String resourcePath, Class<T> type) {
        try (InputStream stream = openResource(resourcePath)) {
            TypeFactory typeFactory = MAPPER.getTypeFactory();
            JavaType javaType = typeFactory.constructCollectionType(List.class, type);
            return MAPPER.readValue(stream, javaType);
        } catch (IOException e) {
            throw new FrameworkException("Unable to read JSON list resource '%s'".formatted(resourcePath), e);
        }
    }

    public static <T> List<T> listFromFile(Path path, Class<T> type) {
        try (InputStream stream = Files.newInputStream(path)) {
            TypeFactory typeFactory = MAPPER.getTypeFactory();
            JavaType javaType = typeFactory.constructCollectionType(List.class, type);
            return MAPPER.readValue(stream, javaType);
        } catch (IOException e) {
            throw new FrameworkException("Unable to read JSON file '%s'".formatted(path), e);
        }
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (IOException e) {
            throw new FrameworkException("Unable to serialise object to JSON", e);
        }
    }

    private static InputStream openResource(String resourcePath) {
        InputStream stream = JsonUtils.class.getClassLoader().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new FrameworkException("JSON resource '%s' not found on classpath".formatted(resourcePath));
        }
        return stream;
    }
}
