package de.isolveproblems.freeframe.api;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigAPI {
    private static final Pattern YAML_KEY_PATTERN = Pattern.compile("^(\\s*)([A-Za-z0-9_.-]+):(?:\\s.*)?$");

    private final File file;
    private FileConfiguration fileConfig;
    private final Map<String, List<String>> commentsByPath = new HashMap<String, List<String>>();
    private boolean commentsEnabled;

    public ConfigAPI(String path, String fileName) {
        this(new File(path), fileName);
    }

    public ConfigAPI(File folder, String fileName) {
        String resolvedFileName = fileName.endsWith(".yml") ? fileName : fileName + ".yml";

        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Could not create config folder: " + folder.getAbsolutePath());
        }

        this.file = new File(folder, resolvedFileName);
        this.fileConfig = YamlConfiguration.loadConfiguration(this.file);

        if (!this.file.exists()) {
            this.fileConfig.options().copyDefaults(true);
            this.saveConfig();
        }
    }

    public FileConfiguration getConfig() {
        return this.fileConfig;
    }

    public void setCommentsEnabled(boolean commentsEnabled) {
        this.commentsEnabled = commentsEnabled;
    }

    public boolean isCommentsEnabled() {
        return this.commentsEnabled;
    }

    public void registerComments(String path, List<String> comments) {
        if (path == null || path.trim().isEmpty() || comments == null || comments.isEmpty()) {
            return;
        }
        List<String> sanitized = new ArrayList<String>();
        for (String comment : comments) {
            if (comment == null) {
                continue;
            }
            sanitized.add(comment.replace("\n", " ").replace("\r", " "));
        }
        if (sanitized.isEmpty()) {
            return;
        }
        this.commentsByPath.put(path, Collections.unmodifiableList(sanitized));
    }

    public <E extends Enum<E> & ConfigEntry> void addDefault(E entry) {
        if (entry == null) {
            return;
        }
        this.fileConfig.addDefault(entry.path(), entry.defaultValue());
        this.registerComments(entry.path(), entry.comments());
    }

    public <E extends Enum<E> & ConfigEntry> void addDefaults(E[] entries) {
        if (entries == null) {
            return;
        }
        for (E entry : entries) {
            this.addDefault(entry);
        }
    }

    public <E extends Enum<E> & ConfigEntry> String getString(E entry) {
        if (entry == null) {
            return "";
        }
        Object fallback = entry.defaultValue();
        String fallbackText = fallback == null ? "" : String.valueOf(fallback);
        return this.fileConfig.getString(entry.path(), fallbackText);
    }

    public <E extends Enum<E> & ConfigEntry> boolean getBoolean(E entry) {
        if (entry == null) {
            return false;
        }
        Object fallback = entry.defaultValue();
        boolean fallbackValue = fallback instanceof Boolean && ((Boolean) fallback).booleanValue();
        return this.fileConfig.getBoolean(entry.path(), fallbackValue);
    }

    public <E extends Enum<E> & ConfigEntry> int getInt(E entry) {
        if (entry == null) {
            return 0;
        }
        Object fallback = entry.defaultValue();
        int fallbackValue = fallback instanceof Number ? ((Number) fallback).intValue() : 0;
        return this.fileConfig.getInt(entry.path(), fallbackValue);
    }

    public <E extends Enum<E> & ConfigEntry> long getLong(E entry) {
        if (entry == null) {
            return 0L;
        }
        Object fallback = entry.defaultValue();
        long fallbackValue = fallback instanceof Number ? ((Number) fallback).longValue() : 0L;
        return this.fileConfig.getLong(entry.path(), fallbackValue);
    }

    public <E extends Enum<E> & ConfigEntry> double getDouble(E entry) {
        if (entry == null) {
            return 0.0D;
        }
        Object fallback = entry.defaultValue();
        double fallbackValue = fallback instanceof Number ? ((Number) fallback).doubleValue() : 0.0D;
        return this.fileConfig.getDouble(entry.path(), fallbackValue);
    }

    public <E extends Enum<E> & ConfigEntry> List<String> getStringList(E entry) {
        if (entry == null) {
            return Collections.emptyList();
        }
        return this.fileConfig.getStringList(entry.path());
    }

    public <E extends Enum<E> & ConfigEntry> void set(E entry, Object value) {
        if (entry == null) {
            return;
        }
        this.fileConfig.set(entry.path(), value);
    }

    public void saveConfig() {
        try {
            if (!this.commentsEnabled || this.commentsByPath.isEmpty()) {
                this.fileConfig.save(this.file);
                return;
            }
            this.saveConfigWithComments();
        } catch (IOException exception) {
            throw new RuntimeException("Could not save config file: " + this.file.getAbsolutePath(), exception);
        }
    }

    public void reloadConfig() {
        this.fileConfig = YamlConfiguration.loadConfiguration(this.file);
    }

    private void saveConfigWithComments() throws IOException {
        String yaml = this.fileConfig.saveToString();
        String withComments = this.injectComments(yaml);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.file), StandardCharsets.UTF_8));
            writer.write(withComments);
            if (!withComments.endsWith("\n")) {
                writer.newLine();
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private String injectComments(String yaml) {
        if (yaml == null || yaml.trim().isEmpty()) {
            return yaml == null ? "" : yaml;
        }

        String[] lines = yaml.split("\\r?\\n", -1);
        StringBuilder builder = new StringBuilder(yaml.length() + Math.max(512, this.commentsByPath.size() * 32));
        List<PathNode> pathStack = new ArrayList<PathNode>();
        Set<String> inserted = new HashSet<String>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#") || trimmed.isEmpty() || trimmed.startsWith("-")) {
                builder.append(line).append('\n');
                continue;
            }

            Matcher matcher = YAML_KEY_PATTERN.matcher(line);
            if (!matcher.find()) {
                builder.append(line).append('\n');
                continue;
            }

            int indent = matcher.group(1).length();
            String key = matcher.group(2);
            while (!pathStack.isEmpty() && indent <= pathStack.get(pathStack.size() - 1).indent) {
                pathStack.remove(pathStack.size() - 1);
            }

            String fullPath = pathStack.isEmpty()
                ? key
                : pathStack.get(pathStack.size() - 1).path + "." + key;
            List<String> comments = this.commentsByPath.get(fullPath);
            if (comments != null && !inserted.contains(fullPath)) {
                String prefix = this.spaces(indent) + "# ";
                for (String comment : comments) {
                    if (comment == null || comment.trim().isEmpty()) {
                        builder.append(this.spaces(indent)).append('#').append('\n');
                    } else {
                        builder.append(prefix).append(comment).append('\n');
                    }
                }
                inserted.add(fullPath);
            }

            builder.append(line).append('\n');
            pathStack.add(new PathNode(indent, fullPath));
        }

        return builder.toString();
    }

    private String spaces(int amount) {
        if (amount <= 0) {
            return "";
        }
        char[] chars = new char[amount];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = ' ';
        }
        return new String(chars);
    }

    private static final class PathNode {
        private final int indent;
        private final String path;

        private PathNode(int indent, String path) {
            this.indent = Math.max(0, indent);
            this.path = path == null ? "" : path;
        }
    }
}
