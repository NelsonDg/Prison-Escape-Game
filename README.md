# CMPT276S26 Group 2 — Escape Game

A Java tile-based escape game built with Maven. The player must collect coins, avoid cops, and find shovel parts across 4 levels to escape the prison.

---

## Prerequisites

- Java 17+
- Maven 3.8+

---

## Build
```bash
cd escape-game
mvn compile
```

## Run
```bash
cd escape-game
mvn exec:java -Dexec.mainClass="escape.App"
```

## Run Tests
```bash
cd escape-game
mvn test
```

All tests must pass before merging any branch.

## Build Artifacts

### JAR File
To create an executable JAR file:
```bash
cd escape-game
mvn package
```

The JAR will be located at:
```
escape-game/target/escape-game-1.0-SNAPSHOT.jar
```

Run the JAR with:
```bash
java -jar escape-game/target/escape-game-1.0-SNAPSHOT.jar
```

### Javadoc
To generate API documentation:
```bash
cd escape-game
mvn javadoc:javadoc
```

View the documentation in your browser:
```
escape-game/target/site/apidocs/index.html
```

## Coverage Report

After running `mvn test`, open the HTML report in your browser:
```
escape-game/target/site/jacoco/index.html
```

This shows line and branch coverage for every class.

---

## Controls

| Key | Action |
|-----|--------|
| W / ↑ | Move up |
| S / ↓ | Move down |
| A / ← | Move left |
| D / → | Move right |

---

## How to Win

1. Collect the required number of coins on each level (shown in the HUD).
2. Find the **3 shovel parts** hidden across Levels 1–3 (Handle, Stick, Shovel Head).
3. Reach the exit tile on Level 4 with all parts collected.

## Lose Conditions

- Health drops to 0 (from cops or traps).
- Time runs out (300 ticks per level).

---

## Project Structure
```
escape-game/
├── src/main/java/escape/   # Production code
├── src/test/java/escape/   # Unit + integration tests
├── src/main/resources/     # Level map files (level1.txt – level4.txt)
└── pom.xml
```

---

## Team

| Name | Role |
|------|------|
| Rainier | GameEngine, UI, Integration Tests, Build |
| Derek | Board, Cell, Trap |
| Kenneth | Player, Cop, Enemy |
| Nelson | ScoreManager, Clock, Item, BonusItem, Shovel |
