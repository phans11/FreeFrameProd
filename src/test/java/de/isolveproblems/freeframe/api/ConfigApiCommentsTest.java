package de.isolveproblems.freeframe.api;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigApiCommentsTest {
    private enum TestKey implements ConfigEntry {
        ROOT_ENABLED("root.enabled", true, Arrays.asList("Enables root feature", "Default: true")),
        ROOT_LIMIT("root.limit", 5, Arrays.asList("Maximum limit", "Default: 5"));

        private final String path;
        private final Object defaultValue;
        private final List<String> comments;

        TestKey(String path, Object defaultValue, List<String> comments) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.comments = comments;
        }

        @Override
        public String path() {
            return this.path;
        }

        @Override
        public Object defaultValue() {
            return this.defaultValue;
        }

        @Override
        public List<String> comments() {
            return this.comments;
        }
    }

    @Test
    void shouldPersistCommentsForEnumDefaults() throws Exception {
        File directory = Files.createTempDirectory("ff-config-api-test").toFile();
        ConfigAPI configApi = new ConfigAPI(directory, "test-config.yml");
        configApi.setCommentsEnabled(true);
        configApi.addDefaults(TestKey.values());
        configApi.getConfig().options().copyDefaults(true);
        configApi.saveConfig();

        String content = new String(Files.readAllBytes(new File(directory, "test-config.yml").toPath()), StandardCharsets.UTF_8);
        assertTrue(content.contains("# Enables root feature"));
        assertTrue(content.contains("# Maximum limit"));
        assertTrue(content.contains("enabled: true"));
        assertTrue(content.contains("limit: 5"));
    }
}
