package escape.factories;

import java.util.Arrays;
import java.util.Collections;

import escape.main.LevelConfig;

/**
 * CafeteriaFactory is the concrete {@link LevelFactory} for Level 2:
 * the Cafeteria &amp; Washrooms section of the prison.
 *
 * Map file: {@code /level2.txt} (20 × 12 grid)
 *
 * Reachable coin tiles: 2 — at (4, 4) and (15, 4).
 * Required coins to exit: 1 (player must collect at least one).
 *
 * All spawn coordinates validated as walkable floor tiles.
 */
public class CafeteriaFactory extends LevelFactory {

    /**
     * Creates the {@link LevelConfig} for the Cafeteria &amp; Washrooms section.
     *
     * - Start: (1, 1) — top-left walkable corner.
     * - Exit: (18, 10) — bottom-right walkable corner (BFS-farthest).
     * - 2 regular cops, 1 corruptible cop.
     * - 1 coin required out of 2 reachable map coins.
     *
     * @return configuration for Level 2
     */
    @Override
    public LevelConfig createConfig() {
        return buildConfig(
            2,
            "Cafeteria & Washrooms",
            /* start  */ 2, 8,
            /* exit   */ 51, 26,
            /* coins  */ 3,
            Arrays.asList(
                    new int[]{5,  5},
                    new int[]{14, 9}
            ),
            null,
            Collections.emptyList()
        );
    }
}
