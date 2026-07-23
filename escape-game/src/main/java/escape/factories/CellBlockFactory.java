package escape.factories;

import java.util.Arrays;
import java.util.Collections;

import escape.main.LevelConfig;

/**
 * CellBlockFactory is the concrete {@link LevelFactory} for Level 1:
 * the Cell Block section of the prison.
 *
 * Map file: {@code /level1.txt} (7 × 7 grid)
 *
 * Reachable coin tiles (BFS-verified): 1 at (2, 2).
 * Required coins to exit: 1.
 *
 * All spawn coordinates have been validated as walkable floor tiles
 * against the map file.
 */
public class CellBlockFactory extends LevelFactory {

    /**
     * Creates the {@link LevelConfig} for the Cell Block section.
     *
     * - Start: (1, 1) — top-left walkable corner.
     * - Exit: (5, 5) — bottom-right walkable corner (BFS-farthest).
     * - 1 regular cop, 1 corruptible cop.
     * - 1 coin required — the single map item at (2, 2).
     *
     * @return configuration for Level 1
     */
    @Override
    public LevelConfig createConfig() {
        return buildConfig(
            1,
            "Cell Block",
            /* start  */ 3, 3,
            /* exit   */ 37, 20,
            /* coins  */ 2,
            Arrays.asList(new int[]{45, 19}),
            null,
            Collections.emptyList()
        );
    }
}
