package com.uscbinp.infra.schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class SqlResourceLoader {

    private SqlResourceLoader() {
    }

    static String loadAsString(String resourcePath) {
        ClassLoader classLoader = SqlResourceLoader.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("SQL resource not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read SQL resource: " + resourcePath, ex);
        }
    }
}
