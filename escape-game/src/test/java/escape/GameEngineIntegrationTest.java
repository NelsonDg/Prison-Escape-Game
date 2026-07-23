package escape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import escape.board.Cell;
import escape.entities.Cop;
import escape.entities.Player;
import escape.items.Item;
import escape.items.Trap;
import escape.main.GameEngine;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link GameEngine}.
 *
 * <p>These tests exercise interactions between multiple components — Board,
 * Player, ScoreManager, Cop, and Trap — through the GameEngine's public API,
 * rather than mocking or testing each class in isolation.</p>
 *
 * <p>Level 1 facts (used throughout):
 * <ul>
 *   <li>Map size: 47 × 21</li>
 *   <li>Player start: (3, 3)</li>
 *   <li>Exit: (37, 20) — requires 1 coin</li>
 *   <li>Cop spawn: (45, 19)</li>
 * </ul>
 * Level 2 facts:
 * <ul>
 *   <li>Map size: 53 × 27</li>
 * </ul>
 * </p>
 */
class GameEngineIntegrationTest {

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        engine.startGame();
    }

    // -----------------------------------------------------------------------
    // Board ↔ GameEngine
    // -----------------------------------------------------------------------

    /**
     * After startGame() the Level 1 board must be 47 columns × 21 rows,
     * matching the level1.txt map file.
     */
    @Test
    void boardLoading_level1HasCorrectDimensions() {
        assertEquals(47, engine.getBoard().getBoardWidth(),  "Level 1 width must be 47");
        assertEquals(21, engine.getBoard().getBoardHeight(), "Level 1 height must be 21");
    }

    /**
     * The player's start cell (3, 3) must be a walkable floor tile —
     * not a wall — so the player can move freely from the start.
     */
    @Test
    void boardLoading_playerStartsOnWalkableCell() {
        Player player = engine.getPlayer();
        Cell start = engine.getBoard().getCell(player.getX(), player.getY());
        assertNotNull(start, "Start cell must exist");
        assertFalse(start.isBlocked(), "Start cell must not be a wall");
    }

    /**
     * The Level 1 exit cell (37, 20) must be a walkable floor tile so the
     * player can step on it to trigger the win condition.
     */
    @Test
    void boardLoading_exitCellIsWalkable() {
        Cell exit = engine.getBoard().getCell(engine.getExitX(), engine.getExitY());
        assertNotNull(exit, "Exit cell must exist on the board");
        assertFalse(exit.isBlocked(), "Exit cell must not be a wall");
    }

    // -----------------------------------------------------------------------
    // Item collection ↔ ScoreManager
    // -----------------------------------------------------------------------

    /**
     * Moving the player onto a COIN cell must:
     * <ul>
     *   <li>Remove the coin from the board.</li>
     *   <li>Increment the coin counter by 1.</li>
     *   <li>Add the coin's value to the score.</li>
     * </ul>
     * Tests the pipeline: movePlayer → handleItemCollection → ScoreManager.addCoin.
     */
    @Test
    void itemCollection_coinUpdatesScoreAndCounter() {
        // Place a coin on the floor cell directly to the right of the player (4,3)
        Cell target = engine.getBoard().getCell(4, 3);
        assertNotNull(target, "Cell (4,3) must exist");
        assertFalse(target.isBlocked(), "Cell (4,3) must be walkable");
        target.setItem(new Item("coin", 10, "COIN"));

        int scoreBefore = engine.getScoreManager().getScore();
        int coinsBefore = engine.getScoreManager().getCoinsCollected();

        engine.movePlayer(1, 0); // (3,3) → (4,3), coin collected

        assertEquals(coinsBefore + 1, engine.getScoreManager().getCoinsCollected(),
                "Coin counter must increment by 1 after collecting a coin");
        assertTrue(engine.getScoreManager().getScore() > scoreBefore,
                "Score must increase after collecting a coin");
        assertNull(engine.getBoard().getCell(4, 3).getItem(),
                "Coin must be removed from the board after collection");
    }

    /**
     * Moving onto a FOOD cell must increase the player's health by 20,
     * capped at 100.
     * Tests the pipeline: movePlayer → handleItemCollection → Player.heal(20).
     */
    @Test
    void itemCollection_foodHealsPlayer() {
        Player player = engine.getPlayer();
        player.takeDamage(40); // subtracts 40 → health = 60
        assertEquals(60, player.getHealth(), "Precondition: player at 60 HP");

        Cell target = engine.getBoard().getCell(4, 3);
        assertNotNull(target);
        target.setItem(new Item("food", 10, "FOOD"));

        engine.movePlayer(1, 0); // collect food at (4,3)

        assertEquals(80, player.getHealth(),
                "Health must increase by 20 after eating food (60 + 20 = 80)");
    }

    /**
     * Collecting food at full health must leave health unchanged at 100
     * (Player.heal is capped at 100).
     */
    @Test
    void itemCollection_foodDoesNotExceedMaxHealth() {
        assertEquals(100, engine.getPlayer().getHealth(), "Player starts at 100 HP");

        Cell target = engine.getBoard().getCell(4, 3);
        assertNotNull(target);
        target.setItem(new Item("food", 10, "FOOD"));

        engine.movePlayer(1, 0);

        assertEquals(100, engine.getPlayer().getHealth(),
                "Health must not exceed 100 after eating food at full HP");
    }

    // -----------------------------------------------------------------------
    // Cop ↔ Player health
    // -----------------------------------------------------------------------

    /**
     * When the player is on the same cell as a cop, handleCopCatch must
     * trigger and deal 10 damage on the next tick.
     * Tests the pipeline: updateTick → handleCopCatch → Cop.dealDamage → Player.setHealth.
     */
    @Test
    void copCatch_adjacentCopDamagesPlayerOnTick() {
        Player player = engine.getPlayer();
        int hpBefore = player.getHealth();

        // Level 1 cop spawns at (45, 19). Teleport player there.
        // Player.move() does a bounds check but no wall check, so this is safe.
        Cop cop = engine.getCops().get(0);
        player.move(cop.getX() - player.getX(), cop.getY() - player.getY());

        engine.updateTick(); // handleCopCatch fires → cop.dealDamage → -10 HP

        assertTrue(engine.getPlayer().getHealth() < hpBefore,
                "Player HP must decrease when standing on a cop cell after updateTick()");
    }

    /**
     * Repeated cop damage must eventually kill the player and end the game as lost.
     * Tests: cop damage → health ≤ 0 → checkLose → endGame(false).
     */
    @Test
    void copCatch_repeatedDamageKillsPlayer() {
        Player player = engine.getPlayer();

        // Set health so a single cop hit (10 damage) finishes the player.
        // setHealth(n) subtracts n from health, so setHealth(90) → 100-90=10 HP.
        player.takeDamage(90);
        assertEquals(10, player.getHealth(), "Precondition: player at 10 HP");

        // Teleport player onto the cop
        Cop cop = engine.getCops().get(0);
        player.move(cop.getX() - player.getX(), cop.getY() - player.getY());

        engine.updateTick(); // 10 HP - 10 damage = 0 → checkLose → endGame

        assertTrue(engine.isGameLost(),    "Game must be lost when player HP reaches 0");
        assertFalse(engine.isGameRunning(), "Game must stop running after player dies");
    }

    // -----------------------------------------------------------------------
    // Level progression
    // -----------------------------------------------------------------------

    /**
     * After the player satisfies the coin requirement and steps on the Level 1
     * exit, checkWin() must:
     * <ul>
     *   <li>Advance currentLevel from 1 to 2.</li>
     *   <li>Load the Level 2 board (53 × 27).</li>
     *   <li>Reset the coin counter to 0.</li>
     *   <li>Keep the game running.</li>
     * </ul>
     */
    @Test
    void levelProgression_advancingToLevel2LoadsNewBoard() {
        // Satisfy Level 1 coin gate (requires 1 coin)
        engine.getScoreManager().addCoin(10);

        // Teleport player to Level 1 exit (37, 20)
        Player player = engine.getPlayer();
        player.move(engine.getExitX() - player.getX(),
                    engine.getExitY() - player.getY());

        engine.checkWin(); // triggers advanceLevel() internally

        assertEquals(2, engine.getCurrentLevel(),
                "Must be on Level 2 after clearing Level 1");
        assertEquals(53, engine.getBoard().getBoardWidth(),
                "Level 2 board width must be 53");
        assertEquals(27, engine.getBoard().getBoardHeight(),
                "Level 2 board height must be 27");
        assertEquals(0, engine.getScoreManager().getCoinsCollected(),
                "Coin counter must reset to 0 on level advance");
        assertTrue(engine.isGameRunning(),
                "Game must still be running after an intermediate level advance");
    }

    /**
     * The level-complete score bonus (100 pts) must be applied when the player
     * clears Level 1 and advances to Level 2.
     */
    @Test
    void levelProgression_scoreIncreasesOnLevelComplete() {
        engine.getScoreManager().addCoin(10);
        int scoreBefore = engine.getScoreManager().getScore();

        Player player = engine.getPlayer();
        player.move(engine.getExitX() - player.getX(),
                    engine.getExitY() - player.getY());
        engine.checkWin();

        assertTrue(engine.getScoreManager().getScore() > scoreBefore,
                "Score must increase by the level-complete bonus when clearing a level");
    }

    // -----------------------------------------------------------------------
    // Trap ↔ Player / ScoreManager
    // -----------------------------------------------------------------------

    /**
     * When the player stands on a cell with a trap, updateTick() must:
     * <ul>
     *   <li>Reduce player health by the trap's penalty.</li>
     *   <li>Reduce the score by the trap score penalty (5 pts).</li>
     * </ul>
     * Tests: updateTick → handleTrapCollision → Player.setHealth + ScoreManager.subtractPoints.
     */
    @Test
    void trapCollision_reducesHealthAndScoreOnTick() {
        // Add score so the subtraction is visible
        engine.getScoreManager().addPoints(50);
        int hpBefore    = engine.getPlayer().getHealth();
        int scoreBefore = engine.getScoreManager().getScore();

        // Place a trap on the player's current cell
        Cell playerCell = engine.getBoard().getCell(
                engine.getPlayer().getX(), engine.getPlayer().getY());
        assertNotNull(playerCell);
        playerCell.setTrap(new Trap(10));

        engine.updateTick();

        assertTrue(engine.getPlayer().getHealth() < hpBefore,
                "Player health must decrease after standing on a trap");
        assertTrue(engine.getScoreManager().getScore() < scoreBefore,
                "Score must decrease after the trap penalty is applied");
    }
}
