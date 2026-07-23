package escape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import escape.scores.ScoreManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ScoreManager} class.
 *
 * <p>Covers both concerns managed by {@code ScoreManager}:</p>
 * <ul>
 *   <li><strong>Score tracking</strong> — {@link ScoreManager#addPoints(int)},
 *       {@link ScoreManager#subtractPoints(int)}, and constructor pre-sets.</li>
 *   <li><strong>Coin gating</strong> — {@link ScoreManager#addCoin(int)},
 *       {@link ScoreManager#resetCoins(int)}, and
 *       {@link ScoreManager#hasEnoughCoins()} including boundary conditions.</li>
 * </ul>
 */
public class ScoreManagerTest {

    /** Fresh {@link ScoreManager} created before each test to prevent state bleed. */
    private ScoreManager manager;

    /**
     * Creates a new default {@link ScoreManager} before every test method,
     * ensuring each test starts from a clean, zero-initialised state.
     */
    @BeforeEach
    void setUp() {
        manager = new ScoreManager();
    }

    // ── Construction ──────────────────────────────────────────────────────────

    /**
     * Verifies that the no-argument constructor initialises the score to zero.
     */
    @Test
    void defaultConstructor_scoreIsZero() {
        assertEquals(0, manager.getScore());
    }

    /**
     * Verifies that the no-argument constructor initialises coins collected
     * to zero.
     */
    @Test
    void defaultConstructor_coinsAreZero() {
        assertEquals(0, manager.getCoinsCollected());
    }

    /**
     * Verifies that the no-argument constructor initialises the coins-required
     * threshold to zero (no coins needed until {@link ScoreManager#resetCoins(int)}
     * is called).
     */
    @Test
    void defaultConstructor_coinsRequiredIsZero() {
        assertEquals(0, manager.getCoinsRequired());
    }

    /**
     * Verifies that the parameterized constructor stores the supplied starting
     * score correctly.
     */
    @Test
    void paramConstructor_setsStartingScore() {
        ScoreManager sm = new ScoreManager(500);
        assertEquals(500, sm.getScore());
    }

    /**
     * Verifies that supplying a starting score via the parameterized constructor
     * does not affect the coin counters, which should still begin at zero.
     */
    @Test
    void paramConstructor_coinsStillZero() {
        ScoreManager sm = new ScoreManager(500);
        assertEquals(0, sm.getCoinsCollected());
    }

    // ── addPoints ─────────────────────────────────────────────────────────────

    /**
     * Verifies the standard case: adding a positive point value increases the
     * score by that exact amount.
     */
    @Test
    void addPoints_positiveValue_increasesScore() {
        manager.addPoints(100);
        assertEquals(100, manager.getScore());
    }

    /**
     * Verifies that {@link ScoreManager#addPoints(int)} returns the updated
     * score so callers can use the return value directly.
     */
    @Test
    void addPoints_returnsUpdatedScore() {
        int returned = manager.addPoints(40);
        assertEquals(40, returned);
    }

    /**
     * Verifies that adding zero points leaves the score unchanged
     * (boundary / identity case).
     */
    @Test
    void addPoints_zeroValue_noChange() {
        manager.addPoints(50);
        manager.addPoints(0);
        assertEquals(50, manager.getScore());
    }

    /**
     * Verifies that a negative argument to {@link ScoreManager#addPoints(int)}
     * is treated as zero and does not decrease the score.
     */
    @Test
    void addPoints_negativeValue_treatedAsZero() {
        manager.addPoints(50);
        manager.addPoints(-20);
        assertEquals(50, manager.getScore(),
                "Negative points should be ignored, not subtracted");
    }

    /**
     * Verifies that multiple consecutive calls to {@link ScoreManager#addPoints(int)}
     * accumulate correctly into the total score.
     */
    @Test
    void addPoints_multipleCallsAccumulate() {
        manager.addPoints(10);
        manager.addPoints(20);
        manager.addPoints(30);
        assertEquals(60, manager.getScore());
    }

    // ── subtractPoints ────────────────────────────────────────────────────────

    /**
     * Verifies the standard case: subtracting a positive penalty reduces the
     * score by the expected amount.
     */
    @Test
    void subtractPoints_reducesScore() {
        manager.addPoints(100);
        manager.subtractPoints(30);
        assertEquals(70, manager.getScore());
    }

    /**
     * Verifies that {@link ScoreManager#subtractPoints(int)} returns the
     * updated score after deduction.
     */
    @Test
    void subtractPoints_returnsUpdatedScore() {
        manager.addPoints(100);
        int returned = manager.subtractPoints(40);
        assertEquals(60, returned);
    }

    /**
     * Verifies that the score is allowed to go negative (i.e. trap and cop
     * penalties can exceed the current total — there is no floor).
     */
    @Test
    void subtractPoints_canGoNegative() {
        manager.addPoints(10);
        manager.subtractPoints(50);
        assertEquals(-40, manager.getScore());
    }

    /**
     * Verifies that a negative argument to {@link ScoreManager#subtractPoints(int)}
     * is treated as zero so the method can never accidentally increase the score.
     */
    @Test
    void subtractPoints_negativeAmount_treatedAsZero() {
        manager.addPoints(100);
        manager.subtractPoints(-20);
        assertEquals(100, manager.getScore(),
                "Negative penalty amount should be ignored");
    }

    /**
     * Verifies that subtracting zero points leaves the score unchanged
     * (boundary / identity case).
     */
    @Test
    void subtractPoints_zeroAmount_noChange() {
        manager.addPoints(100);
        manager.subtractPoints(0);
        assertEquals(100, manager.getScore());
    }

    // ── addCoin ───────────────────────────────────────────────────────────────

    /**
     * Verifies that {@link ScoreManager#addCoin(int)} increments the internal
     * coin counter by one.
     */
    @Test
    void addCoin_incrementsCoinsCollected() {
        manager.addCoin(10);
        assertEquals(1, manager.getCoinsCollected());
    }

    /**
     * Verifies that the point value of a collected coin is also added to the
     * player's score, since coins contribute to both counters simultaneously.
     */
    @Test
    void addCoin_addsValueToScore() {
        manager.addCoin(25);
        assertEquals(25, manager.getScore());
    }

    /**
     * Verifies that collecting multiple coins accumulates both the coin
     * counter and the score correctly.
     */
    @Test
    void addCoin_multipleCoins_accumulatesBoth() {
        manager.addCoin(10);
        manager.addCoin(20);
        assertEquals(2, manager.getCoinsCollected());
        assertEquals(30, manager.getScore());
    }

    /**
     * Verifies that collecting a zero-value coin still increments the counter
     * but does not change the score (boundary case for coin value).
     */
    @Test
    void addCoin_zeroCoinValue_incrementsCounterOnlyNoScore() {
        manager.addCoin(0);
        assertEquals(1, manager.getCoinsCollected());
        assertEquals(0, manager.getScore());
    }

    // ── resetCoins ────────────────────────────────────────────────────────────

    /**
     * Verifies that {@link ScoreManager#resetCoins(int)} stores the supplied
     * requirement so {@link ScoreManager#getCoinsRequired()} reflects the
     * new level's threshold.
     */
    @Test
    void resetCoins_setsRequiredCoins() {
        manager.resetCoins(5);
        assertEquals(5, manager.getCoinsRequired());
    }

    /**
     * Verifies that {@link ScoreManager#resetCoins(int)} resets the coins-
     * collected counter back to zero, clearing progress from the previous level.
     */
    @Test
    void resetCoins_resetsCollectedCoinsToZero() {
        manager.addCoin(10);
        manager.addCoin(10);
        manager.resetCoins(3);
        assertEquals(0, manager.getCoinsCollected());
    }

    /**
     * Verifies that a negative required-coins argument to
     * {@link ScoreManager#resetCoins(int)} is clamped to zero rather than
     * storing an invalid threshold.
     */
    @Test
    void resetCoins_negativeRequired_clampsToZero() {
        manager.resetCoins(-5);
        assertEquals(0, manager.getCoinsRequired());
    }

    /**
     * Verifies that resetting coins does not touch the player's score,
     * since score persists across levels.
     */
    @Test
    void resetCoins_doesNotAffectScore() {
        manager.addPoints(200);
        manager.resetCoins(3);
        assertEquals(200, manager.getScore(),
                "resetCoins should not touch the player's score");
    }

    // ── hasEnoughCoins ────────────────────────────────────────────────────────

    /**
     * Verifies that {@link ScoreManager#hasEnoughCoins()} returns {@code true}
     * when the collected count exactly meets the required threshold
     * (boundary condition).
     */
    @Test
    void hasEnoughCoins_exactlyMet_returnsTrue() {
        manager.resetCoins(3);
        manager.addCoin(10);
        manager.addCoin(10);
        manager.addCoin(10);
        assertTrue(manager.hasEnoughCoins());
    }

    /**
     * Verifies that {@link ScoreManager#hasEnoughCoins()} returns {@code true}
     * when the player has collected more coins than required.
     */
    @Test
    void hasEnoughCoins_moreThanRequired_returnsTrue() {
        manager.resetCoins(2);
        manager.addCoin(5);
        manager.addCoin(5);
        manager.addCoin(5);
        assertTrue(manager.hasEnoughCoins());
    }

    /**
     * Verifies that {@link ScoreManager#hasEnoughCoins()} returns {@code false}
     * when the player has not yet collected enough coins to unlock the exit.
     */
    @Test
    void hasEnoughCoins_notEnoughYet_returnsFalse() {
        manager.resetCoins(5);
        manager.addCoin(10);
        assertFalse(manager.hasEnoughCoins());
    }

    /**
     * Verifies that with a zero coin requirement (the default),
     * {@link ScoreManager#hasEnoughCoins()} always returns {@code true}
     * even before any coins are collected.
     */
    @Test
    void hasEnoughCoins_requirementZero_alwaysTrue() {
        assertTrue(manager.hasEnoughCoins());
    }

    /**
     * Verifies that calling {@link ScoreManager#resetCoins(int)} recalculates
     * the gate correctly: the gate should open after the first level's
     * requirement is met and close again when the next level sets a higher
     * requirement.
     */
    @Test
    void hasEnoughCoins_afterReset_recalculates() {
        manager.resetCoins(1);
        manager.addCoin(10);
        assertTrue(manager.hasEnoughCoins(),
                "Gate should be open after meeting level 1 requirement");

        manager.resetCoins(3);
        assertFalse(manager.hasEnoughCoins(),
                "Gate should close again after resetting for level 2");
    }
}