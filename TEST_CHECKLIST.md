# FreeFrame Test Checklist

## Ziel
Validierung des Plugins auf Spigot `1.8.8` und `1.21.11`.

## Voraussetzungen
- Server A: Spigot `1.8.8`
- Server B: Spigot `1.21.11`
- Plugin-JAR: `target/FreeFrame-1.5.0.jar`
- Testspieler:
  - `Admin` (OP, alle Rechte)
  - `User` (keine Rechte)

## Installation
1. Server stoppen.
2. `FreeFrame-1.5.0.jar` nach `plugins/` kopieren.
3. Server starten.
4. Im Log `Status: Enabled` pruefen.
5. `plugins/FreeFrame/config.yml` pruefen.

## Funktionstests (beide Versionen)
1. Basisbefehle
- `/freeframe help` zeigt Subcommands.
- `/freeframe info` zeigt Version `1.5.0`.

2. Reload
- `User`: `/freeframe reload` -> Permission-Fehler.
- `Admin`: `/freeframe reload` -> Reload + Migrations-/Repair-Statistik.

3. Frame-Erstellung und GUI
- ItemFrame + stackbares Item (z. B. Stone) platzieren.
- Rechtsklick auf Frame -> GUI oeffnet, Slots `2/4/6` belegt.
- Item-Anzahl entspricht `freeframe.item.amount`.
- GUI blockiert Verschieben/Drag/Shift-Klick.

4. Kauf-Flow
- Preis auf `0`: Klick auf Slot `2/4/6` gibt Item + Free-Message.
- Preis > `0` + Vault aktiv: Klick zieht Geld ab + Success-Message.
- Preis > `0` + zu wenig Geld: kein Item + NotEnoughMoney-Message.
- Volles Inventar: Restitems werden gedroppt + InventoryDrop-Message.

5. Access-Policy
- `freeframe.access.requireOwner: true` setzen.
- Nicht-Eigentuemer: Zugriff blockiert + Denied-Message.
- Mit `freeframe.access.bypass`: Zugriff erlaubt.

6. Cooldown und Rate-Limit
- Schnelle Mehrfachklicks -> Cooldown- oder RateLimit-Message.
- Nach kurzer Wartezeit funktioniert Kauf wieder.

7. Destroy-Schutz
- Nicht berechtigt: Zerstoren blockiert.
- Berechtigt aber nicht Creative: blockiert.
- Creative ohne Sneak: blockiert.
- Creative + Sneak + Berechtigung: erlaubt.

8. Admin-Befehle
- `/freeframe list` zeigt Eintraege mit IDs.
- `/freeframe inspect <id>` zeigt Metadaten.
- `/freeframe setprice <id> 12.5 $` aktualisiert Preis.
- `/freeframe remove <id>` entfernt Eintrag.
- `/freeframe repair` bereinigt inkonsistente Eintraege.
- `/freeframe debug` zeigt Runtime-Metriken.

9. Migration
- Legacy-`freeframe.frames` Eintrag manuell setzen.
- `/freeframe migrate` -> Eintrag in `freeframe.framesData` uebernommen.

## Versionsspezifisch
1. Spigot 1.8.8
- Keine Startup-Exceptions.
- Alle Tests erfolgreich.

2. Spigot 1.21.11
- Keine Startup-Exceptions.
- Kein Offhand-Doppeltrigger bei Rechtsklick.
- Alle Tests erfolgreich.

## Regression
- Server-Stopp zeigt `Status: Disabled`.
- Nach Restart bleiben Frames in `freeframe.framesData` erhalten.
