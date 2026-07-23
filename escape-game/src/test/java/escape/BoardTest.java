package escape;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import escape.board.Board;
import escape.board.Cell;
import escape.items.Item;

/**
 * Unit tests for {@link Board}.
 *
 * These tests load real level files from src/test/resources.
 * Covers: getCell() bounds checking, dimension getters,
 * the collectItemAt() lifecycle, and null-file resilience.
 *
 * Note: level files must be present at /level1.txt … /level4.txt
 * on the test classpath (src/test/resources/).
 */
@DisplayName("Board — unit tests")
public class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        // Level 1 is the smallest map — fastest to load for unit tests
        board = new Board(1);
    }

    // ------------------------------------------------------------------ //
    //  Dimension getters                                                   //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getBoardWidth() returns a positive value after loadMap()")
    void getBoardWidth_positive() {
        assertTrue(board.getBoardWidth() > 0,
                "Width must be positive after a successful map load");
    }

    @Test
    @DisplayName("getBoardHeight() returns a positive value after loadMap()")
    void getBoardHeight_positive() {
        assertTrue(board.getBoardHeight() > 0,
                "Height must be positive after a successful map load");
    }

    @Test
    @DisplayName("Level 1 map dimensions match expected 47 × 21")
    void boardDimensions_level1() {
        assertEquals(47, board.getBoardWidth(),  "Level 1 width should be 47");
        assertEquals(21, board.getBoardHeight(), "Level 1 height should be 21");
    }

    @Test
    @DisplayName("Level 2 map dimensions match expected 53 × 27")
    void boardDimensions_level2() {
        Board b2 = new Board(2);
        assertEquals(53, b2.getBoardWidth(),  "Level 2 width should be 53");
        assertEquals(27, b2.getBoardHeight(), "Level 2 height should be 27");
    }

    @Test
    @DisplayName("Level 3 map dimensions match expected 71 × 37")
    void boardDimensions_level3() {
        Board b3 = new Board(3);
        assertEquals(71, b3.getBoardWidth(),  "Level 3 width should be 71");
        assertEquals(37, b3.getBoardHeight(), "Level 3 height should be 37");
    }

    @Test
    @DisplayName("Level 4 map dimensions match expected 63 × 29")
    void boardDimensions_level4() {
        Board b4 = new Board(4);
        assertEquals(63, b4.getBoardWidth(),  "Level 4 width should be 63");
        assertEquals(29, b4.getBoardHeight(), "Level 4 height should be 29");
    }

    // ------------------------------------------------------------------ //
    //  getCell() — valid coordinates                                       //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getCell() returns non-null for a valid interior coordinate")
    void getCell_validCoordinate_returnsCell() {
        Cell cell = board.getCell(1, 1);
        assertNotNull(cell, "getCell() must return a Cell object for a valid coordinate");
    }

    @Test
    @DisplayName("getCell() returns a Cell with matching x coordinate")
    void getCell_cellHasCorrectX() {
        Cell cell = board.getCell(3, 5);
        assertEquals(3, cell.getX());
    }

    @Test
    @DisplayName("getCell() returns a Cell with matching y coordinate")
    void getCell_cellHasCorrectY() {
        Cell cell = board.getCell(3, 5);
        assertEquals(5, cell.getY());
    }

    // ------------------------------------------------------------------ //
    //  getCell() — out-of-bounds coordinates                              //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getCell() returns null for negative x")
    void getCell_negativeX_returnsNull() {
        assertNull(board.getCell(-1, 0),
                "getCell() must return null for x < 0");
    }

    @Test
    @DisplayName("getCell() returns null for negative y")
    void getCell_negativeY_returnsNull() {
        assertNull(board.getCell(0, -1),
                "getCell() must return null for y < 0");
    }

    @Test
    @DisplayName("getCell() returns null for x == width (one past the edge)")
    void getCell_xEqualsWidth_returnsNull() {
        assertNull(board.getCell(board.getBoardWidth(), 0),
                "getCell() must return null when x equals width");
    }

    @Test
    @DisplayName("getCell() returns null for y == height (one past the edge)")
    void getCell_yEqualsHeight_returnsNull() {
        assertNull(board.getCell(0, board.getBoardHeight()),
                "getCell() must return null when y equals height");
    }

    @Test
    @DisplayName("getCell() returns null for very large x")
    void getCell_largeX_returnsNull() {
        assertNull(board.getCell(Integer.MAX_VALUE, 0));
    }

    @Test
    @DisplayName("getCell() returns null for very large y")
    void getCell_largeY_returnsNull() {
        assertNull(board.getCell(0, Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("getCell() returns non-null for the last valid coordinate (width-1, height-1)")
    void getCell_lastValidCoordinate_returnsCell() {
        int w = board.getBoardWidth();
        int h = board.getBoardHeight();
        assertNotNull(board.getCell(w - 1, h - 1),
                "getCell() must return a Cell at the bottom-right corner");
    }

    // ------------------------------------------------------------------ //
    //  getCell() — border cells are walls                                  //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Top-left corner (0, 0) is a wall")
    void getCell_topLeftCorner_isWall() {
        assertTrue(board.getCell(0, 0).isBlocked(),
                "The top-left border cell must be a wall (#)");
    }

    @Test
    @DisplayName("Top-right corner is a wall")
    void getCell_topRightCorner_isWall() {
        assertTrue(board.getCell(board.getBoardWidth() - 1, 0).isBlocked(),
                "The top-right border cell must be a wall (#)");
    }

    @Test
    @DisplayName("Bottom-left corner is a wall")
    void getCell_bottomLeftCorner_isWall() {
        assertTrue(board.getCell(0, board.getBoardHeight() - 1).isBlocked(),
                "The bottom-left border cell must be a wall (#)");
    }

    @Test
    @DisplayName("Bottom-right corner is a wall")
    void getCell_bottomRightCorner_isWall() {
        int w = board.getBoardWidth();
        int h = board.getBoardHeight();
        assertTrue(board.getCell(w - 1, h - 1).isBlocked(),
                "The bottom-right border cell must be a wall (#)");
    }

    // ------------------------------------------------------------------ //
    //  loadMap() — invalid level number                                    //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("loadMap() with an invalid level number does not throw")
    void loadMap_invalidLevel_doesNotThrow() {
        assertDoesNotThrow(() -> new Board(99),
                "Board constructor must not throw when the level file is missing");
    }

    // ------------------------------------------------------------------ //
    //  collectItemAt()                                                     //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("collectItemAt() returns null on a cell that has no item")
    void collectItemAt_noItem_returnsNull() {
        // (0,0) is always a wall with no item
        assertNull(board.collectItemAt(0, 0),
                "collectItemAt() must return null when no item is present");
    }

    @Test
    @DisplayName("collectItemAt() returns null for out-of-bounds coordinates")
    void collectItemAt_outOfBounds_returnsNull() {
        assertNull(board.collectItemAt(-1, -1),
                "collectItemAt() must return null for out-of-bounds coordinates");
    }

    @Test
    @DisplayName("collectItemAt() returns the item when one exists and then removes it")
    void collectItemAt_itemPresent_returnsAndRemovesItem() {
        // Plant an item manually on a known floor cell
        Cell floor = board.getCell(1, 1);
        assertNotNull(floor, "Pre-condition: (1,1) must be a valid cell");

        Item planted = new Item("coin", 10, "COIN");
        floor.setItem(planted);

        Item collected = board.collectItemAt(1, 1);
        assertSame(planted, collected, "collectItemAt() must return the item that was placed");
        assertFalse(floor.hasItem(), "The item must be removed from the cell after collection");
    }

    @Test
    @DisplayName("collectItemAt() returns null on second call after item already collected")
    void collectItemAt_secondCall_returnsNull() {
        Cell floor = board.getCell(1, 1);
        floor.setItem(new Item("coin", 10, "COIN"));

        board.collectItemAt(1, 1); // first collection
        assertNull(board.collectItemAt(1, 1),
                "Second call to collectItemAt() must return null — item already removed");
    }
}
