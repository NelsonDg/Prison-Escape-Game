package escape;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import escape.items.Trap;

/**
 * Unit tests for {@link Trap}.
 *
 * Responsibility: verifies that getPenalty() returns the value
 * the trap was constructed with, under all edge-case inputs.
 */
@DisplayName("Trap — unit tests")
public class TrapTest {

    // ------------------------------------------------------------------ //
    //  getPenalty()                                                      //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("applyPenalty returns the penalty set at construction (standard value)")
    void applyPenalty_standardValue() {
        Trap trap = new Trap(10);
        assertEquals(10, trap.getPenalty(),
                "getPenalty() should return the exact penalty passed to the constructor");
    }

    @Test
    @DisplayName("applyPenalty returns 0 when constructed with zero penalty")
    void applyPenalty_zeroPenalty() {
        Trap trap = new Trap(0);
        assertEquals(0, trap.getPenalty(),
                "A zero-penalty trap should return 0 — no damage case");
    }

    @Test
    @DisplayName("applyPenalty returns negative value when constructed with negative penalty")
    void applyPenalty_negativePenalty() {
        Trap trap = new Trap(-5);
        assertEquals(-5, trap.getPenalty(),
                "Trap does not guard against negative values — caller is responsible for sign");
    }

    @Test
    @DisplayName("applyPenalty returns large penalty value correctly")
    void applyPenalty_largeValue() {
        Trap trap = new Trap(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, trap.getPenalty(),
                "Trap should handle the maximum int value without overflow");
    }

    @Test
    @DisplayName("applyPenalty is idempotent — multiple calls return the same value")
    void applyPenalty_isIdempotent() {
        Trap trap = new Trap(25);
        int first  = trap.getPenalty();
        int second = trap.getPenalty();
        assertEquals(first, second,
                "getPenalty() must not mutate state — repeated calls must return the same value");
    }

    @Test
    @DisplayName("Two traps with different penalties return independent values")
    void applyPenalty_independentInstances() {
        Trap light = new Trap(5);
        Trap heavy = new Trap(50);
        assertNotEquals(light.getPenalty(), heavy.getPenalty(),
                "Two traps constructed with different penalties must not share state");
    }
}
