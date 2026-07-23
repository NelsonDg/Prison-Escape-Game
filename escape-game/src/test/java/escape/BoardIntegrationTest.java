package escape;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import escape.board.Board;
import escape.board.Cell;
import escape.factories.CafeteriaFactory;
import escape.factories.CellBlockFactory;
import escape.factories.GeneralTimeFactory;
import escape.factories.OutdoorRecFactory;
import escape.main.LevelConfig;

/**
 * Integration tests — Board + Cell + Trap + Factory classes working together.
 *
 * <p>Loads real level files from the classpath and verifies correctness
 * across component boundaries:
 * <ul>
 *   <li>Spawn and exit coordinates are walkable floor tiles.</li>
 *   <li>Cop spawn coordinates are walkable floor tiles.</li>
 *   <li>All T/I tiles on every map are BFS-reachable from the level start.</li>
 *   <li>Trap penalty values flow correctly through Board → Cell → Trap.</li>
 *   <li>collectItemAt() integrates correctly with Cell item state.</li>
 *   <li>Additional coin spawns (Level 4) land on walkable, reachable tiles.</li>
 * </ul>
 */
@DisplayName("Integration tests — Board + Cell + Trap + Factories")
public class BoardIntegrationTest {

    // ------------------------------------------------------------------ //
    //  BFS utility                                                         //
    // ------------------------------------------------------------------ //

    /**
     * Returns the set of "x,y" keys for all floor tiles reachable from
     * (startX, startY) by 4-directional BFS.
     */
    private Set<String> bfsReachable(Board board, int startX, int startY) {
        Set<String> visited = new HashSet<>();
        Deque<int[]> queue  = new ArrayDeque<>();

        Cell start = board.getCell(startX, startY);
        if (start == null || start.isBlocked()) return visited;

        queue.add(new int[]{startX, startY});
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int x = cur[0], y = cur[1];
            String key = x + "," + y;
            if (visited.contains(key)) continue;

            Cell cell = board.getCell(x, y);
            if (cell == null || cell.isBlocked()) continue;

            visited.add(key);
            for (int[] d : dirs) queue.add(new int[]{x + d[0], y + d[1]});
        }
        return visited;
    }

    private String key(int x, int y) { return x + "," + y; }

    /** Scans the board and returns coordinates of all trap or item cells. */
    private List<int[]> findTrapAndItemTiles(Board board) {
        List<int[]> found = new ArrayList<>();
        for (int x = 0; x < board.getBoardWidth(); x++) {
            for (int y = 0; y < board.getBoardHeight(); y++) {
                Cell c = board.getCell(x, y);
                if (c != null && (c.getTrap() != null || c.hasItem())) {
                    found.add(new int[]{x, y});
                }
            }
        }
        return found;
    }

    // ================================================================== //
    //  SPAWN + EXIT WALKABILITY                                           //
    // ================================================================== //

    @Test
    @DisplayName("L1 — spawn (3,3) is a walkable floor tile")
    void level1_spawnIsWalkable() {
        Board b = new Board(1);
        LevelConfig cfg = new CellBlockFactory().createConfig();
        Cell spawn = b.getCell(cfg.getStartX(), cfg.getStartY());
        assertNotNull(spawn, "Spawn cell must exist on the map");
        assertFalse(spawn.isBlocked(), "Spawn tile must be walkable");
    }

    @Test
    @DisplayName("L1 — exit (37,20) is a walkable floor tile")
    void level1_exitIsWalkable() {
        Board b = new Board(1);
        LevelConfig cfg = new CellBlockFactory().createConfig();
        Cell exit = b.getCell(cfg.getExitX(), cfg.getExitY());
        assertNotNull(exit, "Exit cell must exist on the map");
        assertFalse(exit.isBlocked(), "Exit tile must be walkable");
    }

    @Test
    @DisplayName("L2 — spawn (2,8) is a walkable floor tile")
    void level2_spawnIsWalkable() {
        Board b = new Board(2);
        LevelConfig cfg = new CafeteriaFactory().createConfig();
        assertFalse(b.getCell(cfg.getStartX(), cfg.getStartY()).isBlocked());
    }

    @Test
    @DisplayName("L2 — exit (51,26) is a walkable floor tile")
    void level2_exitIsWalkable() {
        Board b = new Board(2);
        LevelConfig cfg = new CafeteriaFactory().createConfig();
        assertFalse(b.getCell(cfg.getExitX(), cfg.getExitY()).isBlocked());
    }

    @Test
    @DisplayName("L3 — spawn (2,18) is a walkable floor tile")
    void level3_spawnIsWalkable() {
        Board b = new Board(3);
        LevelConfig cfg = new GeneralTimeFactory().createConfig();
        assertFalse(b.getCell(cfg.getStartX(), cfg.getStartY()).isBlocked());
    }

    @Test
    @DisplayName("L3 — exit (69,18) is a walkable floor tile")
    void level3_exitIsWalkable() {
        Board b = new Board(3);
        LevelConfig cfg = new GeneralTimeFactory().createConfig();
        assertFalse(b.getCell(cfg.getExitX(), cfg.getExitY()).isBlocked());
    }

    @Test
    @DisplayName("L4 — spawn (31,5) is a walkable floor tile")
    void level4_spawnIsWalkable() {
        Board b = new Board(4);
        LevelConfig cfg = new OutdoorRecFactory().createConfig();
        assertFalse(b.getCell(cfg.getStartX(), cfg.getStartY()).isBlocked());
    }

    @Test
    @DisplayName("L4 — exit (31,28) is a walkable floor tile")
    void level4_exitIsWalkable() {
        Board b = new Board(4);
        LevelConfig cfg = new OutdoorRecFactory().createConfig();
        assertFalse(b.getCell(cfg.getExitX(), cfg.getExitY()).isBlocked());
    }

    // ================================================================== //
    //  EXIT IS BFS-REACHABLE FROM SPAWN                                  //
    // ================================================================== //

    @Test
    @DisplayName("L1 — exit is BFS-reachable from spawn")
    void level1_exitReachableFromSpawn() {
        Board b = new Board(1);
        LevelConfig cfg = new CellBlockFactory().createConfig();
        Set<String> reachable = bfsReachable(b, cfg.getStartX(), cfg.getStartY());
        assertTrue(reachable.contains(key(cfg.getExitX(), cfg.getExitY())));
    }

    @Test
    @DisplayName("L2 — exit is BFS-reachable from spawn")
    void level2_exitReachableFromSpawn() {
        Board b = new Board(2);
        LevelConfig cfg = new CafeteriaFactory().createConfig();
        Set<String> reachable = bfsReachable(b, cfg.getStartX(), cfg.getStartY());
        assertTrue(reachable.contains(key(cfg.getExitX(), cfg.getExitY())));
    }

    @Test
    @DisplayName("L3 — exit is BFS-reachable from spawn")
    void level3_exitReachableFromSpawn() {
        Board b = new Board(3);
        LevelConfig cfg = new GeneralTimeFactory().createConfig();
        Set<String> reachable = bfsReachable(b, cfg.getStartX(), cfg.getStartY());
        assertTrue(reachable.contains(key(cfg.getExitX(), cfg.getExitY())));
    }

    @Test
    @DisplayName("L4 — exit is BFS-reachable from spawn")
    void level4_exitReachableFromSpawn() {
        Board b = new Board(4);
        LevelConfig cfg = new OutdoorRecFactory().createConfig();
        Set<String> reachable = bfsReachable(b, cfg.getStartX(), cfg.getStartY());
        assertTrue(reachable.contains(key(cfg.getExitX(), cfg.getExitY())));
    }

    // ================================================================== //
    //  ALL T/I TILES ARE BFS-REACHABLE FROM SPAWN                        //
    // ================================================================== //

    @Test
    @DisplayName("L1 — all trap and item tiles are BFS-reachable from spawn")
    void level1_allTrapsAndItemsReachable() {
        Board b = new Board(1);
        LevelConfig cfg = new CellBlockFactory().createConfig();
        Set<String> reachable = bfsReachable(b, cfg.getStartX(), cfg.getStartY());
        for (int[] coord : findTrapAndItemTiles(b)) {
            assertTrue(reachable.contains(key(coord[0], coord[1])),
                    "Unreachable tile at (" + coord[0] + "," + coord[1] + ")");
        }
    }

    @Test
    @DisplayName("L2 — all trap and item tiles are BFS-reachable from spawn")
    void level2_allTrapsAndItemsReachable() {
        Board b = new Board(2);
        LevelConfig cfg = new CafeteriaFactory().createConfig();
        Set<String> reachable = bfsReachable(b, cfg.getStartX(), cfg.getStartY());
        for (int[] coord : findTrapAndItemTiles(b)) {
            assertTrue(reachable.contains(key(coord[0], coord[1])),
                    "Unreachable tile at (" + coord[0] + "," + coord[1] + ")");
        }
    }

    @Test
    @DisplayName("L3 — all trap and item tiles are BFS-reachable from spawn")
    void level3_allTrapsAndItemsReachable() {
        Board b = new Board(3);
        LevelConfig cfg = new GeneralTimeFactory().createConfig();
        Set<String> reachable = bfsReachable(b, cfg.getStartX(), cfg.getStartY());
        for (int[] coord : findTrapAndItemTiles(b)) {
            assertTrue(reachable.contains(key(coord[0], coord[1])),
                    "Unreachable tile at (" + coord[0] + "," + coord[1] + ")");
        }
    }

    @Test
    @DisplayName("L4 — all trap and item tiles are BFS-reachable from spawn")
    void level4_allTrapsAndItemsReachable() {
        Board b = new Board(4);
        LevelConfig cfg = new OutdoorRecFactory().createConfig();
        Set<String> reachable = bfsReachable(b, cfg.getStartX(), cfg.getStartY());
        for (int[] coord : findTrapAndItemTiles(b)) {
            assertTrue(reachable.contains(key(coord[0], coord[1])),
                    "Unreachable tile at (" + coord[0] + "," + coord[1] + ")");
        }
    }

    // ================================================================== //
    //  COP SPAWN WALKABILITY                                              //
    // ================================================================== //

    @Test
    @DisplayName("L1 — cop spawn (3,3) is walkable (also flags overlap with player spawn)")
    void level1_copSpawnWalkable() {
        Board b = new Board(1);
        assertFalse(b.getCell(3, 3).isBlocked());
    }

    @Test
    @DisplayName("L2 — all cop spawns are walkable floor tiles")
    void level2_copSpawnsWalkable() {
        Board b = new Board(2);
        for (int[] spawn : new CafeteriaFactory().createConfig().getRegularCopSpawns()) {
            Cell c = b.getCell(spawn[0], spawn[1]);
            assertNotNull(c, "Cop spawn (" + spawn[0] + "," + spawn[1] + ") is out of bounds");
            assertFalse(c.isBlocked(),
                    "Cop spawn (" + spawn[0] + "," + spawn[1] + ") must not be a wall");
        }
    }

    @Test
    @DisplayName("L3 — all cop spawns are walkable floor tiles")
    void level3_copSpawnsWalkable() {
        Board b = new Board(3);
        for (int[] spawn : new GeneralTimeFactory().createConfig().getRegularCopSpawns()) {
            Cell c = b.getCell(spawn[0], spawn[1]);
            assertNotNull(c);
            assertFalse(c.isBlocked(),
                    "Cop spawn (" + spawn[0] + "," + spawn[1] + ") must not be a wall");
        }
    }

    @Test
    @DisplayName("L4 — all cop spawns are walkable (regression: corrected from wall positions)")
    void level4_copSpawnsWalkable() {
        Board b = new Board(4);
        for (int[] spawn : new OutdoorRecFactory().createConfig().getRegularCopSpawns()) {
            Cell c = b.getCell(spawn[0], spawn[1]);
            assertNotNull(c);
            assertFalse(c.isBlocked(),
                    "Cop spawn (" + spawn[0] + "," + spawn[1] + ") must not be a wall");
        }
    }

    // ================================================================== //
    //  TRAP PENALTY CHAIN: Board → Cell → Trap                           //
    // ================================================================== //

    @Test
    @DisplayName("All map-loaded traps have penalty value of 10")
    void allLevelTraps_penaltyIsTen() {
        for (int level = 1; level <= 4; level++) {
            Board b = new Board(level);
            for (int x = 0; x < b.getBoardWidth(); x++) {
                for (int y = 0; y < b.getBoardHeight(); y++) {
                    Cell c = b.getCell(x, y);
                    if (c != null && c.getTrap() != null) {
                        assertEquals(10, c.getTrap().getPenalty(),
                                "Level " + level + " trap at (" + x + "," + y + ") must have penalty 10");
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("Trap tile is passable — player can step on it")
    void trapTile_isNotBlocked() {
        Board b = new Board(1);
        for (int x = 0; x < b.getBoardWidth(); x++) {
            for (int y = 0; y < b.getBoardHeight(); y++) {
                Cell c = b.getCell(x, y);
                if (c != null && c.getTrap() != null) {
                    assertFalse(c.isBlocked(),
                            "Trap at (" + x + "," + y + ") must be passable");
                    return;
                }
            }
        }
        fail("No trap found on Level 1 — expected at least one 'T' tile");
    }

    // ================================================================== //
    //  ITEM COLLECTION — Board + Cell integration                        //
    // ================================================================== //

    @Test
    @DisplayName("Item tile is passable — player can step on it")
    void itemTile_isNotBlocked() {
        Board b = new Board(1);
        for (int x = 0; x < b.getBoardWidth(); x++) {
            for (int y = 0; y < b.getBoardHeight(); y++) {
                Cell c = b.getCell(x, y);
                if (c != null && c.hasItem()) {
                    assertFalse(c.isBlocked(),
                            "Item tile at (" + x + "," + y + ") must be passable");
                    return;
                }
            }
        }
    }

    @Test
    @DisplayName("collectItemAt() removes item — second call returns null")
    void collectItemAt_removesItemFromBoard() {
        Board b = new Board(1);
        for (int x = 0; x < b.getBoardWidth(); x++) {
            for (int y = 0; y < b.getBoardHeight(); y++) {
                Cell c = b.getCell(x, y);
                if (c != null && c.hasItem()) {
                    assertNotNull(b.collectItemAt(x, y),  "First collection must return the item");
                    assertNull(b.collectItemAt(x, y), "Second call must return null");
                    return;
                }
            }
        }
        fail("No item tile found on Level 1");
    }

    @Test
    @DisplayName("L1 — map has at least 1 reachable item (satisfies requiredCoins == 1)")
    void level1_hasEnoughReachableItems() {
        Board b = new Board(1);
        LevelConfig cfg = new CellBlockFactory().createConfig();
        Set<String> reachable = bfsReachable(b, cfg.getStartX(), cfg.getStartY());
        long count = findTrapAndItemTiles(b).stream()
                .filter(c -> b.getCell(c[0], c[1]).hasItem())
                .filter(c -> reachable.contains(key(c[0], c[1])))
                .count();
        assertTrue(count >= cfg.getRequiredCoins(),
                "Need >= " + cfg.getRequiredCoins() + " reachable item(s); found " + count);
    }

    @Test
    @DisplayName("L2 — map has at least 1 reachable item (satisfies requiredCoins == 1)")
    void level2_hasEnoughReachableItems() {
        Board b = new Board(2);
        LevelConfig cfg = new CafeteriaFactory().createConfig();
        Set<String> reachable = bfsReachable(b, cfg.getStartX(), cfg.getStartY());
        long count = findTrapAndItemTiles(b).stream()
                .filter(c -> b.getCell(c[0], c[1]).hasItem())
                .filter(c -> reachable.contains(key(c[0], c[1])))
                .count();
        assertTrue(count >= cfg.getRequiredCoins());
    }

    @Test
    @DisplayName("L3 — map has at least 2 reachable items (satisfies requiredCoins == 2)")
    void level3_hasEnoughReachableItems() {
        Board b = new Board(3);
        LevelConfig cfg = new GeneralTimeFactory().createConfig();
        Set<String> reachable = bfsReachable(b, cfg.getStartX(), cfg.getStartY());
        long count = findTrapAndItemTiles(b).stream()
                .filter(c -> b.getCell(c[0], c[1]).hasItem())
                .filter(c -> reachable.contains(key(c[0], c[1])))
                .count();
        assertTrue(count >= cfg.getRequiredCoins(),
                "Need >= 2 reachable items; found " + count);
    }

    // ================================================================== //
    //  LEVEL 4 ADDITIONAL COIN SPAWNS                                    //
    // ================================================================== //

    @Test
    @DisplayName("L4 — additional coin spawn (5,1) is a walkable tile")
    void level4_additionalCoinSpawn_5_1_isWalkable() {
        Board b = new Board(4);
        Cell c = b.getCell(5, 1);
        assertNotNull(c);
        assertFalse(c.isBlocked(), "L4 programmatic coin spawn (5,1) must be a floor tile");
    }

    @Test
    @DisplayName("L4 — additional coin spawn (28,1) is a walkable tile")
    void level4_additionalCoinSpawn_28_1_isWalkable() {
        Board b = new Board(4);
        Cell c = b.getCell(28, 1);
        assertNotNull(c);
        assertFalse(c.isBlocked(), "L4 programmatic coin spawn (28,1) must be a floor tile");
    }

    @Test
    @DisplayName("L4 — both additional coin spawns are BFS-reachable from spawn")
    void level4_additionalCoinSpawns_areReachable() {
        Board b = new Board(4);
        LevelConfig cfg = new OutdoorRecFactory().createConfig();
        Set<String> reachable = bfsReachable(b, cfg.getStartX(), cfg.getStartY());
        for (int[] spawn : cfg.getAdditionalCoinSpawns()) {
            assertTrue(reachable.contains(key(spawn[0], spawn[1])),
                    "Additional coin spawn (" + spawn[0] + "," + spawn[1] + ") must be BFS-reachable");
        }
    }
}