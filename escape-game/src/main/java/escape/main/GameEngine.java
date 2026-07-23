package escape.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import escape.board.Board;
import escape.board.Cell;
import escape.board.Clock;
import escape.entities.Cop;
import escape.entities.Player;
import escape.factories.LevelFactory;
import escape.items.Item;
import escape.items.RegularItem;
import escape.items.ShovelPart;
import escape.items.Trap;
import escape.scores.ScoreManager;

/**
 * GameEngine orchestrates the entire Escape game lifecycle across all four
 * prison sections: Cell Block, Cafeteria/Washrooms, General Time, and
 * Outdoor Recreation.
 *
 * Level configuration is provided entirely by the {@link LevelFactory}
 * hierarchy (Factory Method pattern), keeping level-specific data out of
 * this class. GameEngine only coordinates the objects; it does not
 * hard-code map coordinates or coin requirements.
 *
 * Typical usage:
 * <pre>
 *   GameEngine engine = new GameEngine();
 *   engine.startGame();
 *
 *   // each frame / key-press:
 *   engine.movePlayer(dx, dy);
 *   engine.updateTick();
 *   if (engine.checkWin() || engine.checkLose()) { ... }
 * </pre>
 */
public class GameEngine {

    // -----------------------------------------------------------------------
    // Public constants
    // -----------------------------------------------------------------------

    /** Total number of levels (prison sections) in the game. */
    public static final int TOTAL_LEVELS = 4;

    /**
     * Maximum ticks allowed per level before the player loses from time
     * running out.  One tick roughly equals one game-loop iteration.
     */
    public static final int MAX_TICKS_PER_LEVEL = 300;

    // -----------------------------------------------------------------------
    // Private constants
    // -----------------------------------------------------------------------

    /** Score bonus awarded when the player clears a level. */
    private static final int LEVEL_COMPLETE_BONUS = 100;

    /** Score penalty deducted each time the player steps on a trap. */
    private static final int TRAP_SCORE_PENALTY = 5;

    /** Score penalty deducted each time a regular cop catches the player. */
    private static final int COP_SCORE_PENALTY = 10;

    /** Item type identifier for coin collectibles. */
    private static final String ITEM_TYPE_COIN = "COIN";

    /** Item type identifier for food collectibles. */
    private static final String ITEM_TYPE_FOOD = "FOOD";

    /** Item type identifier for shovel part collectibles. */
    private static final String ITEM_TYPE_SHOVEL_PART = "SHOVEL_PART";

    // -----------------------------------------------------------------------
    // Game-state fields
    // -----------------------------------------------------------------------

    /** The level currently being played (1-based: 1–4). */
    private int currentLevel;

    /** True while the game is active and neither won nor lost. */
    private boolean gameRunning;

    /** True when the player has successfully escaped (finished Level 4). */
    private boolean gameWon;

    /** True when the player has been defeated. */
    private boolean gameLost;

    /**
     * Configuration for the level currently loaded, produced by the
     * appropriate {@link LevelFactory} subclass.  Never {@code null} while
     * the game is running.
     */
    private LevelConfig currentConfig;

    // -----------------------------------------------------------------------
    // Core game-object references
    // -----------------------------------------------------------------------

    /** The current level's board (grid of cells). */
    private Board board;

    /** The player character. */
    private Player player;

    /**
     * Tracks the player's cumulative score and per-level coin collection.
     * Coin logic lives entirely in {@link ScoreManager}; this class only
     * calls {@link ScoreManager#addCoin(int)}, {@link ScoreManager#resetCoins(int)},
     * and {@link ScoreManager#hasEnoughCoins()}.
     */
    private ScoreManager scoreManager;

    /**
     * Counts elapsed ticks within the current level.
     * Uses the {@code clock} class provided by teammates (lowercase name).
     */
    private Clock gameClock;

    /** Regular cops patrolling the current level. */
    private List<Cop> cops;

    /** Tracks the cell currently holding a bonus reward, or null if none is active. */
    private Cell activeBonusCell;

    /** Tick on which the current bonus reward expires. */
    private int bonusExpireTick;

    /** Random number generator for bonus reward spawning. */
    private final Random random = new Random();

    /** Spawn a bonus reward every this many ticks. */
    private static final int BONUS_SPAWN_INTERVAL = 20;

    /** Bonus reward disappears after this many ticks. */
    private static final int BONUS_DURATION = 15;

    /**
     * Tracks which shovel parts the player has collected across all levels.
     * Must contain "Handle", "Stick", and "Shovel Head" to unlock the
     * final exit on Level 4.
     */
    private Set<String> collectedShovelParts;

    /** Cumulative ticks across all levels (gameClock resets each level). */
    private int totalTicks;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new GameEngine in its pre-start state.
     * Call {@link #startGame()} to initialise the first level and begin play.
     */
    public GameEngine() {
        this.currentLevel         = 1;
        this.gameRunning          = false;
        this.gameWon              = false;
        this.gameLost             = false;
        this.cops                 = new ArrayList<>();
        this.scoreManager         = new ScoreManager();
        this.gameClock            = new Clock();
        this.collectedShovelParts = new HashSet<>();
    }

    // -----------------------------------------------------------------------
    // Public API – main lifecycle methods
    // -----------------------------------------------------------------------

    /**
     * Initialises and starts the game at Level 1 (Cell Block).
     *
     * Resets all state, creates a fresh {@link ScoreManager}, loads the
     * first level via {@link LevelFactory}, and places the player and enemies.
     * Must be called before the first {@link #updateTick()}.
     */
    public void startGame() {
        System.out.println("=== ESCAPE GAME STARTED ===");
        System.out.println("Objective: escape the prison across " + TOTAL_LEVELS + " sections.");
        System.out.println("Tip: find the 3 shovel parts (Handle, Stick, Shovel Head) "
                + "hidden across Levels 1–3 to unlock the final exit!\n");

        // Reset all global state (allows restart by calling startGame() again)
        this.currentLevel         = 1;
        this.gameWon              = false;
        this.gameLost             = false;
        this.gameRunning          = true;
        this.scoreManager         = new ScoreManager();
        this.collectedShovelParts = new HashSet<>();
        this.totalTicks           = 0;

        // Load level 1 via factory
        loadLevel(currentLevel);

        System.out.println("Current section: " + currentConfig.getLevelName());
        System.out.println("Collect " + currentConfig.getRequiredCoins()
                + " coin(s), then reach the exit. Good luck!\n");
    }

    /**
     * Advances the game by one tick (one frame / one time unit).
     *
     * Each tick:
     * <ol>
     *   <li>Increments the level timer.</li>
     *   <li>Updates every cop's patrol/chase movement.</li>
     *   <li>Checks whether any cop has caught the player (deals damage).</li>
     *   <li>Checks whether the player is on a trap (deals damage).</li>
     *   <li>Re-evaluates win and lose conditions.</li>
     * </ol>
     *
     * Returns immediately if the game has already ended.
     */
    public void updateTick() {
        if (!gameRunning) return;

        // Step 1 – advance level timer
        gameClock.updateTime();

        // Step 2 – move all cops
        updateCops();

        // Step 3 – cop-catch damage
        handleCopCatch();

        // Step 4 – trap damage
        handleTrapCollision();

        // Step 5 – bonus reward spawn / expiry
        updateBonusReward();

        // Step 6 – evaluate end conditions (lose checked first)
        if (checkLose()) {
            endGame(false);
        } else if (checkWin()) {
            endGame(true);
        }
    }

    /**
     * Moves the player one step in the direction (dx, dy), then automatically
     * attempts item collection at the new cell.
     *
     * The target cell must be in-bounds and not a wall ({@code !isBlocked()}).
     * {@link Player#move(int, int)} does not check walls itself, so this
     * method performs the check before delegating.
     *
     * @param dx horizontal displacement: -1 = left, +1 = right, 0 = none
     * @param dy vertical displacement:   -1 = up,   +1 = down,  0 = none
     */
    public void movePlayer(int dx, int dy) {
        if (!gameRunning) return;

        int targetX = player.getX() + dx;
        int targetY = player.getY() + dy;

        Cell targetCell = board.getCell(targetX, targetY);

        // Reject move into walls or out-of-bounds
        if (targetCell == null || targetCell.isBlocked()) {
            System.out.println("Can't move there — path is blocked.");
            return;
        }

        player.move(dx, dy);
        System.out.println("Player moved to ("
                + player.getX() + ", " + player.getY() + ").");

        // Automatically pick up any item at the new position
        handleItemCollection();
    }

    /**
     * Evaluates whether the player has satisfied the win condition.
     *
     * Win requires <em>two</em> things to be true simultaneously:
     * <ol>
     *   <li>The player is standing on the exit tile.</li>
     *   <li>{@link ScoreManager#hasEnoughCoins()} returns {@code true}.</li>
     * </ol>
     * If the player is at the exit but lacks enough coins, a message is
     * printed and the method returns {@code false} — the exit stays locked.
     *
     * For Levels 1–3 the win triggers {@link #advanceLevel()} rather than
     * ending the game.  Only the Level 4 exit constitutes a full win.
     *
     * On Level 4, the player must also have all 3 shovel parts collected
     * across the previous levels, otherwise the final exit stays locked.
     *
     * @return {@code true} if the full game has been won; {@code false} otherwise
     */
    public boolean checkWin() {
        if (!gameRunning) return gameWon;

        boolean atExit = (player.getX() == currentConfig.getExitX()
                       && player.getY() == currentConfig.getExitY());

        if (atExit) {
            // Coin gate — exit is locked until enough coins are collected
            if (!scoreManager.hasEnoughCoins()) {
                System.out.println("Exit locked! Collect "
                        + scoreManager.getCoinsRequired() + " coin(s) to proceed. "
                        + "You have " + scoreManager.getCoinsCollected() + ".");
                return false;
            }

            if (currentLevel == TOTAL_LEVELS) {
                // Shovel gate — must have all 3 parts to escape on the final level
                if (!hasFullShovel()) {
                    System.out.println("The exit is sealed — you need all 3 shovel parts!");
                    System.out.println("Collected: " + collectedShovelParts
                            + "  (" + collectedShovelParts.size() + "/3)");
                    if (!collectedShovelParts.contains("Handle"))
                        System.out.println("  Missing: Handle (Level 1)");
                    if (!collectedShovelParts.contains("Stick"))
                        System.out.println("  Missing: Stick (Level 2)");
                    if (!collectedShovelParts.contains("Shovel Head"))
                        System.out.println("  Missing: Shovel Head (Level 3)");
                    return false;
                }
                // All coins collected + full shovel = full game win
                gameWon = true;
                return true;
            } else {
                // Intermediate exit — advance to next level
                advanceLevel();
            }
        }

        return false;
    }

    /**
     * Evaluates whether the player has lost the game.
     *
     * Loss conditions:
     * <ul>
     *   <li>Player health drops to 0 or below (cops or traps).</li>
     *   <li>Level timer reaches {@link #MAX_TICKS_PER_LEVEL}.</li>
     * </ul>
     *
     * @return {@code true} if a loss condition is met; {@code false} otherwise
     */
    public boolean checkLose() {
        if (!gameRunning) return gameLost;

        // Condition 1 – health depleted
        if (player.getHealth() <= 0) {
            System.out.println("You have been caught and defeated. Game over!");
            gameLost = true;
            return true;
        }

        // Condition 2 – time expired
        if (gameClock.getTime() >= MAX_TICKS_PER_LEVEL) {
            System.out.println("Time has run out! The guards found you. Game over!");
            gameLost = true;
            return true;
        }

        return false;
    }

    // -----------------------------------------------------------------------
    // Getters – for UI, rendering, and testing
    // -----------------------------------------------------------------------

    /**
     * Returns the level currently being played (1-based).
     * @return current level number (1–4)
     */
    public int getCurrentLevel() { return currentLevel; }

    /**
     * Returns {@code true} while the game loop is active.
     * @return {@code true} if running
     */
    public boolean isGameRunning() { return gameRunning; }

    /**
     * Returns {@code true} if the player has won.
     * @return {@code true} if game was won
     */
    public boolean isGameWon() { return gameWon; }

    /**
     * Returns {@code true} if the player has lost.
     * @return {@code true} if game was lost
     */
    public boolean isGameLost() { return gameLost; }

    /**
     * Returns the current level's board.
     * @return active {@link Board}
     */
    public Board getBoard() { return board; }

    /**
     * Returns the player.
     * @return {@link Player}
     */
    public Player getPlayer() { return player; }

    /**
     * Returns the score manager.
     * @return {@link ScoreManager}
     */
    public ScoreManager getScoreManager() { return scoreManager; }

    /**
     * Returns the per-level tick counter.
     * @return {@link Clock} instance
     */
    public Clock getGameClock() { return gameClock; }

    /** Returns total ticks elapsed across all levels. */
    public int getTotalTicks() { return totalTicks + gameClock.getTime(); }

    /**
     * Returns the human-readable name of the current section, sourced from
     * {@link LevelConfig#getLevelName()}.
     *
     * @return section name, e.g. {@code "Cafeteria & Washrooms"}
     */
    public String getCurrentLevelName() {
        return currentConfig.getLevelName();
    }

    /**
     * Returns the x-coordinate of the exit tile for the current level.
     * @return exit x
     */
    public int getExitX() { return currentConfig.getExitX(); }

    /**
     * Returns the y-coordinate of the exit tile for the current level.
     * @return exit y
     */
    public int getExitY() { return currentConfig.getExitY(); }

    /**
     * Returns an unmodifiable view of the regular cops in the current level.
     * @return list of {@link Cop} instances
     */
    public List<Cop> getCops() {
        return java.util.Collections.unmodifiableList(cops);
    }

    /**
     * Returns {@code true} if the player has collected all 3 shovel parts.
     *
     * @return true when Handle, Stick, and Shovel Head are all collected
     */
    public boolean hasFullShovel() {
        return collectedShovelParts.contains("Handle")
            && collectedShovelParts.contains("Stick")
            && collectedShovelParts.contains("Shovel Head");
    }

    /**
     * Returns an unmodifiable view of collected shovel part names.
     * Used by {@link GameUI} to display shovel progress in the HUD.
     *
     * @return set of collected part names
     */
    public Set<String> getCollectedShovelParts() {
        return java.util.Collections.unmodifiableSet(collectedShovelParts);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Loads the specified level using {@link LevelFactory}.
     *
     * Steps:
     * 1. Obtain a {@link LevelConfig} from the matching factory.
     * 2. Build the {@link Board} from the map file.
     * 3. Spawn the {@link Player} at the config's start position.
     * 4. Reset the per-level clock.
     * 5. Reset the coin counter via {@link ScoreManager#resetCoins(int)}.
     * 6. Place any additional coins from the config programmatically.
     * 7. Place the shovel part for this level (Levels 1–3 only).
     * 8. Spawn and validate all enemies.
     *
     * @param levelNumber 1-based level index to load (1–4)
     */
    private void loadLevel(int levelNumber) {
        // Obtain level configuration from the appropriate factory
        this.currentConfig = LevelFactory.forLevel(levelNumber).createConfig();

        System.out.println("\n--- Loading: " + currentConfig.getLevelName()
                + " (Level " + levelNumber + ") ---");

        // Build board from map file
        this.board = new Board(levelNumber);

        // Spawn player at the factory-specified start position
        this.player = new Player(
                currentConfig.getStartX(), currentConfig.getStartY(), board);

        // Reset per-level timer
        this.gameClock = new Clock();

        // Tell ScoreManager how many coins are needed for this level's exit
        scoreManager.resetCoins(currentConfig.getRequiredCoins());

        // Place any programmatic coin spawns (e.g. Level 4 sealed-room workaround)
        placeAdditionalCoins();

        // Place the shovel part for this level (Levels 1–3 only)
        placeShovelPart(levelNumber);

        // Spawn enemies with position validation
        spawnEnemies();

        System.out.println("Start : (" + currentConfig.getStartX()
                + ", " + currentConfig.getStartY() + ")");
        System.out.println("Exit  : (" + currentConfig.getExitX()
                + ", " + currentConfig.getExitY() + ")");
        System.out.println("Coins : 0 / " + currentConfig.getRequiredCoins());
    }

    /**
     * Handles progression to the next level after the player clears an
     * intermediate exit.  Awards a level-complete score bonus, increments
     * {@link #currentLevel}, and calls {@link #loadLevel(int)}.
     */
    private void advanceLevel() {
        if (currentLevel >= TOTAL_LEVELS) return;

        System.out.println("\n*** Section complete: "
                + currentConfig.getLevelName() + " ***");

        totalTicks += gameClock.getTime();
        scoreManager.addPoints(LEVEL_COMPLETE_BONUS);
        System.out.println("Bonus: +" + LEVEL_COMPLETE_BONUS
                + " pts. Total: " + scoreManager.getScore());

        currentLevel++;
        loadLevel(currentLevel);

        System.out.println("Next section: " + currentConfig.getLevelName());
    }

    /**
     * Spawns all enemies for the current level using positions from
     * {@link #currentConfig}, validating each against the board.
     *
     * If a desired spawn position is inside a wall, {@link #findNearestWalkable}
     * moves it to the closest walkable cell automatically, so cops can never
     * spawn inside walls regardless of config values.
     */
    private void spawnEnemies() {
        cops.clear();

        // Spawn regular cops with validation
        for (int[] desired : currentConfig.getRegularCopSpawns()) {
            int[] safe = findNearestWalkable(desired[0], desired[1]);
            cops.add(new Cop(safe[0], safe[1], board));
        }

        System.out.println("Spawned " + cops.size() + " cop(s).");
    }

    /**
     * Places additional COIN items at the positions listed in
     * {@link LevelConfig#getAdditionalCoinSpawns()}.
     *
     * Used as a workaround for levels whose map 'I' tiles are inside
     * sealed rooms (currently Level 4). Each position is validated before
     * placement — walls are silently skipped.
     */
    private void placeAdditionalCoins() {
        for (int[] pos : currentConfig.getAdditionalCoinSpawns()) {
            Cell cell = board.getCell(pos[0], pos[1]);
            if (cell != null && !cell.isBlocked()) {
                cell.setItem(new Item("coin", 10, ITEM_TYPE_COIN));
                System.out.println("Placed extra coin at ("
                        + pos[0] + ", " + pos[1] + ").");
            }
        }
    }

    /**
     * Places the appropriate shovel part on the map for levels 1–3.
     *
     * <ul>
     *   <li>Level 1 → "Handle"</li>
     *   <li>Level 2 → "Stick"</li>
     *   <li>Level 3 → "Shovel Head"</li>
     *   <li>Level 4 → nothing (player must already have all 3)</li>
     * </ul>
     *
     * Change the px/py coordinates below to any open floor tile on your
     * actual maps. The method validates the cell before placing — walls are
     * silently skipped with a warning.
     *
     * @param levelNumber the level being loaded (1-based)
     */
    private static final Map<Integer, String[]> SHOVEL_PART_CONFIG = new HashMap<>();
    static {
        SHOVEL_PART_CONFIG.put(1, new String[]{"Handle",      "15", "9"});
        SHOVEL_PART_CONFIG.put(2, new String[]{"Stick",       "25", "8"});
        SHOVEL_PART_CONFIG.put(3, new String[]{"Shovel Head", "35", "18"});
    }

    private void placeShovelPart(int levelNumber) {
        String[] config = SHOVEL_PART_CONFIG.get(levelNumber);
        if (config == null) return; // Level 4 has no shovel part to place

        String partName = config[0];
        int px = Integer.parseInt(config[1]);
        int py = Integer.parseInt(config[2]);

        Cell cell = board.getCell(px, py);
        if (cell != null && !cell.isBlocked()) {
            cell.setItem(new ShovelPart(partName));
            System.out.println("Placed shovel part [" + partName
                    + "] at (" + px + ", " + py + ").");
        } else {
            System.out.println("WARNING: Could not place shovel part ["
                    + partName + "] at (" + px + ", " + py
                    + ") — cell is blocked or out of bounds. "
                    + "Update the coordinates in placeShovelPart().");
        }
    }

    /**
     * Calls {@link Cop#move(Player)} on every cop so they patrol or chase
     * the player during this tick.
     */
    private void updateCops() {
        for (Cop cop : cops) {
            cop.move(player);
        }
    }

    private void updateBonusReward() {
        int tick = gameClock.getTime();

        // Expire the active bonus if its time is up
        if (activeBonusCell != null && tick >= bonusExpireTick) {
            if (activeBonusCell.hasItem()
                    && "FOOD".equals(activeBonusCell.getItem().getType())) {
                activeBonusCell.removeItem();
            }
            activeBonusCell = null;
        }

        // Spawn a new bonus every BONUS_SPAWN_INTERVAL ticks if none is active
        if (activeBonusCell == null && tick > 0 && tick % BONUS_SPAWN_INTERVAL == 0) {
            Cell cell = findRandomFloorCell();
            if (cell != null) {
                cell.setItem(new escape.items.BonusRewards("food", 10, "FOOD", player));
                activeBonusCell  = cell;
                bonusExpireTick  = tick + BONUS_DURATION;
                System.out.println("Bonus reward spawned at ("
                        + cell.getX() + ", " + cell.getY()
                        + ") — expires at tick " + bonusExpireTick);
            }
        }
    }

    private Cell findRandomFloorCell() {
        int w = board.getBoardWidth();
        int h = board.getBoardHeight();
        for (int attempts = 0; attempts < 100; attempts++) {
            int x = random.nextInt(w);
            int y = random.nextInt(h);
            Cell c = board.getCell(x, y);
            if (c != null && !c.isBlocked() && !c.hasItem()
                    && !(x == player.getX() && y == player.getY())) {
                return c;
            }
        }
        return null;
    }

    /**
     * Checks whether any cop is adjacent to the player (Manhattan distance ≤ 1).
     * If so, {@link Cop#dealDamage(Player)} is called and a score penalty applied.
     */
    private void handleCopCatch() {
        for (Cop cop : cops) {
            int dist = manhattanDistance(cop.getX(), cop.getY());
            if (dist <= 1) {
                System.out.println("A cop caught you!");
                cop.dealDamage(player);
                scoreManager.subtractPoints(COP_SCORE_PENALTY);
            }
        }

    }

    /**
     * Checks whether the player's current cell contains a {@link Trap}.
     * If so, applies health damage via {@link Player#setHealth(int)} and
     * a score penalty via {@link ScoreManager#subtractPoints(int)}.
     */
    private void handleTrapCollision() {
        Cell currentCell = board.getCell(player.getX(), player.getY());
        if (currentCell == null) return;

        Trap trap = currentCell.getTrap();
        if (trap != null) {
            int penalty = trap.getPenalty();
            System.out.println("You stepped on a trap! -" + penalty
                    + " health, -" + TRAP_SCORE_PENALTY + " score.");
            player.takeDamage(penalty);
            scoreManager.subtractPoints(TRAP_SCORE_PENALTY);
        }
    }

    /**
     * Checks whether the player's current cell contains an {@link Item}.
     * If so, removes it from the board and processes it:
     * <ul>
     *   <li><b>COIN</b> items are counted via {@link ScoreManager#addCoin(int)},
     *       which also adds their value to the score.</li>
     *   <li><b>SHOVEL_PART</b> items are added to the shovel parts set.</li>
     *   <li><b>RegularItem</b> instances apply their own effect.</li>
     *   <li>Other items (e.g. BRIBE, ESCAPEKEY) are stored in inventory only.</li>
     * </ul>
     */
    private void handleItemCollection() {
        Item collected = board.collectItemAt(player.getX(), player.getY());
        if (collected == null) return;

        player.viewItems().add(collected);
        System.out.println("Picked up: " + collected.getType()
                + " (value: " + collected.getValue() + ")");

        if (ITEM_TYPE_COIN.equals(collected.getType())) {
            handleCoinPickup(collected);
        } else if (ITEM_TYPE_FOOD.equals(collected.getType())) {
            handleFoodPickup();
        } else if (ITEM_TYPE_SHOVEL_PART.equals(collected.getType())) {
            handleShovelPartPickup(collected);
        } else if (collected instanceof RegularItem) {
            handleRegularItemPickup(collected);
        }
    }

    private void handleCoinPickup(Item coin) {
        scoreManager.addCoin(coin.getValue());
        System.out.println("Coins: " + scoreManager.getCoinsCollected()
                + " / " + scoreManager.getCoinsRequired()
                + "   Score: " + scoreManager.getScore());
    }

    private void handleFoodPickup() {
        player.heal(20);
    }

    private void handleShovelPartPickup(Item item) {
        ShovelPart part = (ShovelPart) item;
        collectedShovelParts.add(part.getPartName());
        System.out.println("Shovel part collected: " + part.getPartName()
                + "  (" + collectedShovelParts.size() + "/3)");
        System.out.println("Parts so far: " + collectedShovelParts);
    }

    private void handleRegularItemPickup(Item item) {
        ((RegularItem) item).applyEffect();
        System.out.println("Score: " + scoreManager.getScore());
    }

    /**
     * Terminates the game, prints a summary, and marks the game as won or lost.
     *
     * @param won {@code true} if the player escaped; {@code false} if defeated
     */
    private void endGame(boolean won) {
        gameRunning = false;
        gameWon     = won;
        gameLost    = !won;

        System.out.println("\n=============================");
        if (won) {
            System.out.println("  YOU ESCAPED! CONGRATULATIONS!");
        } else {
            System.out.println("  GAME OVER. Better luck next time.");
        }
        System.out.println("  Final Score  : " + scoreManager.getScore());
        System.out.println("  Sections Done: "
                + (currentLevel - (won ? 0 : 1)) + " / " + TOTAL_LEVELS);
        System.out.println("  Ticks Used   : " + gameClock.getTime());
        System.out.println("  Shovel Parts : " + collectedShovelParts.size() + "/3 "
                + collectedShovelParts);
        System.out.println("=============================\n");
    }

    /**
     * Finds the nearest walkable (non-wall) cell to the desired position using
     * BFS. Used by {@link #spawnEnemies()} to guarantee no entity ever spawns
     * inside a wall tile.
     *
     * If {@code (desiredX, desiredY)} is already walkable it is returned
     * immediately. Otherwise the search expands outward one step at a time
     * until a floor cell is found.
     *
     * @param desiredX preferred x coordinate
     * @param desiredY preferred y coordinate
     * @return the nearest walkable [x, y], or the original position as a
     *         fallback if the board has no reachable floor cells
     */
    private int[] findNearestWalkable(int desiredX, int desiredY) {
        // Fast path: desired position is already walkable
        Cell cell = board.getCell(desiredX, desiredY);
        if (cell != null && !cell.isBlocked()) {
            return new int[]{desiredX, desiredY};
        }

        // BFS outward from the desired position
        Queue<int[]>  queue   = new LinkedList<>();
        Set<String>   visited = new HashSet<>();
        int[][]       dirs    = {{1,0},{-1,0},{0,1},{0,-1}};

        queue.add(new int[]{desiredX, desiredY});
        visited.add(desiredX + "," + desiredY);

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            for (int[] d : dirs) {
                int nx = pos[0] + d[0];
                int ny = pos[1] + d[1];
                String key = nx + "," + ny;
                if (visited.contains(key)) continue;
                visited.add(key);

                Cell c = board.getCell(nx, ny);
                if (c == null) continue;      // out of bounds
                if (!c.isBlocked()) {
                    System.out.println("  Spawn adjusted ("
                            + desiredX + "," + desiredY + ") -> ("
                            + nx + "," + ny + ")");
                    return new int[]{nx, ny};
                }
                queue.add(new int[]{nx, ny}); // keep expanding through walls
            }
        }

        // Fallback: return original (should never happen on a valid map)
        return new int[]{desiredX, desiredY};
    }

    /**
     * Returns the Manhattan distance between the player and a given entity.
     *
     * @param entityX the x-coordinate of the entity
     * @param entityY the y-coordinate of the entity
     * @return the Manhattan distance
     */
    private int manhattanDistance(int entityX, int entityY) {
        return Math.abs(player.getX() - entityX) + Math.abs(player.getY() - entityY);
    }
}