package escape.factories;

import escape.main.GameEngine;
import escape.main.LevelConfig;

/**
 * LevelFactory is the abstract Creator in the
 * Factory Method design pattern.
 *
 * Why Factory Method?
 * Each of the four prison sections (Cell Block, Cafeteria &amp; Washrooms,
 * General Time, Outdoor Recreation) has a unique layout, different enemy
 * counts, a different coin requirement, and potentially different construction
 * logic.  Factory Method lets us:
 *   Isolate variation – each level's setup is encapsulated
 *       in its own concrete subclass, not in a giant switch statement.
 *   Open/Closed Principle – adding a fifth level means
 *       writing one new subclass with no changes to existing code.
 *   Testability – each factory can be instantiated and
 *       tested independently.
 *
 * Pattern roles in this codebase
 *   Creator: {@code LevelFactory} (this class)
 *   Concrete Creators: {@link CellBlockFactory},
 *       {@link CafeteriaFactory}, {@link GeneralTimeFactory},
 *       {@link OutdoorRecFactory}
 *   Product: {@link LevelConfig}
 *
 * Callers use {@link #forLevel(int)} to obtain the correct factory without
 * knowing or depending on the concrete type.
 */
public abstract class LevelFactory {

    /**
     * Factory method — implemented by each concrete subclass to produce a
     * fully populated {@link LevelConfig} for its prison section.
     *
     * Spawn coordinates provided here are desired positions.
     * {@link GameEngine} validates each one against the board and moves it to
     * the nearest walkable cell if it falls inside a wall.
     *
     * @return configuration object for this level
     */
    public abstract LevelConfig createConfig();

    /**
     * Shared helper that removes duplicated LevelConfig constructor calls
     * across all concrete factory subclasses.
     *
     * @param levelNumber         1-based level index (1–4)
     * @param levelName           human-readable section name
     * @param startX              player start x
     * @param startY              player start y
     * @param exitX               exit tile x
     * @param exitY               exit tile y
     * @param requiredCoins       coins needed to unlock the exit
     * @param regularCopSpawns    list of [x, y] spawn pairs for regular cops
     * @param corruptibleCopSpawn [x, y] for the corruptible cop, or null
     * @param additionalCoinSpawns extra [x, y] positions to place coins
     * @return fully constructed LevelConfig
     */
    protected LevelConfig buildConfig(
            int levelNumber, String levelName,
            int startX, int startY,
            int exitX,  int exitY,
            int requiredCoins,
            java.util.List<int[]> regularCopSpawns,
            int[]                 corruptibleCopSpawn,
            java.util.List<int[]> additionalCoinSpawns) {
        return new LevelConfig(
                levelNumber, levelName,
                startX, startY,
                exitX,  exitY,
                requiredCoins,
                regularCopSpawns,
                corruptibleCopSpawn,
                additionalCoinSpawns);
    }
    
    /**
     * Static helper that maps a level number to the correct concrete factory,
     * keeping callers decoupled from the concrete types.
     *
     * @param levelNumber 1-based level index (1–4)
     * @return the matching {@link LevelFactory} subclass instance
     * @throws IllegalArgumentException if {@code levelNumber} is outside 1–4
     */
    public static LevelFactory forLevel(int levelNumber) {
        switch (levelNumber) {
            case 1: return new CellBlockFactory();
            case 2: return new CafeteriaFactory();
            case 3: return new GeneralTimeFactory();
            case 4: return new OutdoorRecFactory();
            default:
                throw new IllegalArgumentException(
                        "No factory defined for level " + levelNumber
                        + " (valid range: 1–4)");
        }
    }
}
