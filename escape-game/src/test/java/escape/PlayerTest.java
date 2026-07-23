package escape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import escape.board.Board;
import escape.entities.Player;
import escape.items.Item;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    private Board board;
    private Player player;

    /**
    * To initialize the player and the board for these test cases.
    */
    @BeforeEach
    private void setup() {
        this.board = new Board(1);
        this.player = new Player(5, 5, board);
    }

    /**
     * To test to make sure that the inventory starts as empty
     */
    @Test
    public void playerStartsWithEmptyInventory() {
        assertTrue(player.viewItems().isEmpty());
    }

    /**
     * To check that items gets added into the player's inventory properly
     */
    @Test
    public void checkIfInventoryWorks() {
        board.getCell(5, 5).setItem(new Item("key", 1, "KEY"));
        player.collectItem();
        assertEquals("KEY", player.viewItems().get(0).getType());
    }

    /**
     * Test how the player would handle picking up a nonexistent item in the grid.
     */
    @Test
    public void checkPickingUpNull() {
        player.collectItem();
        assertTrue(player.viewItems().isEmpty());
    }

    /**
     * Test to see that the player starts with full health
     */
    @Test
    public void checkUserStartsWithFullHealth() {
        assertEquals(player.getHealth(), 100);
    }

    /**
     * Test to see that player properly takes damage
     */
    @Test
    public void checkUserHealthUpdate() {
        player.takeDamage(20);
        assertEquals(80, player.getHealth());
    }    
}
