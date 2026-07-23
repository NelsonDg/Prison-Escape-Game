package escape;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import escape.board.Cell;
import escape.items.Item;
import escape.items.Trap;

/**
 * Unit tests for {@link Cell}.
 *
 * Covers: construction, isBlocked(), coordinate getters,
 * the full item lifecycle (set/has/get/remove), and trap accessors.
 */
@DisplayName("Cell — unit tests")
public class CellTest {

    // ------------------------------------------------------------------ //
    //  Construction + isBlocked()                                          //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Wall cell reports isBlocked() == true")
    void isBlocked_wallCell_returnsTrue() {
        Cell wall = new Cell(0, 0, true);
        assertTrue(wall.isBlocked(), "A cell constructed as a wall must be blocked");
    }

    @Test
    @DisplayName("Floor cell reports isBlocked() == false")
    void isBlocked_floorCell_returnsFalse() {
        Cell floor = new Cell(1, 1, false);
        assertFalse(floor.isBlocked(), "A cell constructed as a floor must not be blocked");
    }

    @Test
    @DisplayName("isBlocked() is immutable — no setter exists, value never changes")
    void isBlocked_immutableAfterConstruction() {
        Cell floor = new Cell(2, 2, false);
        // Call twice to confirm no internal state mutation
        assertFalse(floor.isBlocked());
        assertFalse(floor.isBlocked());
    }

    // ------------------------------------------------------------------ //
    //  Coordinate getters                                                  //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("getX() returns the x value passed at construction")
    void getX_returnsConstructorValue() {
        Cell cell = new Cell(7, 3, false);
        assertEquals(7, cell.getX());
    }

    @Test
    @DisplayName("getY() returns the y value passed at construction")
    void getY_returnsConstructorValue() {
        Cell cell = new Cell(4, 9, true);
        assertEquals(9, cell.getY());
    }

    @Test
    @DisplayName("Origin cell (0, 0) stores coordinates correctly")
    void coordinates_originCell() {
        Cell origin = new Cell(0, 0, false);
        assertEquals(0, origin.getX());
        assertEquals(0, origin.getY());
    }

    // ------------------------------------------------------------------ //
    //  Item lifecycle: set → has → get → remove → has                     //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Fresh cell has no item — hasItem() returns false")
    void hasItem_freshCell_returnsFalse() {
        Cell cell = new Cell(1, 1, false);
        assertFalse(cell.hasItem(), "A newly constructed cell must not contain an item");
    }

    @Test
    @DisplayName("getItem() returns null on a fresh cell")
    void getItem_freshCell_returnsNull() {
        Cell cell = new Cell(1, 1, false);
        assertNull(cell.getItem(), "getItem() must return null when no item has been set");
    }

    @Test
    @DisplayName("hasItem() returns true after setItem()")
    void hasItem_afterSetItem_returnsTrue() {
        Cell cell = new Cell(2, 2, false);
        cell.setItem(new Item("coin", 10, "COIN"));
        assertTrue(cell.hasItem());
    }

    @Test
    @DisplayName("getItem() returns the exact item placed by setItem()")
    void getItem_returnsPlacedItem() {
        Cell cell = new Cell(3, 3, false);
        Item coin = new Item("coin", 10, "COIN");
        cell.setItem(coin);
        assertSame(coin, cell.getItem(), "getItem() must return the same Item instance that was set");
    }

    @Test
    @DisplayName("removeItem() clears the item — hasItem() becomes false")
    void removeItem_clearsItem() {
        Cell cell = new Cell(4, 4, false);
        cell.setItem(new Item("coin", 10, "COIN"));
        cell.removeItem();
        assertFalse(cell.hasItem(), "hasItem() must return false after removeItem()");
    }

    @Test
    @DisplayName("getItem() returns null after removeItem()")
    void getItem_afterRemoveItem_returnsNull() {
        Cell cell = new Cell(5, 5, false);
        cell.setItem(new Item("coin", 10, "COIN"));
        cell.removeItem();
        assertNull(cell.getItem());
    }

    @Test
    @DisplayName("removeItem() on an empty cell does not throw")
    void removeItem_onEmptyCell_doesNotThrow() {
        Cell cell = new Cell(6, 6, false);
        assertDoesNotThrow(cell::removeItem,
                "Calling removeItem() on an already-empty cell must not throw an exception");
    }

    @Test
    @DisplayName("setItem() overwrites a previously set item")
    void setItem_overwritesPreviousItem() {
        Cell cell = new Cell(7, 7, false);
        Item first  = new Item("coin", 10, "COIN");
        Item second = new Item("food", 10, "FOOD");
        cell.setItem(first);
        cell.setItem(second);
        assertSame(second, cell.getItem(), "Second setItem() must replace the first item");
    }

    @Test
    @DisplayName("setItem(null) effectively removes the item")
    void setItem_null_effectivelyRemovesItem() {
        Cell cell = new Cell(8, 8, false);
        cell.setItem(new Item("coin", 10, "COIN"));
        cell.setItem(null);
        assertFalse(cell.hasItem(), "setItem(null) should leave the cell with no item");
    }

    // ------------------------------------------------------------------ //
    //  Trap accessors                                                      //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Fresh cell has no trap — getTrap() returns null")
    void getTrap_freshCell_returnsNull() {
        Cell cell = new Cell(1, 1, false);
        assertNull(cell.getTrap(), "A newly constructed cell must not contain a trap");
    }

    @Test
    @DisplayName("getTrap() returns the trap placed by setTrap()")
    void getTrap_returnsPlacedTrap() {
        Cell cell = new Cell(2, 2, false);
        Trap trap = new Trap(10);
        cell.setTrap(trap);
        assertSame(trap, cell.getTrap(), "getTrap() must return the same Trap instance that was set");
    }

    @Test
    @DisplayName("Trap penalty is accessible via getTrap().getPenalty()")
    void getTrap_penaltyAccessible() {
        Cell cell = new Cell(3, 3, false);
        cell.setTrap(new Trap(20));
        assertEquals(20, cell.getTrap().getPenalty());
    }

    @Test
    @DisplayName("setTrap(null) clears the trap")
    void setTrap_null_clearsTrap() {
        Cell cell = new Cell(4, 4, false);
        cell.setTrap(new Trap(15));
        cell.setTrap(null);
        assertNull(cell.getTrap(), "setTrap(null) must leave the cell with no trap");
    }

    @Test
    @DisplayName("A wall cell can still hold a trap (no logic prevents it)")
    void wallCell_canHoldTrap() {
        Cell wall = new Cell(0, 0, true);
        wall.setTrap(new Trap(5));
        assertNotNull(wall.getTrap());
    }

    // ------------------------------------------------------------------ //
    //  Cell has no hasTrap() — document the gap                           //
    // ------------------------------------------------------------------ //

    @Test
    @DisplayName("Trap presence must be checked via getTrap() != null (no hasTrap() exists)")
    void trapPresence_checkedViaGetTrap() {
        Cell cell = new Cell(1, 1, false);
        // Demonstrates the idiom callers must use
        assertFalse(cell.getTrap() != null, "No hasTrap() method — callers rely on getTrap() != null");
        cell.setTrap(new Trap(10));
        assertTrue(cell.getTrap() != null);
    }
}
