package de.isolveproblems.freeframe.api;

import java.util.List;

public interface ConfigEntry {
    String path();

    Object defaultValue();

    List<String> comments();
}
