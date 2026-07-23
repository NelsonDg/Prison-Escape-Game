package escape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import escape.board.Board;
import escape.entities.CorruptibleCop;
import escape.entities.Player;
import escape.items.Item;

import static org.junit.jupiter.api.Assertions.*;

public class CorruptibeCopTest {
    private CorruptibleCop cop;
    private Player player;
    private Board board;

    /**
     * The set up for testing the code
     */
    @BeforeEach
    private void setup() {
        this.board = new Board(1);
        this.player = new Player(9,9, board);
        this.cop = new CorruptibleCop(7, 7, board);
    }

    /**
     * Test to check that the player contains the escape key after bribing.
     */
    @Test
    void inventoryHasEscapeKeyAfterBribe() {
        player.viewItems().add(new Item("BRIBE", 0, "BRIBE"));
        cop.giveEscapeItem(player);
        assertEquals(1, player.viewItems().size()); 
    }

    /**
     * Test that checks for successful bribing
     */
    @Test
    void bribeSucceedsWhenPlayerHasBribeItem() {
        player.viewItems().add(new Item("BRIBE", 0, "BRIBE"));
        cop.giveEscapeItem(player);
        boolean hasEscapeKey = false;
        for (Item item : player.viewItems()) {
            if (item.getType().equals("ESCAPEKEY")) {
                hasEscapeKey = true;
            }
        }
        assertTrue(hasEscapeKey);
    }

    /**
     * Test that checks for failed bribing
     */
    @Test
    void bribeFailsWhenPlayerHasNoBribeItem() {
        cop.giveEscapeItem(player);
        boolean hasEscapeKey = false;
        for (Item item : player.viewItems()) {
            if (item.getType().equals("ESCAPEKEY")) {
                hasEscapeKey = true;
            }
        }
        assertFalse(hasEscapeKey);
    }
 
    /**
     * Test that the bribe item is removed after bribing.
     */
    @Test
    void bribeItemIsRemovedAfterSuccessfulBribe() {
        player.viewItems().add(new Item("BRIBE", 0, "BRIBE"));
        cop.giveEscapeItem(player);
        boolean hasBribe = false;
        for (Item item : player.viewItems()) {
            if (item.getType().equals("BRIBE")) {
                hasBribe = true;
            }
        }
        assertFalse(hasBribe);
    }
 
}   
