package escape.board;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import escape.items.Item;
import escape.items.Trap;

/**
 * Represents the overall game world and grid system
 * Manages dimensions of map and stores individual cell objects
 */
public class Board {
    private int width;
    private int height;
    private Cell[][] grid;

    private static final int TRAP_PENALTY = 10;

    /**
     * Constructs a new Board and loads the specified level.
     * @param levelNumber The level to load (1, 2, 3, or 4)
     */
    public Board(int levelNumber) {
        loadMap(levelNumber);
    }

    /**
     * Retrieves a specific cell from the grid based on its coordinates
     * @param x The x-coordinate to look up
     * @param y The y-coordinate to look up
     * @return The Cell at the specified coordinates, or null if out of bounds
     */
    public Cell getCell(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return grid[x][y];
    }

    /**
     * Attempts to grab an item at the specified coordinates.
     * If an item exists, it is removed from the board and returned to the caller.
     * @param x The x-coordinate of the player.
     * @param y The y-coordinate of the player.
     * @return The Item if one was present, or null if the cell is empty.
     */
    public Item collectItemAt(int x, int y) {
        Cell currentCell = getCell(x, y);
        if (currentCell != null && currentCell.hasItem()) {
            Item foundItem = currentCell.getItem();
            currentCell.removeItem();
            return foundItem;
        }
        return null;
    }

    /**
     * Returns the width (number of columns) of the loaded map.
     * @return grid width in cells
     */
    public int getBoardWidth()  { return width; }

    /**
     * Returns the height (number of rows) of the loaded map.
     * @return grid height in cells
     */
    public int getBoardHeight() { return height; }

    /**
    * Loads the map layout, setting up walls, floors, and initial trap placements.
    * Delegates to readLines() and initializeGrid() for clarity.
    */
    public void loadMap(int levelNumber) {
        String filename = "/level" + levelNumber + ".txt";
        InputStream is = getClass().getResourceAsStream(filename);
        if (is == null) {
            System.err.println("Could not find " + filename);
            return;
        }
        List<String> lines = readLines(is);
        initializeGrid(lines);
    }

    /**
    * Reads all lines from the given InputStream into a List.
    * @param is the input stream to read from
    * @return list of lines from the map file
    */
    private List<String> readLines(InputStream is) {
        List<String> lines = new ArrayList<>();
        try (Scanner scanner = new Scanner(is)) {
            while (scanner.hasNextLine()) lines.add(scanner.nextLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
    * Sets up the grid dimensions and populates each cell
    * based on the characters read from the map file.
    * @param lines the list of map rows
    */
    private void initializeGrid(List<String> lines) {
        this.height = lines.size();
        this.width  = lines.isEmpty() ? 0 : lines.get(0).length();
        this.grid   = new Cell[width][height];
        for (int y = 0; y < height; y++) {
            String line = lines.get(y);
            for (int x = 0; x < width; x++) {
                grid[x][y] = createCell(x, y, line.charAt(x));
            }
        }
    }

    /**
     * Creates a single cell based on the character read from the map file.
     * D = door (passable), = = table (impassable), B = bars (impassable)
     * @param x the x coordinate
     * @param y the y coordinate
     * @param c the character from the map file
     * @return the constructed Cell
     */
    private Cell createCell(int x, int y, char c) {
        boolean isWall = (c == '#' || c == '=' || c == 'B'
                    || c == 'O' || c == 'R' || c == 'W'
                    || c == 'H' || c == 'C');
        Cell cell = new Cell(x, y, isWall);
        switch (c) {
            case '#': cell.setTileType("WALL");      break;
            case 'D': cell.setTileType("DOOR");      break;
            case 'G': cell.setTileType("GATE");      break;
            case '=': cell.setTileType("TABLE");     break;
            case 'B': cell.setTileType("BARS");      break;
            case 'O': cell.setTileType("BARRIER");   break;
            case 'R': cell.setTileType("SHELF");     break;
            case 'W': cell.setTileType("WORKBENCH"); break;
            case 'H': cell.setTileType("BED");       break;
            case 'C': cell.setTileType("CABINET");   break;
            case 'T':
                cell.setTileType("FLOOR");
                cell.setTrap(new Trap(TRAP_PENALTY)); break;
            case 'I':
                cell.setTileType("FLOOR");
                cell.setItem(new Item("coin", 10, "COIN")); break;
            case 'F':
                cell.setTileType("FLOOR");
                cell.setItem(new Item("food", 10, "FOOD")); break;
            default:
                cell.setTileType("FLOOR"); break;
        }
        return cell;
    }
}