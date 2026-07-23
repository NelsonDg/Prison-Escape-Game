package escape.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import escape.factories.LevelFactory;

/**
 * LevelConfig is an immutable data object (the <em>Product</em> in the
 * Factory Method pattern) that holds every value needed to set up one level
 * of the Escape game.
 *
 * It encapsulates:
 * <ul>
 *   <li>Which map file to load (via {@link #getLevelNumber()}).</li>
 *   <li>Where the player starts and where the exit is.</li>
 *   <li>How many coins the player must collect before the exit unlocks.</li>
 *   <li>Desired spawn coordinates for regular and corruptible cops.</li>
 *   <li>Any additional coin positions to place programmatically (used when
 *       the map file's 'I' tiles are in unreachable rooms).</li>
 * </ul>
 *
 * Instances are created exclusively by {@link LevelFactory} subclasses.
 * No setters are provided — the config is read-only after construction.
 */
public class LevelConfig {

    /** 1-based level index (1 = Cell Block … 4 = Outdoor Recreation). */
    private final int levelNumber;

    /** Human-readable section name shown in the HUD. */
    private final String levelName;

    /** Player starting x coordinate on this level's grid. */
    private final int startX;

    /** Player starting y coordinate on this level's grid. */
    private final int startY;

    /** x coordinate of the exit tile the player must reach. */
    private final int exitX;

    /** y coordinate of the exit tile the player must reach. */
    private final int exitY;

    /**
     * Minimum number of COIN items the player must have collected before the
     * exit is unlocked.  {@link GameEngine#checkWin()} enforces this.
     */
    private final int requiredCoins;

    /**
     * Desired [x, y] spawn positions for regular (non-bribable) cops.
     * {@link GameEngine} validates each position and adjusts if necessary.
     */
    private final List<int[]> regularCopSpawns;

    /**
     * Desired [x, y] spawn for the corruptible cop, or {@code null} if this
     * level has none.
     */
    private final int[] corruptibleCopSpawn;

    /**
     * Extra coin positions to place programmatically after the board loads.
     * Used for levels whose map 'I' tiles fall inside unreachable sealed rooms
     * (a known map-file bug in Level 4).  Empty for levels that have enough
     * reachable 'I' tiles.
     */
    private final List<int[]> additionalCoinSpawns;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Constructs a fully-specified, immutable level configuration.
     *
     * @param levelNumber         1-based level index (1–4)
     * @param levelName           human-readable section name
     * @param startX              player start x
     * @param startY              player start y
     * @param exitX               exit tile x
     * @param exitY               exit tile y
     * @param requiredCoins       coins needed to unlock the exit (≥ 0)
     * @param regularCopSpawns    list of [x, y] spawn pairs for regular cops
     * @param corruptibleCopSpawn [x, y] for the corruptible cop, or null
     * @param additionalCoinSpawns extra [x, y] positions to place coins
     */
    public LevelConfig(int levelNumber, String levelName,
                       int startX, int startY,
                       int exitX, int exitY,
                       int requiredCoins,
                       List<int[]> regularCopSpawns,
                       int[]       corruptibleCopSpawn,
                       List<int[]> additionalCoinSpawns) {

        this.levelNumber          = levelNumber;
        this.levelName            = levelName;
        this.startX               = startX;
        this.startY               = startY;
        this.exitX                = exitX;
        this.exitY                = exitY;
        this.requiredCoins        = requiredCoins;

        /* Defensive copies so external mutation cannot affect the config */
        this.regularCopSpawns     = Collections.unmodifiableList(
                new ArrayList<>(regularCopSpawns));
        this.corruptibleCopSpawn  = corruptibleCopSpawn != null
                ? corruptibleCopSpawn.clone() : null;
        this.additionalCoinSpawns = Collections.unmodifiableList(
                new ArrayList<>(additionalCoinSpawns));
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    /**
     * Returns the 1-based level number, used to load the corresponding map
     * file ({@code /level<N>.txt}).
     *
     * @return level number (1–4)
     */
    public int getLevelNumber() { return levelNumber; }

    /**
     * Returns the human-readable name of this prison section.
     *
     * @return section name, e.g. {@code "Cafeteria & Washrooms"}
     */
    public String getLevelName() { return levelName; }

    /**
     * Returns the x coordinate of the player's starting cell.
     * @return start x
     */
    public int getStartX() { return startX; }

    /**
     * Returns the y coordinate of the player's starting cell.
     * @return start y
     */
    public int getStartY() { return startY; }

    /**
     * Returns the x coordinate of this level's exit tile.
     * @return exit x
     */
    public int getExitX() { return exitX; }

    /**
     * Returns the y coordinate of this level's exit tile.
     * @return exit y
     */
    public int getExitY() { return exitY; }

    /**
     * Returns the minimum number of coins the player must collect before the
     * exit unlocks.
     *
     * @return required coin count (0 means exit is always open)
     */
    public int getRequiredCoins() { return requiredCoins; }

    /**
     * Returns an unmodifiable list of [x, y] spawn coordinates for regular
     * (non-bribable) cops.  {@link GameEngine} validates each position before
     * placing an enemy.
     *
     * @return list of cop spawn positions
     */
    public List<int[]> getRegularCopSpawns() { return regularCopSpawns; }

    /**
     * Returns the [x, y] desired spawn for the corruptible cop, or
     * {@code null} if this level has no corruptible cop.
     *
     * @return corruptible cop spawn coordinates, or null
     */
    public int[] getCorruptibleCopSpawn() {
        return corruptibleCopSpawn != null ? corruptibleCopSpawn.clone() : null;
    }

    /**
     * Returns an unmodifiable list of [x, y] positions where the engine
     * should place additional COIN items programmatically after the board
     * loads.  Useful when a level's map 'I' tiles are inside sealed rooms.
     *
     * @return extra coin spawn positions (may be empty)
     */
    public List<int[]> getAdditionalCoinSpawns() { return additionalCoinSpawns; }
}
