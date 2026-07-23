package escape.board;

import escape.items.Item;
import escape.items.Trap;

/**
 * Represents a single coordinate grid space on the game board
 * A cell can act as a wall or an open floor, and may contain a trap
 */
public class Cell {
    private int x;
    private int y;
    private boolean wall;
    private Trap trap;
    private Item item;
    private String tileType; // "WALL", "FLOOR", "DOOR", "TABLE", "BARS"

    /**
     * Constructs a new cell
     * @param x The x-coordinate of the cell
     * @param y The y-coordinate of the cell
     * @param wall True if the cell is a wall, false if it is passable floor
     */
    public Cell(int x, int y, boolean wall) {
        this.x        = x;
        this.y        = y;
        this.wall     = wall;
        this.trap     = null;
        this.item     = null;
        this.tileType = wall ? "WALL" : "FLOOR";
    }

    /**
     * Checks if this cell can be stepped on by an entity
     * @return True if the cell is a wall (impassable), false otherwise
     */
    public boolean isBlocked() {
        return this.wall;
    }

    /**
     * Sets the cell to be a wall
     */
    public void setWall() {
        if (!this.isBlocked()) {
            this.wall = !wall;
        }
    }

    /**
     * Returns the visual tile type of this cell.
     * @return "WALL", "FLOOR", "DOOR", "TABLE", or "BARS"
     */
    public String getTileType()            { return tileType; }

    /**
     * Sets the visual tile type of this cell.
     * @param type the tile type string
     */
    public void setTileType(String type)   { this.tileType = type; }

    public void    setItem(Item item) { this.item = item; }
    public Item    getItem()          { return this.item; }

    /**
     * Checks if this cell currently holds an item.
     * @return true if an item is present, false otherwise.
     */
    public boolean hasItem() {
        return this.item != null;
    }

    /**
     * Removes the item from the cell (used when a player collects it).
     */
    public void removeItem() {
        this.item = null;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setTrap(Trap trap) { this.trap = trap; }
    public Trap getTrap()          { return trap; }

    /**
     * Checks if this cell currently holds a trap.
     * @return true if a trap is present, false otherwise.
     */
    public boolean hasTrap() {
        return this.trap != null;
    }
}