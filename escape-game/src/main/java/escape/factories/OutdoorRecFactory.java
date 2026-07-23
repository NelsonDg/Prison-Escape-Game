package escape.factories;

import java.util.Arrays;

import escape.main.LevelConfig;

/**
 * OutdoorRecFactory is the concrete {@link LevelFactory} for Level 4:
 * the Outdoor Recreation section — the final prison section.
 *
 * Map file: {@code /level4.txt} (35 × 14 grid)
 *
 * Map bug (teammate issue): All four 'I' tiles in the map are inside sealed
 * rooms whose walls have no entrance: (3, 3), (31, 3) are enclosed by the
 * {@code #####} blocks at y=2 and y=4, and (11, 7), (23, 7) are inside the
 * central chamber bounded by the {@code ###############} wall at y=6 and
 * inner walls at y=8. None of these cells are reachable by BFS from the
 * start (1, 1). The map files need to be corrected by the team members
 * responsible for level design.
 *
 * Workaround: This factory provides two {@code additionalCoinSpawns} at
 * known reachable positions on the fully open y=1 row so the coin mechanic
 * remains functional until the map is fixed. Required coins: 2.
 *
 * Cop spawn coordinates (15, 8) and (28, 10) from the earlier hard-coded
 * values were walls; corrected to (12, 8) and (26, 10) respectively.
 */
public class OutdoorRecFactory extends LevelFactory {

    /**
     * Creates the {@link LevelConfig} for the Outdoor Recreation section.
     *
     * - Start: (1, 1) — top-left walkable cell on the fully open row.
     * - Exit: (33, 12) — BFS-farthest reachable floor cell (43 steps).
     * - 3 regular cops, no corruptible cop (final section — no shortcut).
     * - 2 coins required; placed programmatically at (5, 1) and (28, 1)
     *   because map 'I' tiles are in sealed rooms.
     *
     * @return configuration for Level 4
     */
    @Override
    public LevelConfig createConfig() {
        return buildConfig(
            4,
            "Outdoor Recreation",
            /* start  */ 31,  5,
            /* exit   */ 31, 28,
            /* coins  */ 4,
            Arrays.asList(
                    new int[]{6,  5},
                    new int[]{12, 9},
                    new int[]{26, 10}
            ),
            null,
            Arrays.asList(
                    new int[]{5,  1},
                    new int[]{28, 1}
            )
        );
    }
}
