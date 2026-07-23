package escape.factories;

import java.util.Arrays;
import java.util.Collections;

import escape.main.LevelConfig;

/**
 * GeneralTimeFactory is the concrete {@link LevelFactory} for Level 3:
 * the General Time section of the prison.
 *
 * Map file: {@code /level3.txt} (50 × 21 grid — a complex maze)
 *
 * Map note: The top-left area (x=1–4, y=1–4) is a 13-cell dead-end pocket
 * completely isolated from the rest of the maze. The player therefore starts
 * in the main open corridor at (1, 6), which connects to all 252 reachable
 * cells. This is a known map-file limitation that teammates should correct
 * in the level design.
 *
 * Reachable coin tiles (BFS-verified from start (1, 6)):
 * (9, 1), (33, 4), (1, 19), (48, 19) — 4 total.
 * The 'I' tiles at (1, 1) and (5, 10) are inside the isolated pocket and
 * are never reachable. Required coins to exit: 3.
 *
 * All cop spawn coordinates have been validated as walkable floor tiles.
 */
public class GeneralTimeFactory extends LevelFactory {

    /**
     * Creates the {@link LevelConfig} for the General Time section.
     *
     * - Start: (1, 6) — first cell of the main open corridor.
     * - Exit: (48, 19) — BFS-farthest reachable floor cell (60 steps).
     * - 3 regular cops, 1 corruptible cop — all in reachable zones.
     * - 2 coins required out of 4 reachable map coins.
     *
     * @return configuration for Level 3
     */
    @Override
    public LevelConfig createConfig() {
        return buildConfig(
            3,
            "General Time",
            /* start  */ 2, 18,
            /* exit   */ 69, 18,
            /* coins  */ 3,
            Arrays.asList(
                    new int[]{20, 6},
                    new int[]{1,  12},
                    new int[]{30, 17}
            ),
            null,
            Collections.emptyList()
        );
    }
}
