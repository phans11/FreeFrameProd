# FreeFrame Test Checklist

## Ziel
Validierung der Plugin-Funktion auf Spigot `1.8.8` und `1.21.11`.

## Voraussetzungen
- Server A: Spigot `1.8.8`
- Server B: Spigot `1.21.11`
- Plugin-JAR: `target/FreeFrame-1.2.0.jar`
- Testspieler:
  - `Admin` (OP, alle Rechte)
  - `User` (keine Rechte)

## Installation
- Server stoppen.
- `FreeFrame-1.2.0.jar` in `plugins/` kopieren.
- Server starten.
- Prüfen, ob im Log `Status: Enabled` erscheint.
- Prüfen, ob `plugins/FreeFrame/config.yml` erzeugt wurde.

## Funktionstests (auf beiden Versionen)
1. Command-Basis
- `/freeframe` zeigt Usage-Text.
- `/freeframe info` zeigt Plugin-Infos und Version `1.2.0`.

2. Reload-Berechtigung
- `User`: `/freeframe reload` -> Permission-Fehler.
- `Admin`: `/freeframe reload` -> Reload-Erfolgsmeldung.

3. Frame-Interaktion
- ItemFrame platzieren, stackbares Item einsetzen (z. B. Stone).
- Rechtsklick auf Frame -> GUI mit 3 Items auf Slots 2/4/6.
- Prüfen: Item-Anzahl entspricht `freeframe.item.amount`.

4. Destroy-Schutz
- `User` versucht ItemFrame zu zerstören -> blockiert + Fehlermeldung.
- `Admin` in Survival versucht zu zerstören -> blockiert + Creative-Hinweis.
- `Admin` in Creative, nicht sneaken -> blockiert + Sneak-Hinweis.
- `Admin` in Creative + sneaken -> Zerstören erlaubt + Erfolgsmeldung.

5. Pfeil-Schutz
- Mit Bogen auf ItemFrame schießen -> Schaden wird blockiert.

## Version-spezifisch
1. Spigot 1.8.8
- Keine Startup-Fehler/Exceptions.
- Alle Funktionstests erfolgreich.

2. Spigot 1.21.11
- Keine Startup-Fehler/Exceptions.
- Rechtsklick auf ItemFrame erzeugt keine Doppel-Aktion (Offhand-Handling).
- Alle Funktionstests erfolgreich.

## Regression Check
- Server-Stopp zeigt `Status: Disabled` im Log.
- Nach Server-Restart bleibt Konfiguration erhalten.
