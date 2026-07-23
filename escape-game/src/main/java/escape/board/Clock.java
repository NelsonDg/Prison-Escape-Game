package escape.board;


/**
 * Represents a clock that tracks elapsed time in an escape room.
 *
 * The clock maintains a non-negative integer representing the amount of time
 * that has elapsed. Time can be incremented, subtracted, or initialized to a
 * specific starting value.
 */
public class Clock {

    /** The total amount of time elapsed, in seconds. Cannot be negative. */
    private int timeElapsed;

    /**
     * Constructs a new {@link Clock} instance with the elapsed time initialized to zero.
     */
    public Clock() {
        this.timeElapsed = 0;
    }

    /**
     * Constructs a new Clock instance with a specified starting time.
     *
     * If the provided starting time is negative, the elapsed time is
     * set to zero instead.
     *
     * @param startingTime the desired starting time, negative values are treated as zero
     */
    public Clock(int startingTime) {
        this.timeElapsed = Math.max(0, startingTime);
    }

    /**
     * Increments the elapsed time by one unit.
     *
     * Intended to be called once per second to advance the clock forward.
     */
    public void updateTime() {
        timeElapsed += 1;
    }

    /**
     * Returns the current elapsed time.
     *
     * @return the total elapsed time as a non-negative integer
     */
    public int getTime() {
        return timeElapsed;
    }

    /**
     * Subtracts a specified amount from the elapsed time.
     *
     * If the amount is negative, this method does nothing. If subtracting
     * the amount would result in a negative elapsed time, the elapsed time
     * is set to zero instead.
     *
     * @param amount the amount of time to subtract; negative values are ignored
     */
    public void subtractTime(int amount) {
        if (amount < 0) return;
        timeElapsed = Math.max(0, timeElapsed - amount);
    }
}
