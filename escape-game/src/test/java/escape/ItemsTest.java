package escape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import escape.entities.Player;
import escape.items.BonusRewards;
import escape.items.Item;
import escape.items.RegularItem;
import escape.items.ShovelPart;
import escape.scores.ScoreManager;

import static org.junit.jupiter.api.Assertions.*;

// ============================================================================
//  ItemTest
// ============================================================================

/**
 * Unit tests for the {@link Item} base class.
 *
 * <p>Features tested:</p>
 * <ul>
 *   <li>Constructor correctly stores name, value, and type</li>
 *   <li>{@link Item#getName()}, {@link Item#getValue()}, and
 *       {@link Item#getType()} return the stored values</li>
 *   <li>Edge cases: zero value and negative value (no clamping in base class)</li>
 * </ul>
 */
class ItemTest {

    /** The {@link Item} instance shared across tests in this class. */
    private Item item;

    /**
     * Creates a standard {@link Item} before each test to avoid shared
     * mutable state between test methods.
     */
    @BeforeEach
    void setUp() {
        item = new Item("Gold Coin", 50, "COIN");
    }

    /** Verifies that {@link Item#getName()} returns the name provided at construction. */
    @Test
    void getName_returnsCorrectName() {
        assertEquals("Gold Coin", item.getName());
    }

    /** Verifies that {@link Item#getValue()} returns the numeric value provided at construction. */
    @Test
    void getValue_returnsCorrectValue() {
        assertEquals(50, item.getValue());
    }

    /** Verifies that {@link Item#getType()} returns the type string provided at construction. */
    @Test
    void getType_returnsCorrectType() {
        assertEquals("COIN", item.getType());
    }

    /** Verifies that an {@link Item} with a zero value stores and returns zero correctly. */
    @Test
    void item_withZeroValue() {
        Item zero = new Item("Empty", 0, "MISC");
        assertEquals(0, zero.getValue());
    }

    /** Verifies that the {@link Item} base class does not clamp negative values. */
    @Test
    void item_withNegativeValue() {
        Item neg = new Item("Penalty", -10, "MISC");
        assertEquals(-10, neg.getValue());
    }
}


// ============================================================================
//  RegularItemTest
// ============================================================================

/**
 * Unit tests for the {@link RegularItem} class.
 *
 * <p>Features tested:</p>
 * <ul>
 *   <li>{@link RegularItem#applyEffect()} adds the item's value to the
 *       associated {@link ScoreManager}</li>
 *   <li>{@link RegularItem#applyEffect()} with a {@code null}
 *       {@link ScoreManager} does not throw</li>
 *   <li>Repeated calls accumulate score correctly</li>
 *   <li>Inherited {@link Item} getters return the correct values</li>
 * </ul>
 */
class RegularItemTest {

    /** Shared {@link ScoreManager} injected into items under test. */
    private ScoreManager scoreManager;

    /**
     * Creates a fresh {@link ScoreManager} before each test so that score
     * state from one test cannot affect another.
     */
    @BeforeEach
    void setUp() {
        scoreManager = new ScoreManager();
    }

    /** Verifies that applyEffect() adds the item's full point value to the score. */
    @Test
    void applyEffect_addsValueToScore() {
        RegularItem item = new RegularItem("Coin", 30, "COIN", scoreManager);
        item.applyEffect();
        assertEquals(30, scoreManager.getScore());
    }

    /** Verifies that applyEffect() with a null ScoreManager does not throw. */
    @Test
    void applyEffect_doesNotThrowWhenScoreManagerIsNull() {
        RegularItem item = new RegularItem("Coin", 30, "COIN", null);
        assertDoesNotThrow(item::applyEffect);
    }

    /** Verifies that calling applyEffect() twice accumulates the score correctly. */
    @Test
    void applyEffect_calledTwice_accumulatesScore() {
        RegularItem item = new RegularItem("Coin", 10, "COIN", scoreManager);
        item.applyEffect();
        item.applyEffect();
        assertEquals(20, scoreManager.getScore());
    }

    /** Verifies that RegularItem correctly exposes inherited Item getters. */
    @Test
    void regularItem_inheritsGetters() {
        RegularItem item = new RegularItem("Key", 100, "KEY", scoreManager);
        assertEquals("Key", item.getName());
        assertEquals(100, item.getValue());
        assertEquals("KEY", item.getType());
    }

    /** Verifies that a zero-value item leaves the score unchanged after applyEffect(). */
    @Test
    void applyEffect_zeroValueItem_scoreRemainsZero() {
        RegularItem item = new RegularItem("Freebie", 0, "MISC", scoreManager);
        item.applyEffect();
        assertEquals(0, scoreManager.getScore());
    }
}


// ============================================================================
//  ShovelPartTest
// ============================================================================

/**
 * Unit tests for the {@link ShovelPart} class.
 *
 * <p>Features tested:</p>
 * <ul>
 *   <li>{@link ShovelPart#getPartName()} for all three parts</li>
 *   <li>Type is always {@code "SHOVEL_PART"}</li>
 *   <li>Value is always {@code 0}</li>
 *   <li>{@link ShovelPart#getName()} matches the part name</li>
 * </ul>
 */
class ShovelPartTest {

    /** Verifies getPartName() returns "Handle" for the Level 1 part. */
    @Test
    void getPartName_handle() {
        assertEquals("Handle", new ShovelPart("Handle").getPartName());
    }

    /** Verifies getPartName() returns "Stick" for the Level 2 part. */
    @Test
    void getPartName_stick() {
        assertEquals("Stick", new ShovelPart("Stick").getPartName());
    }

    /** Verifies getPartName() returns "Shovel Head" for the Level 3 part. */
    @Test
    void getPartName_shovelHead() {
        assertEquals("Shovel Head", new ShovelPart("Shovel Head").getPartName());
    }

    /** Verifies the type is always "SHOVEL_PART" regardless of which part is constructed. */
    @Test
    void getType_isAlwaysShovelPart() {
        assertEquals("SHOVEL_PART", new ShovelPart("Handle").getType());
    }

    /** Verifies shovel parts carry a point value of zero. */
    @Test
    void getValue_isAlwaysZero() {
        assertEquals(0, new ShovelPart("Stick").getValue());
    }

    /** Verifies that getName() matches the part name passed at construction. */
    @Test
    void getName_matchesPartName() {
        assertEquals("Shovel Head", new ShovelPart("Shovel Head").getName());
    }
}


// ============================================================================
//  BonusRewardsTest
// ============================================================================

/**
 * Unit tests for the {@link BonusRewards} class.
 *
 * <p>Features tested:</p>
 * <ul>
 *   <li>{@link BonusRewards#applyEffect()} heals the player by 20 HP</li>
 *   <li>Health is capped at 100 by {@link Player#heal(int)}</li>
 *   <li>{@link BonusRewards#applyEffect()} with a {@code null} player does not throw</li>
 *   <li>Inherited {@link Item} getters return correct values</li>
 * </ul>
 */
class BonusRewardsTest {

    /** Shared {@link Player} injected into items under test. */
    private Player player;

    /**
     * Creates a fresh {@link Player} at position (0, 0) before each test.
     * A null board is acceptable here since movement is not being tested.
     */
    @BeforeEach
    void setUp() {
        player = new Player(0, 0, null);
    }

    /** Verifies that applyEffect() heals the player by exactly 20 HP. */
    @Test
    void applyEffect_healsPlayerBy20() {
        player.takeDamage(40); // 100 - 40 = 60 HP
        BonusRewards bonus = new BonusRewards("Sandwich", 0, "BONUS", player);
        bonus.applyEffect();
        assertEquals(80, player.getHealth(),
                "Health should increase by 20 (60 + 20 = 80)");
    }

    /** Verifies that health does not exceed 100 when the player is already at full HP. */
    @Test
    void applyEffect_doesNotExceedMaxHealth() {
        BonusRewards bonus = new BonusRewards("Sandwich", 0, "BONUS", player);
        bonus.applyEffect();
        assertEquals(100, player.getHealth(),
                "Health must not exceed 100");
    }

    /** Verifies that applyEffect() with a null player does not throw. */
    @Test
    void applyEffect_nullPlayer_doesNotThrow() {
        BonusRewards bonus = new BonusRewards("Sandwich", 0, "BONUS", null);
        assertDoesNotThrow(bonus::applyEffect);
    }

    /** Verifies that BonusRewards correctly exposes the inherited Item getters. */
    @Test
    void bonusRewards_inheritsItemGetters() {
        BonusRewards bonus = new BonusRewards("Apple", 5, "BONUS", player);
        assertEquals("Apple", bonus.getName());
        assertEquals(5, bonus.getValue());
        assertEquals("BONUS", bonus.getType());
    }

    /** Verifies that collecting the bonus twice heals the player twice (if not already at max). */
    @Test
    void applyEffect_calledTwice_healsUpToMax() {
        player.takeDamage(70); // 100 - 70 = 30 HP
        BonusRewards bonus = new BonusRewards("Berry", 0, "BONUS", player);
        bonus.applyEffect(); // 30 + 20 = 50 HP
        bonus.applyEffect(); // 50 + 20 = 70 HP
        assertEquals(70, player.getHealth());
    }
}