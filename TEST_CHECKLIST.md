# FreeFrame Test Checklist

## Ziel
Validierung des Plugins auf Spigot `1.8.8` und `1.21.11`.

## Voraussetzungen
- Server A: Spigot `1.8.8`
- Server B: Spigot `1.21.11`
- Plugin-JAR: `target/FreeFrame-1.6.0.jar`
- Testspieler: `Admin` (OP), `User`
- Optional: Vault + Economy Plugin, PlaceholderAPI

## Installation
1. Server stoppen.
2. `FreeFrame-1.6.0.jar` nach `plugins/` kopieren.
3. Server starten.
4. Im Log `Status: Enabled` pruefen.
5. `plugins/FreeFrame/config.yml` pruefen.

## Funktionstests
1. Basis
- `/freeframe help` zeigt alle Subcommands.
- `/freeframe info` zeigt Version `1.6.0`.

2. Buy-Limits
- `freeframe.limits.enabled: true`
- Mehrfachkauf -> Limit-Meldung erscheint.

3. Stock + Auto-Refill
- `inspect` zeigt `stock/maxStock`.
- Bei `stock=0` kein Kauf moeglich.
- Auto-Refill fuellt nach Intervall auf.

4. Owner-Revenue
- Preis > `0`, Kauf durch fremden Spieler.
- Money wird dem Frame-Owner gutgeschrieben.

5. Display/Hologramm
- Ueber aktivem Frame wird ArmorStand-Name angezeigt.
- Preis/Stock-Aenderung aktualisiert Anzeige.

6. Item-Policy
- Blacklist oder Whitelist setzen.
- Geblockte Items koennen nicht als FreeFrame genutzt werden.

7. Restriktionen
- World-/Region-Restriktion aktivieren.
- Interaktion in gesperrten Bereichen wird blockiert.

8. Setup-Wand
- `/freeframe wand` gibt Wand.
- Rechtsklick auf Frame mit Wand -> Editor-GUI.
- Active/Stock/Preis/Auto-Refill dort aenderbar.

9. Logging + Export
- Kaeufe/Adminaktionen erzeugen Audit-Logs.
- `/freeframe export` erzeugt Exportdatei in `plugins/FreeFrame/exports`.

10. Storage Backends
- `/freeframe storage sqlite` und `/freeframe storage mysql` testen.
- Daten bleiben nach Neustart erhalten.

11. PlaceholderAPI
- Bei installiertem PlaceholderAPI: Placeholder in Messages/Display werden ersetzt.

## Regression
- Destroy-Schutz bleibt aktiv (Permission/Creative/Sneak).
- GUI blockiert Verschieben/Drag.
- Startup/Shutdown ohne Exceptions.
