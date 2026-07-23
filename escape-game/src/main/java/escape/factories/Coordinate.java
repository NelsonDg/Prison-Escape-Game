package escape.factories;

/**
 * Represents an (x, y) position on the game grid.
 * Replaces raw int[] arrays used for spawn and exit coordinates
 * across the factory classes, improving readability and type safety.
 */
public class Coordinate {
    public final int x;
    public final int y;

    /**
     * Constructs a new Coordinate.
     * @param x the x position on the grid
     * @param y the y position on the grid
     */
    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}