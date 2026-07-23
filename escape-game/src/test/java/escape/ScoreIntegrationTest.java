package escape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import escape.board.Clock;
import escape.items.RegularItem;
import escape.items.ShovelPart;
import escape.scores.ScoreManager;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Integration tests for the escape-room components.
 *
 * <p>Unlike the unit tests, which verify each class in isolation, these tests
 * exercise the interactions between two or more components to confirm that they
 * collaborate correctly under realistic game scenarios.</p>
 *
 * <p>Interactions covered:</p>
 * <ol>
 *   <li>{@link RegularItem} ↔ {@link ScoreManager} — collecting items
 *       modifies the score as expected</li>
 *   <li>{@link Clock} ↔ {@link ScoreManager} — time-based score-penalty
 *       simulation</li>
 *   <li>{@link ShovelPart} collection logic — all three parts tracked in
 *       an inventory list</li>
 *   <li>Multi-item pickup sequence — realistic single-level play scenario
 *       combining coins, traps, and keys</li>
 *   <li>Coin gate / win condition — {@link ScoreManager#hasEnoughCoins()}
 *       driving level completion across multiple levels</li>
 * </ol>
 */
public class ScoreIntegrationTest {

    /**
     * Shared {@link ScoreManager} instance recreated before each test to
     * prevent score state from leaking between tests.
     */
    private ScoreManager scoreManager;

    /**
     * Creates a fresh, zero-initialised {@link ScoreManager} before every
     * test method runs.
     */
    @BeforeEach
    void setUp() {
        scoreManager = new ScoreManager();
    }


    // =========================================================================
    // 1. RegularItem ↔ ScoreManager
    // =========================================================================

    /**
     * Simulates a player picking up three coins of different values and
     * verifies that the total score equals the sum of all three coin values.
     *
     * <p>Interaction under test: {@link RegularItem#applyEffect()} →
     * {@link ScoreManager#addPoints(int)}.</p>
     */
    @Test
    void multipleRegularItems_sumToTotalScore() {
        RegularItem coin1 = new RegularItem("Bronze Coin", 10, "COIN", scoreManager);
        RegularItem coin2 = new RegularItem("Silver Coin", 25, "COIN", scoreManager);
        RegularItem coin3 = new RegularItem("Gold Coin",   50, "COIN", scoreManager);

        coin1.applyEffect();
        coin2.applyEffect();
        coin3.applyEffect();

        assertEquals(85, scoreManager.getScore(),
                "Score should equal the sum of all collected coin values");
    }

    /**
     * Confirms that a {@link RegularItem} constructed with a {@code null}
     * {@link ScoreManager} can be picked up without crashing and does not
     * modify the score of a separate, valid {@link ScoreManager}.
     *
     * <p>Interaction under test: null-manager item alongside a live
     * {@link ScoreManager} — the two must remain independent.</p>
     */
    @Test
    void regularItem_nullManager_doesNotAffectOtherManagers() {
        RegularItem safeItem = new RegularItem("Ghost Coin", 100, "COIN", null);
        scoreManager.addPoints(50);
        safeItem.applyEffect();

        assertEquals(50, scoreManager.getScore(),
                "A null-manager item should not affect other score managers");
    }

    // =========================================================================
    // 3. Clock ↔ ScoreManager — time-based score penalty
    // =========================================================================

    /**
     * Simulates a game mechanic where a score penalty of 20 points is applied
     * every 10 seconds. Confirms that after 30 clock ticks the elapsed time
     * is 30 and the score has been reduced by three penalties.
     *
     * <p>Interaction under test: {@link Clock#updateTime()} / {@link Clock#getTime()}
     * driving conditional calls to {@link ScoreManager#subtractPoints(int)}.</p>
     */
    @Test
    void clock_and_scoreManager_timePenaltySimulation() {
        Clock clock = new Clock();
        scoreManager.addPoints(200);

        for (int i = 0; i < 30; i++) {
            clock.updateTime();
            if (clock.getTime() % 10 == 0) {
                scoreManager.subtractPoints(20);
            }
        }

        assertEquals(30, clock.getTime(),
                "Clock should have ticked 30 times");
        assertEquals(140, scoreManager.getScore(),
                "Three 20-point penalties at t=10,20,30 → 200 - 60 = 140");
    }

    /**
     * Verifies that {@link Clock#subtractTime(int)} has no effect on the
     * {@link ScoreManager}, confirming that the two components are independent
     * and do not share any state.
     */
    @Test
    void clock_subtractTime_doesNotChangeScore() {
        Clock clock = new Clock(60);
        scoreManager.addPoints(300);

        clock.subtractTime(30);

        assertEquals(30, clock.getTime(),
                "Clock time should have been reduced");
        assertEquals(300, scoreManager.getScore(),
                "Score should be unaffected by clock manipulation");
    }


    // =========================================================================
    // 4. ShovelPart collection across levels
    // =========================================================================

    /**
     * Simulates the player collecting all three {@link ShovelPart} items
     * across levels 1–3 and verifies that the inventory contains exactly
     * the three expected parts: "Handle", "Stick", and "Shovel Head".
     *
     * <p>Interaction under test: {@link ShovelPart} construction and storage
     * in a player inventory list, then queried at the win-condition check.</p>
     */
    @Test
    void collectAllShovelParts_inventoryComplete() {
        List<ShovelPart> inventory = new ArrayList<>();

        inventory.add(new ShovelPart("Handle"));
        inventory.add(new ShovelPart("Stick"));
        inventory.add(new ShovelPart("Shovel Head"));

        assertEquals(3, inventory.size(),
                "Inventory should contain exactly three shovel parts");
        assertTrue(inventory.stream()
                .anyMatch(p -> p.getPartName().equals("Handle")));
        assertTrue(inventory.stream()
                .anyMatch(p -> p.getPartName().equals("Stick")));
        assertTrue(inventory.stream()
                .anyMatch(p -> p.getPartName().equals("Shovel Head")));
    }

    /**
     * Verifies that picking up a {@link ShovelPart} does not affect the
     * player's score in the {@link ScoreManager}, since shovel parts carry
     * a value of zero and have no {@code applyEffect()} method.
     */
    @Test
    void shovelParts_doNotAffectScore() {
        scoreManager.addPoints(100);
        ShovelPart part = new ShovelPart("Handle");

        assertEquals(0, part.getValue(),
                "ShovelPart value should be 0");
        assertEquals(100, scoreManager.getScore(),
                "Collecting a ShovelPart should leave the score unchanged");
    }

    /**
     * Verifies that holding only two of the three required {@link ShovelPart}
     * items is insufficient to satisfy the game-win condition.
     *
     * <p>Interaction under test: partial inventory checked against the
     * complete set of required part names.</p>
     */
    @Test
    void missingOneShovelPart_inventoryNotComplete() {
        List<ShovelPart> inventory = new ArrayList<>();
        inventory.add(new ShovelPart("Handle"));
        inventory.add(new ShovelPart("Stick"));

        boolean hasAll = inventory.stream()
                .map(ShovelPart::getPartName)
                .collect(Collectors.toSet())
                .containsAll(List.of("Handle", "Stick", "Shovel Head"));

        assertFalse(hasAll,
                "Player should not satisfy the win condition without the Shovel Head");
    }


    // =========================================================================
    // 5. Multi-item level scenario
    // =========================================================================

    /**
     * Full single-level simulation: the player collects three coins via
     * {@link ScoreManager#addCoin(int)}, triggers a trap penalty, then picks
     * up a {@link RegularItem} key.
     *
     * <p>Verifies the final score ({@code 30 - 15 + 50 = 75}) and that the
     * coin gate is open after meeting the three-coin threshold.</p>
     *
     * <p>Interactions under test: {@link ScoreManager#addCoin(int)},
     * {@link ScoreManager#subtractPoints(int)}, and
     * {@link RegularItem#applyEffect()} all targeting the same
     * {@link ScoreManager}.</p>
     */
    @Test
    void levelSimulation_coinsAndTrapAndKey() {
        scoreManager.resetCoins(3);

        scoreManager.addCoin(10);
        scoreManager.addCoin(10);
        scoreManager.addCoin(10);

        scoreManager.subtractPoints(15);

        RegularItem key = new RegularItem("Silver Key", 50, "KEY", scoreManager);
        key.applyEffect();

        assertEquals(3, scoreManager.getCoinsCollected(),
                "Player should have collected exactly three coins");
        assertEquals(65, scoreManager.getScore(),
                "Score: 30 (coins) - 15 (trap) + 50 (key) = 65");
        assertTrue(scoreManager.hasEnoughCoins(),
                "Coin gate should be open after collecting the required three coins");
    }


    // =========================================================================
    // 6. Coin gate / win condition
    // =========================================================================

    /**
     * Verifies that the coin gate remains closed while coins are being
     * collected and opens at the exact moment the required threshold is met.
     *
     * <p>Interaction under test: {@link ScoreManager#addCoin(int)} →
     * {@link ScoreManager#hasEnoughCoins()} evaluated after each coin.</p>
     */
    @Test
    void coinGate_unlocksExactlyAtThreshold() {
        scoreManager.resetCoins(4);

        for (int i = 1; i <= 3; i++) {
            scoreManager.addCoin(5);
            assertFalse(scoreManager.hasEnoughCoins(),
                    "Gate should still be locked after collecting " + i + " coin(s)");
        }

        scoreManager.addCoin(5);
        assertTrue(scoreManager.hasEnoughCoins(),
                "Gate should unlock exactly when the required coin count is reached");
    }

    /**
     * Verifies that calling {@link ScoreManager#resetCoins(int)} at the start
     * of a new level correctly closes the coin gate, even if it was open at
     * the end of the previous level.
     *
     * <p>Interaction under test: cross-level state managed entirely by
     * {@link ScoreManager#resetCoins(int)} and
     * {@link ScoreManager#hasEnoughCoins()}.</p>
     */
    @Test
    void coinGate_resetBetweenLevels_clearsGateState() {
        scoreManager.resetCoins(2);
        scoreManager.addCoin(10);
        scoreManager.addCoin(10);
        assertTrue(scoreManager.hasEnoughCoins(),
                "Level-1 exit should be open after meeting its coin requirement");

        scoreManager.resetCoins(5);
        assertFalse(scoreManager.hasEnoughCoins(),
                "Gate should be closed again at the start of level 2");
    }
}