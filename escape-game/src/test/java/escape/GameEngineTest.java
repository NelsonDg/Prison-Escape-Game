package escape;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import escape.board.Board;
import escape.board.Clock;
import escape.entities.Player;
import escape.factories.CellBlockFactory;
import escape.main.GameEngine;
import escape.scores.ScoreManager;

/**
 * Unit tests for {@link GameEngine}.
 *
 * <p>These tests cover the four primary responsibilities of the GameEngine:
 * <ul>
 *   <li>{@link GameEngine#startGame()} – correct initialisation</li>
 *   <li>{@link GameEngine#updateTick()} – clock advances, game reacts</li>
 *   <li>{@link GameEngine#checkWin()} – win condition detection</li>
 *   <li>{@link GameEngine#checkLose()} – loss condition detection</li>
 * </ul>
 * </p>
 *
 * <p>All tests run against real {@link Board} instances loaded from the
 * resource map files (no mocking), ensuring integration with the file-based
 * level system works end-to-end.</p>
 */
class GameEngineTest {

    /** Fresh engine created before each test. */
    private GameEngine engine;

    /**
     * Creates a new {@link GameEngine} and calls {@link GameEngine#startGame()}
     * so every test starts from a clean, running state at Level 1.
     */
    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        engine.startGame();
    }

    // -----------------------------------------------------------------------
    // startGame() tests
    // -----------------------------------------------------------------------

    /**
     * After {@link GameEngine#startGame()}, the engine must be in a running
     * state at Level 1 with default score and a live player.
     */
    @Test
    void startGame_initialStateIsCorrect() {
        assertTrue(engine.isGameRunning(),    "Game should be running after startGame()");
        assertFalse(engine.isGameWon(),       "Game should not be won at start");
        assertFalse(engine.isGameLost(),      "Game should not be lost at start");
        assertEquals(1, engine.getCurrentLevel(), "Should start at Level 1");
        assertEquals(0, engine.getScoreManager().getScore(), "Score should be 0 at start");
    }

    /**
     * The player must be alive and positioned at the Level 1 start tile (3, 3)
     * immediately after {@link GameEngine#startGame()}.
     */
    @Test
    void startGame_playerStartsAtCorrectPosition() {
        Player player = engine.getPlayer();
        assertNotNull(player, "Player must not be null after startGame()");
        assertEquals(3, player.getX(), "Player start X should be 3");
        assertEquals(3, player.getY(), "Player start Y should be 3");
        assertEquals(100, player.getHealth(), "Player should start with 100 health");
    }

    /**
     * The game clock must start at 0 ticks when a new level is loaded.
     */
    @Test
    void startGame_clockStartsAtZero() {
        assertEquals(0, engine.getGameClock().getTime(),
                "Clock must be 0 at the start of a level");
    }

    /**
     * {@link GameEngine#startGame()} must set the level name to "Cell Block".
     */
    @Test
    void startGame_levelNameIsCellBlock() {
        assertEquals("Cell Block", engine.getCurrentLevelName(),
                "Level 1 section must be Cell Block");
    }

    /**
     * Calling {@link GameEngine#startGame()} a second time must reset the
     * game fully — score back to 0, level back to 1, and running again.
     */
    @Test
    void startGame_canRestartGame() {
        // Simulate some progress
        engine.getScoreManager().addPoints(500);
        // Restart
        engine.startGame();

        assertEquals(1, engine.getCurrentLevel(), "Level must reset to 1 on restart");
        assertEquals(0, engine.getScoreManager().getScore(), "Score must reset to 0 on restart");
        assertTrue(engine.isGameRunning(), "Game must be running after restart");
    }

    // -----------------------------------------------------------------------
    // updateTick() tests
    // -----------------------------------------------------------------------

    /**
     * Each call to {@link GameEngine#updateTick()} must advance the game clock
     * by exactly one tick.
     */
    @Test
    void updateTick_advancesClockByOne() {
        engine.updateTick();
        assertEquals(1, engine.getGameClock().getTime(), "Clock should be 1 after one tick");

        engine.updateTick();
        assertEquals(2, engine.getGameClock().getTime(), "Clock should be 2 after two ticks");
    }

    /**
     * Calling {@link GameEngine#updateTick()} after the game has ended must
     * be a no-op — the clock must not increment past game-over.
     */
    @Test
    void updateTick_doesNothingAfterGameEnds() {
        // Force game over by draining health
        engine.getPlayer().takeDamage(100); // sets health = 0 (subtracts 100)
        engine.updateTick(); // this tick detects the loss and stops the game

        int clockAfterEnd = engine.getGameClock().getTime();
        engine.updateTick(); // second tick — should be a no-op
        assertEquals(clockAfterEnd, engine.getGameClock().getTime(),
                "Clock must not advance after game has ended");
    }

    /**
     * After {@link #MAX_TICKS} ticks, {@link GameEngine#checkLose()} must
     * return {@code true} and the game must stop running.
     */
    @Test
    void updateTick_triggersLossWhenTimeExpires() {
        /* Tick up to the limit.  We use the public constant so the test stays
           in sync if the constant changes. */
        for (int i = 0; i < GameEngine.MAX_TICKS_PER_LEVEL; i++) {
            if (!engine.isGameRunning()) break;
            engine.updateTick();
        }

        assertFalse(engine.isGameRunning(), "Game must stop when time runs out");
        assertTrue(engine.isGameLost(),     "Game must be flagged as lost on timeout");
        assertFalse(engine.isGameWon(),     "Game must not be won on timeout");
    }

    // -----------------------------------------------------------------------
    // checkLose() tests
    // -----------------------------------------------------------------------

    /**
     * {@link GameEngine#checkLose()} must return {@code false} at the start
     * of the game when neither loss condition is met.
     */
    @Test
    void checkLose_returnsFalseAtStart() {
        assertFalse(engine.checkLose(),
                "checkLose() should be false at game start");
    }

    /**
     * {@link GameEngine#checkLose()} must return {@code true} when the
     * player's health drops to 0.
     */
    @Test
    void checkLose_returnsTrueWhenPlayerHealthIsZero() {
        /* setHealth(int damage) subtracts damage from health.
           Applying 100 damage to a player with 100 health yields 0. */
        engine.getPlayer().takeDamage(100);
        assertTrue(engine.checkLose(),
                "checkLose() should be true when player health reaches 0");
    }

    /**
     * {@link GameEngine#checkLose()} must return {@code true} when the level
     * clock reaches or exceeds {@link GameEngine#MAX_TICKS_PER_LEVEL}.
     */
    @Test
    void checkLose_returnsTrueWhenTimeExpires() {
        /* Manually advance the clock to the limit without triggering
           updateTick()'s full event pipeline. */
        Clock c = engine.getGameClock();
        for (int i = 0; i < GameEngine.MAX_TICKS_PER_LEVEL; i++) {
            c.updateTime();
        }
        assertTrue(engine.checkLose(),
                "checkLose() should be true when time reaches MAX_TICKS_PER_LEVEL");
    }

    // -----------------------------------------------------------------------
    // checkWin() tests
    // -----------------------------------------------------------------------

    /**
     * {@link GameEngine#checkWin()} must return {@code false} at the start of
     * the game when the player has not yet reached any exit.
     */
    @Test
    void checkWin_returnsFalseAtStart() {
        assertFalse(engine.checkWin(),
                "checkWin() should be false at game start");
    }

    // -----------------------------------------------------------------------
    // movePlayer() tests
    // -----------------------------------------------------------------------

    /**
     * Moving into a wall (coordinate that maps to a blocked cell) must be
     * rejected — the player's position must not change.
     */
    @Test
    void movePlayer_doesNotMoveIntoWall() {
        /* Player starts at (3,3).  The cell at (5,3) is a '#' wall.
           We teleport the player to (4,3) using Player.move() — which
           bypasses the engine's wall check — so that (5,3) is the very
           next cell to the right, then verify engine.movePlayer() blocks it. */
        engine.getPlayer().move(1, 0); // (3,3) → (4,3)  [no wall check]
        int startX = engine.getPlayer().getX(); // 4
        int startY = engine.getPlayer().getY(); // 3

        engine.movePlayer(1, 0); // attempt to move right into (5,3) = '#'

        assertEquals(startX, engine.getPlayer().getX(),
                "Player X must not change when moving into a wall");
        assertEquals(startY, engine.getPlayer().getY(),
                "Player Y must not change when moving into a wall");
    }

    /**
     * Moving into a valid floor cell must update the player's coordinates.
     */
    @Test
    void movePlayer_movesOntoFloorCell() {
        /* Player starts at (3,3).  Moving right (dx=+1) targets (4,3)
           which is a '.' floor tile in Level 1. */
        int startY = engine.getPlayer().getY();

        engine.movePlayer(1, 0); // move right

        assertEquals(4, engine.getPlayer().getX(),
                "Player X should be 4 after moving right from (3,3)");
        assertEquals(startY, engine.getPlayer().getY(),
                "Player Y must not change on a horizontal move");
    }

    /**
     * {@link GameEngine#movePlayer(int, int)} must be a no-op after the game
     * has ended.
     */
    @Test
    void movePlayer_doesNothingAfterGameEnds() {
        // End the game
        engine.getPlayer().takeDamage(100); // zero out health
        engine.updateTick();               // triggers game over

        int x = engine.getPlayer().getX();
        int y = engine.getPlayer().getY();

        engine.movePlayer(1, 0);           // should be ignored

        assertEquals(x, engine.getPlayer().getX(),
                "Player X must not change after game ends");
        assertEquals(y, engine.getPlayer().getY(),
                "Player Y must not change after game ends");
    }

    // -----------------------------------------------------------------------
    // ScoreManager integration tests
    // -----------------------------------------------------------------------

    /**
     * The {@link ScoreManager} returned by the engine must not be {@code null}
     * after {@link GameEngine#startGame()}.
     */
    @Test
    void scoreManager_isNotNullAfterStart() {
        assertNotNull(engine.getScoreManager(),
                "ScoreManager must not be null after startGame()");
    }

    /**
     * Score must start at exactly 0 at the beginning of the game.
     */
    @Test
    void scoreManager_startsAtZero() {
        assertEquals(0, engine.getScoreManager().getScore(),
                "Initial score must be 0");
    }

    // -----------------------------------------------------------------------
    // General state tests
    // -----------------------------------------------------------------------

    /**
     * The {@link Board} must not be {@code null} after the engine starts.
     */
    @Test
    void getBoard_isNotNullAfterStart() {
        assertNotNull(engine.getBoard(),
                "Board must not be null after startGame()");
    }

    /**
     * The level count constant must equal 4, matching the four prison
     * sections defined in the project spec.
     */
    @Test
    void totalLevels_isFour() {
        assertEquals(4, GameEngine.TOTAL_LEVELS,
                "The game must have exactly 4 levels");
    }

    // -----------------------------------------------------------------------
    // Coin system tests (ScoreManager integration)
    // -----------------------------------------------------------------------

    /**
     * After {@link GameEngine#startGame()}, Level 1 (Cell Block) is loaded
     * from {@link CellBlockFactory} which requires exactly 1 coin.
     * {@link ScoreManager#getCoinsRequired()} must reflect this.
     */
    @Test
    void coins_requiredIsOneOnLevel1() {
        assertEquals(1, engine.getScoreManager().getCoinsRequired(),
                "Level 1 (Cell Block) must require exactly 1 coin");
    }

    /**
     * No coins have been collected at the very start of Level 1.
     */
    @Test
    void coins_collectedStartsAtZero() {
        assertEquals(0, engine.getScoreManager().getCoinsCollected(),
                "Coins collected must be 0 at game start");
    }

    /**
     * Before any coins are collected, {@link ScoreManager#hasEnoughCoins()}
     * must return {@code false} because 0 &lt; 1 (the Level 1 requirement).
     */
    @Test
    void coins_hasEnoughCoins_falseBeforeCollecting() {
        assertFalse(engine.getScoreManager().hasEnoughCoins(),
                "hasEnoughCoins() must be false at game start (0 of 1 collected)");
    }

    /**
     * After calling {@link ScoreManager#addCoin(int)}, the collected count
     * must increment by 1 and the coin's value must be added to the score.
     */
    @Test
    void coins_addCoin_incrementsCollectedAndScore() {
        ScoreManager sm = engine.getScoreManager();
        int scoreBefore = sm.getScore();

        sm.addCoin(10);

        assertEquals(1, sm.getCoinsCollected(),
                "Collected count must be 1 after one addCoin() call");
        assertEquals(scoreBefore + 10, sm.getScore(),
                "Score must increase by the coin's value");
    }

    /**
     * After collecting the required number of coins,
     * {@link ScoreManager#hasEnoughCoins()} must return {@code true}.
     */
    @Test
    void coins_hasEnoughCoins_trueAfterCollectingRequired() {
        ScoreManager sm = engine.getScoreManager();
        // Level 1 requires 1 coin — collect it
        sm.addCoin(10);

        assertTrue(sm.hasEnoughCoins(),
                "hasEnoughCoins() must be true after collecting the required coins");
    }

    /**
     * {@link ScoreManager#resetCoins(int)} must clear the collected counter
     * and update the requirement — as happens when advancing between levels.
     */
    @Test
    void coins_resetCoins_clearsCounterAndSetsRequirement() {
        ScoreManager sm = engine.getScoreManager();
        sm.addCoin(10);                  // collect one coin
        sm.resetCoins(3);               // simulate level transition

        assertEquals(0, sm.getCoinsCollected(),
                "Collected count must reset to 0 after resetCoins()");
        assertEquals(3, sm.getCoinsRequired(),
                "Required count must be updated to the new level's requirement");
    }

    /**
     * {@link GameEngine#checkWin()} must return {@code false} when the player
     * reaches the exit tile but has not yet collected enough coins.
     *
     * <p>The player is teleported directly to the Level 1 exit (5, 5) using
     * {@link Player#move(int, int)}, bypassing wall checks, to isolate the
     * coin-gate logic from pathfinding concerns.</p>
     */
    @Test
    void checkWin_returnsFalseAtExitWithoutEnoughCoins() {
        // Teleport player from start (1,1) to Level 1 exit (5,5)
        // Player.move(dx, dy) adds displacement without a wall check
        Player player = engine.getPlayer();
        int exitX = engine.getExitX(); // 5
        int exitY = engine.getExitY(); // 5
        player.move(exitX - player.getX(), exitY - player.getY());

        // Sanity check: player is now at exit
        assertEquals(exitX, player.getX(), "Player must be at exit X");
        assertEquals(exitY, player.getY(), "Player must be at exit Y");

        // Coin requirement not yet met (0 of 1) — exit must stay locked
        assertFalse(engine.getScoreManager().hasEnoughCoins(),
                "Precondition: coins not yet satisfied");
        assertFalse(engine.checkWin(),
                "checkWin() must be false at exit when coins are insufficient");
    }

    /**
     * {@link GameEngine#checkWin()} must advance to Level 2 when the player
     * reaches the Level 1 exit after collecting enough coins.
     *
     * <p>After a successful intermediate exit, the engine is on Level 2 and
     * the game is still running (not yet won — that only happens at Level 4).</p>
     */
    @Test
    void checkWin_advancesLevelWhenAtExitWithEnoughCoins() {
        // Collect the 1 required coin for Level 1
        engine.getScoreManager().addCoin(10);

        // Teleport player to Level 1 exit (5, 5)
        Player player = engine.getPlayer();
        player.move(engine.getExitX() - player.getX(),
                    engine.getExitY() - player.getY());

        // Trigger checkWin — should advance to Level 2 internally
        engine.checkWin();

        assertEquals(2, engine.getCurrentLevel(),
                "Engine must advance to Level 2 after clearing Level 1 exit with coins");
        assertTrue(engine.isGameRunning(),
                "Game must still be running after an intermediate level advance");
    }
}
