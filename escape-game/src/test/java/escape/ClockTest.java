package escape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import escape.board.Clock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Clock} class.
 *
 * <p>Covers all public methods and edge-case behaviour:</p>
 * <ul>
 *   <li>Default and parameterized construction (positive, zero, negative start)</li>
 *   <li>{@link Clock#updateTime()} increments elapsed time by exactly 1 per call</li>
 *   <li>{@link Clock#subtractTime(int)} normal reduction, clamp-to-zero, and
 *       negative-amount no-op</li>
 * </ul>
 */
public class ClockTest {

    /** The {@link Clock} instance recreated fresh before every test. */
    private Clock clock;

    /**
     * Initialises a default {@link Clock} before each test method runs.
     * Using {@code @BeforeEach} guarantees that no test can pollute the
     * state seen by any other test.
     */
    @BeforeEach
    void setUp() {
        clock = new Clock();
    }

    // ── Construction ──────────────────────────────────────────────────────────

    /**
     * Verifies that the no-argument constructor initialises elapsed time to
     * zero, as required by the specification.
     */
    @Test
    void defaultConstructor_startsAtZero() {
        assertEquals(0, clock.getTime(),
                "Default clock should start at 0");
    }

    /**
     * Verifies that a positive starting time supplied to the parameterized
     * constructor is stored correctly.
     */
    @Test
    void paramConstructor_positiveStartingTime() {
        Clock c = new Clock(30);
        assertEquals(30, c.getTime(),
                "Clock should start at the provided positive value");
    }

    /**
     * Verifies that supplying zero as the starting time results in an elapsed
     * time of zero (boundary value).
     */
    @Test
    void paramConstructor_zeroStartingTime() {
        Clock c = new Clock(0);
        assertEquals(0, c.getTime());
    }

    /**
     * Verifies that a negative starting time is clamped to zero rather than
     * allowing the clock to hold an invalid negative state.
     */
    @Test
    void paramConstructor_negativeStartingTime_clampsToZero() {
        Clock c = new Clock(-10);
        assertEquals(0, c.getTime(),
                "Negative starting time should be clamped to 0");
    }

    // ── updateTime ────────────────────────────────────────────────────────────

    /**
     * Verifies that a single call to {@link Clock#updateTime()} increments
     * the elapsed time by exactly one unit.
     */
    @Test
    void updateTime_incrementsByOne() {
        clock.updateTime();
        assertEquals(1, clock.getTime());
    }

    /**
     * Verifies that repeated calls to {@link Clock#updateTime()} accumulate
     * correctly, confirming the method does not reset or skip values.
     */
    @Test
    void updateTime_multipleCallsAccumulate() {
        for (int i = 0; i < 5; i++) clock.updateTime();
        assertEquals(5, clock.getTime());
    }

    // ── subtractTime ──────────────────────────────────────────────────────────

    /**
     * Verifies the standard case where a valid positive amount is subtracted
     * and the result is still non-negative.
     */
    @Test
    void subtractTime_normalCase() {
        Clock c = new Clock(10);
        c.subtractTime(3);
        assertEquals(7, c.getTime());
    }

    /**
     * Verifies that subtracting the exact current elapsed time results in
     * zero (boundary condition).
     */
    @Test
    void subtractTime_exactlyZero() {
        Clock c = new Clock(5);
        c.subtractTime(5);
        assertEquals(0, c.getTime());
    }

    /**
     * Verifies that subtracting more than the current elapsed time clamps the
     * result to zero rather than allowing a negative elapsed time.
     */
    @Test
    void subtractTime_wouldGoNegative_clampsToZero() {
        Clock c = new Clock(3);
        c.subtractTime(100);
        assertEquals(0, c.getTime(),
                "Time should not go below zero");
    }

    /**
     * Verifies that passing a negative amount to {@link Clock#subtractTime(int)}
     * is treated as a no-op, leaving elapsed time unchanged.
     */
    @Test
    void subtractTime_negativeAmount_doesNothing() {
        Clock c = new Clock(10);
        c.subtractTime(-5);
        assertEquals(10, c.getTime(),
                "Negative subtraction amount should be a no-op");
    }

    /**
     * Verifies that subtracting zero leaves the elapsed time unchanged
     * (boundary / identity case).
     */
    @Test
    void subtractTime_zeroAmount_doesNothing() {
        Clock c = new Clock(10);
        c.subtractTime(0);
        assertEquals(10, c.getTime());
    }
}