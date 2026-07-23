package escape.items;

import escape.scores.ScoreManager;

/**
 * Represents a regular collectible item in the escape room, such as a coin pickup.
 *
 * When its effect is applied, this item adds its value to the player's score
 * because of the associated ScoreManager. Extends {@link Item} to inherit
 * base item properties.
 */
public class RegularItem extends Item {

    /** The score manager used to apply point rewards when this item is collected. */
    private final ScoreManager scoreManager;

    /**
     * Constructs a new {@code RegularItem} with the specified attributes.
     *
     * @param name         the display name of the item
     * @param value        the point value awarded when the item is collected
     * @param type         the category or type of the item
     * @param scoreManager the instance used to apply points;
     *                     may be null, in which case no points are awarded
     */
    public RegularItem(String name, int value, String type, ScoreManager scoreManager) {
        super(name, value, type);
        this.scoreManager = scoreManager;
    }

    /**
     * Applies this item's effect by adding its value to the player's score.
     *
     * If no ScoreManager was provided at construction, this method
     * does nothing.
     */
    public void applyEffect() {
        // coin pickup adds points
        if (scoreManager != null) {
            scoreManager.addPoints(getValue());
        }
    }
}