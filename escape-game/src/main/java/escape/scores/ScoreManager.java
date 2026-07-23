package escape.scores;

import escape.main.GameEngine;

/**
 * ScoreManager tracks the player's score and coin collection progress
 * throughout the Escape game.
 *
 * It is the single source of truth for two related but separate concerns:
 * <ul>
 *   <li><strong>Score</strong> — a running point total modified by item
 *       pickups, trap penalties, and cop collisions.</li>
 *   <li><strong>Coins</strong> — a counter that must reach
 *       {@link #coinsRequired} before the level exit unlocks.
 *       {@link GameEngine} checks this via {@link #hasEnoughCoins()}.</li>
 * </ul>
 *
 * Coin state is reset each level via {@link #resetCoins(int)} so the
 * requirement can differ between sections of the prison.
 */
public class ScoreManager {

    /** Running point total accumulated across all levels. */
    private int score;

    /** Number of COIN items the player has collected this level. */
    private int coinsCollected;

    /**
     * Number of COIN items required to unlock the exit on the current level.
     * Set (or reset) by {@link #resetCoins(int)} at the start of each level.
     */
    private int coinsRequired;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /**
     * Creates a ScoreManager with zero score and no coin requirement.
     * Call {@link #resetCoins(int)} after construction to set the level's
     * coin requirement before gameplay begins.
     */
    public ScoreManager() {
        this.score          = 0;
        this.coinsCollected = 0;
        this.coinsRequired  = 0;
    }

    /**
     * Creates a ScoreManager with a pre-set starting score and no coin
     * requirement.
     *
     * @param startingScore initial score value (must be ≥ 0)
     */
    public ScoreManager(int startingScore) {
        this.score          = startingScore;
        this.coinsCollected = 0;
        this.coinsRequired  = 0;
    }

    // -----------------------------------------------------------------------
    // Score methods
    // -----------------------------------------------------------------------

    /**
     * Returns the player's current total score.
     *
     * @return current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Adds points to the score.  Negative values are treated as zero so
     * this method can never decrease the score.
     *
     * @param points number of points to add (negative values are ignored)
     * @return updated score after addition
     */
    public int addPoints(int points) {
        if (points < 0) points = 0;
        score += points;
        return score;
    }

    /**
     * Subtracts points from the score.  Used for trap and cop penalties.
     * Negative values are treated as zero so this method can never
     * accidentally add points.
     *
     * @param points number of points to subtract (negative values are ignored)
     * @return updated score after subtraction
     */
    public int subtractPoints(int points) {
        if (points < 0) points = 0;
        score -= points;
        return score;
    }

    // -----------------------------------------------------------------------
    // Coin methods
    // -----------------------------------------------------------------------

    /**
     * Resets the coin counter to zero and sets the required coin threshold
     * for the current level.  Should be called at the start of every level
     * so the requirement can vary between prison sections.
     *
     * @param required minimum coins needed to unlock the exit (must be ≥ 0)
     */
    public void resetCoins(int required) {
        this.coinsCollected = 0;
        this.coinsRequired  = Math.max(0, required);
    }

    /**
     * Records the collection of one COIN item, increments the coin counter,
     * and awards the coin's point value to the score.
     *
     * <p>Call this method from {@link GameEngine} whenever the player steps
     * on a cell containing an item of type {@code "COIN"}.</p>
     *
     * @param coinValue point value of the collected coin (added to score)
     */
    public void addCoin(int coinValue) {
        coinsCollected++;
        addPoints(coinValue); // coins also contribute to the score
    }

    /**
     * Returns {@code true} when the player has collected at least as many
     * coins as the current level requires.
     *
     * Intended usage in {@link GameEngine}:
     * <pre>
     *   public boolean checkWin() {
     *       return scoreManager.hasEnoughCoins() &amp;&amp; player.isAtExit();
     *   }
     * </pre>
     *
     * @return {@code true} if {@link #coinsCollected} &ge; {@link #coinsRequired}
     */
    public boolean hasEnoughCoins() {
        return coinsCollected >= coinsRequired;
    }

    /**
     * Returns the number of COIN items the player has collected so far on
     * the current level.
     *
     * @return coins collected this level
     */
    public int getCoinsCollected() {
        return coinsCollected;
    }

    /**
     * Returns the number of coins required to unlock the exit on the current
     * level.
     *
     * @return coins required for exit
     */
    public int getCoinsRequired() {
        return coinsRequired;
    }
}
