# FreeFrameProd

Spigot plugin to create free item-frame shops.

## Compatibility
- Minecraft/Spigot 1.8.8 up to 1.21.11.

## Codebase
- Standard Maven layout (`src/main/java`, `src/main/resources`, `src/test/java`)
- Constructor-based dependency injection between plugin, commands and listeners
- CI + tests for core utility behavior

## Build
```bash
mvn clean package
```

## Release Build (shaded)
```bash
mvn -P release clean package
```

## CI
- GitHub Actions workflow: `.github/workflows/ci.yml`
- Runs `mvn clean verify -P release` on push and pull requests.
