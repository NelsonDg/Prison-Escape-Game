package escape.items;
/**
 * Represents a trap on the game board
 * Traps apply a penalty to the player's score or health when triggered
 */
public class Trap {
    private int penalty;

    /**
     * Constructs new trap with specified penalty
     * @param penalty Amount to penalize player
     */
    public Trap(int penalty) {
        this.penalty = penalty;
    }

    /**
     * Retrieves penalty value of trap
     * @return The penalty int val
     */
    public int getPenalty() {
        return this.penalty;
    }
}