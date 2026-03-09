# FreeFrameProd

Spigot-Plugin fuer ItemFrame-Shops mit Free- und Preis-Mode.

## Kompatibilitaet
- Minecraft/Spigot `1.8.8` bis `1.21.11`
- Java 8 Build-Target

## Umgesetzte Features
1. Buy-Limits pro Spieler/Zeitfenster
2. Frame-Stock inkl. Auto-Refill
3. Owner-Revenue (Vault Payout)
4. Display/Hologramm via ArmorStand ueber Frame
5. Global Blacklist/Whitelist fuer Items
6. Audit-Logging + CSV-Export
7. World-/Region-Restriktionen
8. In-Game Setup-Wand + Editor-GUI
9. PlaceholderAPI-Unterstuetzung (optional)
10. Storage-Backends: YAML, SQLite, MySQL
11. Chest-basiertes Restocking
12. Mehrere Kaufprofile pro Frame
13. Rabatt-System ueber Permissions
14. Transaktionsschutz gegen Klick-Races
15. Frame- und Spieler-Statistiken
16. Frame-Typen: `FREE`, `SHOP`, `LIMITED`, `ADMIN_ONLY`, `PREVIEW_ONLY`
17. Mehrsprachige Messages (`messages_en.yml`, `messages_de.yml`)
18. Backup-, Restore- und Doctor-Tools
19. Discord/Webhook-Exports
20. WorldGuard/GriefPrevention-Hooks (best effort)
21. Asynchrone DB-Save-Queue fuer SQLite/MySQL
22. Transaktionsjournal inkl. Replay fuer Idempotency-Rebuild
23. Dynamic Pricing (Nachfrage + Stock-abhängig)
24. Tax/Fee-System (Admin-Shop/User-Shop getrennt)
25. Auktionen mit Bidding und automatischem Settlement
26. Shop-Netzwerke (gemeinsamer Pool/Management)
27. Saisonregeln mit Preis-/Tax-Override per Zeitfenster
28. Dashboard HTTP-Server fuer Health/Stats/Frames
29. Alerting (Low-Stock, Purchase-/Auction-Probleme)
30. Exploit-Hardening (signierte IDs, Idempotency, Race-Schutz)
31. Admin-Shop vs User-Shop Trennung mit Offer-Filter `admin|user|both`
32. Owner-Management fuer eigene Shops

## Commands
- `/freeframe help`
- `/freeframe info`
- `/freeframe reload`
- `/freeframe bid <id> <amount>`
- `/freeframe myshops`
- `/freeframe list [page]`
- `/freeframe inspect <id>`
- `/freeframe remove <id>`
- `/freeframe setprice <id> <price> [currency]`
- `/freeframe setstock <id> <stock> [max]`
- `/freeframe settype <id> <type>`
- `/freeframe shoptype <id> <admin|user>`
- `/freeframe setprofile <id> <slot> <amount> <price> [displayName]`
- `/freeframe clearprofiles <id>`
- `/freeframe linkchest <id>`
- `/freeframe network <set|clear|info> ...`
- `/freeframe season <set|clear|info> ...`
- `/freeframe auction <start|stop|info> ...`
- `/freeframe stats <frame|player> <target>`
- `/freeframe backup`
- `/freeframe restore <file>`
- `/freeframe doctor`
- `/freeframe replay [dry]`
- `/freeframe wand`
- `/freeframe storage <yaml|sqlite|mysql>`
- `/freeframe export`
- `/freeframe migrate`
- `/freeframe repair`
- `/freeframe debug`

## Rechte
- `freeframe.reload`
- `freeframe.destroy`
- `freeframe.admin`
- `freeframe.access.bypass`
- `freeframe.owner.manage`
- `freeframe.adminonly`
- `freeframe.discount.vip`
- `freeframe.discount.mvp`

## Build
```bash
mvn clean package
```

## Release Build (Shade)
```bash
mvn clean verify -P release
```

## Hinweise zu Storage
- `freeframe.storage.type: yaml|sqlite|mysql`
- SQLite-Datei: `freeframe.storage.sqlite.file`
- MySQL-Zugang: `freeframe.storage.mysql.*`
- Tabellenname: `freeframe.storage.mysql.table`
- Auto-Migration bei Backend-Switch: `freeframe.storage.migrateOnSwitch`

## Admin-Konfiguration (wichtigste Gruppen)
- GUI: `freeframe.gui.*` (Titel, Groesse, Verkaufsslots, Verhalten bei Kauf)
- Setup-Editor: `freeframe.setup.editor.*` (Schritte, Slots, Materialien, Auto-Close)
- Setup-Wand: `freeframe.setup.wand*` (Material, Name, Lore, Menge)
- Display/Hologramm: `freeframe.display.*` (Offset, ArmorStand-Eigenschaften, Sichtbarkeit)
- Economy: `freeframe.economy.*` (Owner-Payout, Verhalten ohne Vault)
- Logging/Export: `freeframe.logging.*` (Aktivierung, Unterordner, Dateiname)
- Webhooks: `freeframe.webhooks.*` (Discord/Webhook Ziel)
- Alerts: `freeframe.alerts.*` (Cooldown, Schwellen)
- Limits/Stock: `freeframe.limits.*`, `freeframe.stock.*`
- Kaufprofile/Typen: `freeframe.profiles.*`, `freeframe.types.*`
- Shop-Angebot: `freeframe.shops.offerMode` (`ADMIN|USER|BOTH`)
- Restriktionen: `freeframe.restrictions.*` (Welten und Cuboid-Regionen)
- Integrationen: `freeframe.integrations.*` (WorldGuard, GriefPrevention)
- Item-Policy: `freeframe.items.*` (Blacklist/Whitelist)
- Lokalisierung: `freeframe.localization.*`
- Dynamic Pricing: `freeframe.dynamicPricing.*`
- Tax/Fee: `freeframe.tax.*`
- Seasons: `freeframe.seasons.*`
- Auction: `freeframe.auction.*`
- Dashboard: `freeframe.dashboard.*`
- Security/Idempotency: `freeframe.security.*`
- Async DB Queue: `freeframe.storage.asyncQueue.enabled`

## CI
- Workflow: `.github/workflows/ci.yml`
