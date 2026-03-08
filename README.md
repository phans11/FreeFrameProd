# FreeFrameProd

Spigot-Plugin fuer ItemFrame-Shops mit Free- und Price-Modus.

## Kompatibilitaet
- Minecraft/Spigot `1.8.8` bis `1.21.11`
- Java 8 Build-Target

## Features
- FreeFrame-GUI mit festen Verkaufsslots (`2/4/6`)
- Optionales Pricing pro Frame (Vault-Economy, konfigurierbar)
- Ownership-/Access-Policy (`requireOwner` + Bypass-Permission)
- Player-Cooldown + Frame-Rate-Limit
- Persistente Frame-Metadaten (`freeframe.framesData`)
- Admin-Tools: `list`, `inspect`, `remove`, `setprice`, `migrate`, `repair`, `debug`
- Migration alter `freeframe.frames` Eintraege

## Befehle
- `/freeframe help`
- `/freeframe info`
- `/freeframe reload`
- `/freeframe list [page]`
- `/freeframe inspect <id>`
- `/freeframe remove <id>`
- `/freeframe setprice <id> <price> [currency]`
- `/freeframe migrate`
- `/freeframe repair`
- `/freeframe debug`

## Rechte
- `freeframe.reload`
- `freeframe.destroy`
- `freeframe.admin`
- `freeframe.access.bypass`

## Build
```bash
mvn clean package
```

## Release Build (Shade)
```bash
mvn clean verify -P release
```

## CI
- Workflow: `.github/workflows/ci.yml`
- Build + Tests via Maven Release-Profil
