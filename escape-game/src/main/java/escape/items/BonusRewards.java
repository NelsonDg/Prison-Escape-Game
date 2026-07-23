package escape.items;

import escape.entities.Player;

/**
 * Represents a bonus reward item in the escape room that restores player health on pickup.
 *
 * When collected, this item heals the player by 20 HP, capped at the player's
 * maximum health. Extends {@link Item} to inherit base item properties.
 */
public class BonusRewards extends Item {

    /** The player whose health is restored when this item is collected. */
    private final Player player;

    /**
     * Constructs a new {@code BonusRewards} item with the specified attributes.
     *
     * @param name   the display name of the item
     * @param value  the base value of the item
     * @param type   the category or type of the item
     * @param player the player to heal when this item is collected;
     *               may be null, in which case no effect is applied
     */
    public BonusRewards(String name, int value, String type, Player player) {
        super(name, value, type);
        this.player = player;
    }

    /**
     * Applies this item's effect by restoring 20 HP to the player.
     *
     * Health is capped at the player's maximum by {@link Player#heal(int)}.
     * If no player was provided at construction, this method does nothing.
     */
    public void applyEffect() {
        if (player != null) {
            player.heal(20);
        }
    }
}