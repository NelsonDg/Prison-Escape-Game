package escape.main;

import javax.swing.*;

import escape.audio.MusicPlayer;
import escape.board.Board;
import escape.board.Cell;
import escape.entities.Cop;
import escape.entities.Player;
import escape.items.Item;
import escape.items.ShovelPart;
import escape.scores.ScoreManager;

import java.awt.*;
import java.awt.event.*;

/**
 * GameUI is the Swing-based graphical front-end for the Escape game.
 *
 * All pixel-art sprite constants are declared on the outer class so that
 * both the inner {@link GamePanel} and the {@link #buildLegend()} method can
 * access them without compilation errors.
 */
public class GameUI extends JFrame {

    // -----------------------------------------------------------------------
    // Layout / rendering constants
    // -----------------------------------------------------------------------

    private static final int CANVAS_W    = 840;
    private static final int CANVAS_H    = 560;
    private static final int MAX_CELL_PX = 56;
    private static final int MIN_CELL_PX = 8;
    private static final int TICK_MS     = 600;

    // -----------------------------------------------------------------------
    // Fallback colours (used on tiny cells and as sprite backgrounds)
    // -----------------------------------------------------------------------

    private static final Color COL_WALL        = new Color( 45,  45,  45);
    private static final Color COL_FLOOR       = new Color(210, 205, 185);
    private static final Color COL_EXIT        = new Color( 30, 180,  70);
    private static final Color COL_EXIT_LOCKED = new Color(100, 100, 100);
    private static final Color COL_PLAYER      = new Color( 40, 100, 220);
    private static final Color COL_COP         = new Color(200,  35,  35);
    private static final Color COL_GRID_LINE   = new Color(160, 155, 135);
    private static final Color COL_HUD_BG      = new Color( 25,  25,  25);
    private static final Color COL_LEGEND_BG   = new Color( 38,  38,  38);

    // -----------------------------------------------------------------------
    // Pixel-art sprite constants
    // Each sprite is an 8x8 int[][] where 0 = transparent.
    // -----------------------------------------------------------------------

    // Prisoner (orange jumpsuit)
    private static final int[][] SPRITE_PRISONER = {
        {0, 0, 6, 6, 6, 6, 0, 0},
        {0, 6, 2, 2, 2, 2, 6, 0},
        {0, 6, 2, 5, 5, 2, 6, 0},
        {0, 0, 2, 2, 2, 2, 0, 0},
        {0, 4, 4, 5, 5, 4, 4, 0},
        {0, 3, 4, 4, 4, 4, 3, 0},
        {0, 3, 4, 0, 0, 4, 3, 0},
        {0, 7, 3, 0, 0, 3, 7, 0},
    };
    private static final Color[] PAL_PRISONER = {
        null,
        new Color(160, 100,  60),  // 1 skin dark
        new Color(210, 155,  95),  // 2 skin
        new Color(180,  80,   0),  // 3 jumpsuit dark
        new Color(230, 120,  20),  // 4 jumpsuit orange
        new Color(240, 240, 240),  // 5 white stripe/eyes
        new Color( 50,  30,  10),  // 6 dark hair
        new Color( 40,  30,  20),  // 7 shoe
    };

    // Regular cop (navy uniform)
    private static final int[][] SPRITE_COP = {
        {0, 0, 6, 6, 6, 6, 0, 0},
        {0, 6, 6, 6, 6, 6, 6, 0},
        {0, 0, 2, 2, 2, 2, 0, 0},
        {0, 0, 1, 2, 2, 1, 0, 0},
        {0, 4, 5, 4, 4, 4, 4, 0},
        {0, 3, 4, 4, 4, 4, 3, 0},
        {0, 3, 4, 0, 0, 4, 3, 0},
        {0, 7, 3, 0, 0, 3, 7, 0},
    };
    private static final Color[] PAL_COP = {
        null,
        new Color(160, 100,  60),  // 1 skin dark
        new Color(210, 155,  95),  // 2 skin
        new Color( 20,  30,  70),  // 3 uniform dark navy
        new Color( 40,  55, 120),  // 4 uniform navy
        new Color(220, 180,  20),  // 5 gold badge
        new Color( 15,  15,  15),  // 6 black cap
        new Color( 25,  25,  25),  // 7 shoe
    };

    // Prisoner walk frame (legs together — alternates with SPRITE_PRISONER)
    private static final int[][] SPRITE_PRISONER_WALK = {
        {0, 0, 6, 6, 6, 6, 0, 0},
        {0, 6, 2, 2, 2, 2, 6, 0},
        {0, 6, 2, 5, 5, 2, 6, 0},
        {0, 0, 2, 2, 2, 2, 0, 0},
        {0, 4, 4, 5, 5, 4, 4, 0},
        {0, 3, 4, 4, 4, 4, 3, 0},
        {0, 0, 3, 4, 4, 3, 0, 0},  // legs together
        {0, 0, 7, 3, 3, 7, 0, 0},  // feet together
    };

    // Cop walk frame (legs together — alternates with SPRITE_COP)
    private static final int[][] SPRITE_COP_WALK = {
        {0, 0, 6, 6, 6, 6, 0, 0},
        {0, 6, 6, 6, 6, 6, 6, 0},
        {0, 0, 2, 2, 2, 2, 0, 0},
        {0, 0, 1, 2, 2, 1, 0, 0},
        {0, 4, 5, 4, 4, 4, 4, 0},
        {0, 3, 4, 4, 4, 4, 3, 0},
        {0, 0, 3, 4, 4, 3, 0, 0},  // legs together
        {0, 0, 7, 3, 3, 7, 0, 0},  // feet together
    };

    // Coin (gold circle with shine)
    private static final int[][] SPRITE_COIN = {
        {0, 0, 1, 1, 1, 1, 0, 0},
        {0, 1, 2, 2, 2, 2, 1, 0},
        {1, 2, 3, 3, 2, 2, 2, 1},
        {1, 2, 3, 4, 2, 2, 2, 1},
        {1, 2, 2, 2, 2, 3, 2, 1},
        {1, 2, 2, 2, 3, 3, 2, 1},
        {0, 1, 2, 2, 2, 2, 1, 0},
        {0, 0, 1, 1, 1, 1, 0, 0},
    };
    private static final Color[] PAL_COIN = {
        null,
        new Color(180, 130,  10),
        new Color(220, 175,  30),
        new Color(255, 220,  60),
        new Color(255, 255, 180),
    };

    // Shovel handle
    private static final int[][] SPRITE_HANDLE = {
        {0, 0, 0, 1, 1, 0, 0, 0},
        {0, 0, 1, 2, 3, 1, 0, 0},
        {0, 0, 1, 2, 3, 1, 0, 0},
        {0, 0, 4, 4, 4, 4, 0, 0},
        {0, 0, 1, 2, 3, 1, 0, 0},
        {0, 0, 1, 2, 3, 1, 0, 0},
        {0, 1, 2, 2, 2, 2, 1, 0},
        {0, 1, 1, 1, 1, 1, 1, 0},
    };
    private static final Color[] PAL_HANDLE = {
        null,
        new Color(100,  60,  20),
        new Color(160, 100,  40),
        new Color(200, 145,  70),
        new Color(140, 140, 140),
    };

    // Shovel stick
    private static final int[][] SPRITE_STICK = {
        {0, 0, 1, 2, 3, 1, 0, 0},
        {0, 0, 1, 2, 3, 1, 0, 0},
        {0, 0, 1, 3, 2, 1, 0, 0},
        {0, 0, 1, 2, 3, 1, 0, 0},
        {0, 0, 1, 2, 3, 1, 0, 0},
        {0, 0, 1, 3, 2, 1, 0, 0},
        {0, 0, 1, 2, 3, 1, 0, 0},
        {0, 0, 1, 2, 3, 1, 0, 0},
    };
    private static final Color[] PAL_STICK = {
        null,
        new Color(100,  60,  20),
        new Color(160, 100,  40),
        new Color(200, 145,  70),
    };

    // Shovel head (metal blade)
    private static final int[][] SPRITE_SHOVELHEAD = {
        {0, 1, 1, 1, 1, 1, 1, 0},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 3, 4, 4, 3, 2, 1},
        {0, 1, 2, 3, 3, 2, 1, 0},
        {0, 0, 1, 2, 2, 1, 0, 0},
        {0, 0, 0, 1, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
    };
    private static final Color[] PAL_SHOVELHEAD = {
        null,
        new Color( 80,  80,  90),
        new Color(140, 140, 155),
        new Color(190, 190, 205),
        new Color(230, 235, 255),
    };

    // Food (bread loaf)
    private static final int[][] SPRITE_FOOD = {
        {0, 0, 1, 1, 1, 1, 0, 0},
        {0, 1, 2, 2, 2, 2, 1, 0},
        {1, 2, 2, 3, 3, 2, 2, 1},
        {1, 2, 3, 4, 3, 3, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {0, 1, 1, 2, 2, 1, 1, 0},
        {0, 0, 1, 1, 1, 1, 0, 0},
    };
    private static final Color[] PAL_FOOD = {
        null,
        new Color(160,  90,  20),
        new Color(210, 140,  50),
        new Color(240, 200, 130),
        new Color(255, 235, 180),
    };

    // Wall (brick pattern)
    private static final int[][] SPRITE_WALL = {
        {1, 1, 2, 1, 1, 1, 2, 1},
        {3, 3, 3, 3, 3, 3, 3, 3},
        {1, 2, 1, 1, 1, 2, 1, 1},
        {3, 3, 3, 3, 3, 3, 3, 3},
        {1, 1, 1, 2, 1, 1, 1, 2},
        {3, 3, 3, 3, 3, 3, 3, 3},
        {2, 1, 1, 1, 2, 1, 1, 1},
        {3, 3, 3, 3, 3, 3, 3, 3},
    };
    private static final Color[] PAL_WALL = {
        null,
        new Color( 90,  90,  90),
        new Color( 60,  60,  60),
        new Color( 30,  30,  30),
    };

    // Floor (stone tile pattern)
    private static final int[][] SPRITE_FLOOR = {
        {1, 1, 2, 1, 1, 2, 1, 1},
        {1, 3, 1, 1, 3, 1, 1, 3},
        {2, 1, 1, 2, 1, 1, 2, 1},
        {1, 1, 3, 1, 1, 3, 1, 1},
        {1, 2, 1, 1, 2, 1, 1, 2},
        {3, 1, 1, 3, 1, 1, 3, 1},
        {1, 1, 2, 1, 1, 2, 1, 1},
        {1, 3, 1, 1, 3, 1, 1, 3},
    };
    private static final Color[] PAL_FLOOR = {
        null,
        new Color(210, 203, 185),
        new Color(184, 176, 160),
        new Color(160, 152, 136),
    };

    // Trap (spiky orange burst)
    private static final int[][] SPRITE_TRAP = {
        {0, 0, 0, 1, 1, 0, 0, 0},
        {0, 0, 1, 2, 2, 1, 0, 0},
        {0, 1, 2, 3, 3, 2, 1, 0},
        {1, 2, 3, 4, 4, 3, 2, 1},
        {1, 2, 3, 4, 4, 3, 2, 1},
        {0, 1, 2, 3, 3, 2, 1, 0},
        {0, 0, 1, 1, 1, 1, 0, 0},
        {0, 1, 1, 0, 0, 1, 1, 0},
    };
    private static final Color[] PAL_TRAP = {
        null,
        new Color(138,  42,   0),
        new Color(204,  68,   0),
        new Color(255, 119,   0),
        new Color(255, 221,   0),
    };

    // Door (wooden interior door with handle)
    private static final int[][] SPRITE_DOOR = {
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 3, 0, 4, 3, 2, 1},
        {1, 2, 3, 4, 0, 3, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 1, 1, 1, 1, 1, 1, 1},
    };
    private static final Color[] PAL_DOOR = {
        null,
        new Color(101,  67,  33),  // dark wood border
        new Color(139,  90,  43),  // mid wood
        new Color(180, 120,  60),  // light wood
        new Color(200, 200, 200),  // door handle
    };

    // Gate (outdoor metal gate — vertical bars + crossbar + gold latch)
    private static final int[][] SPRITE_GATE = {
        {1, 0, 2, 0, 2, 0, 2, 1},
        {1, 0, 2, 0, 2, 0, 2, 1},
        {1, 0, 2, 0, 2, 0, 2, 1},
        {3, 3, 3, 4, 4, 3, 3, 3},
        {1, 0, 2, 0, 2, 0, 2, 1},
        {1, 0, 2, 0, 2, 0, 2, 1},
        {3, 3, 3, 3, 3, 3, 3, 3},
        {1, 0, 2, 0, 2, 0, 2, 1},
    };
    private static final Color[] PAL_GATE = {
        null,
        new Color( 90,  90,  75),  // post sides (darker)
        new Color(155, 155, 130),  // vertical bars (lighter metal)
        new Color( 65,  65,  50),  // crossbar (dark)
        new Color(205, 170,  45),  // gold latch
    };

    // Table (indoor wooden table with legs)
    private static final int[][] SPRITE_TABLE = {
        {0, 0, 0, 0, 0, 0, 0, 0},
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {0, 1, 0, 0, 0, 0, 1, 0},
        {0, 1, 0, 0, 0, 0, 1, 0},
    };
    private static final Color[] PAL_TABLE = {
        null,
        new Color(101,  67,  33),  // dark wood
        new Color(139,  90,  43),  // mid wood
        new Color(180, 130,  70),  // light wood surface
    };

    // Bars (prison cell bars — vertical bars with crossbars)
    private static final int[][] SPRITE_BARS = {
        {1, 2, 1, 2, 1, 2, 1, 2},
        {1, 2, 1, 2, 1, 2, 1, 2},
        {1, 2, 1, 2, 1, 2, 1, 2},
        {3, 3, 3, 3, 3, 3, 3, 3},
        {1, 2, 1, 2, 1, 2, 1, 2},
        {1, 2, 1, 2, 1, 2, 1, 2},
        {1, 2, 1, 2, 1, 2, 1, 2},
        {3, 3, 3, 3, 3, 3, 3, 3},
    };
    private static final Color[] PAL_BARS = {
        null,
        new Color( 80,  80,  90),  // dark metal
        new Color(150, 150, 160),  // light metal
        new Color( 50,  50,  55),  // crossbar
    };

    // Barrier (outdoor concrete block — solid grey)
    private static final int[][] SPRITE_BARRIER = {
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 1, 1, 1, 1, 1, 1, 1},
    };
    private static final Color[] PAL_BARRIER = {
        null,
        new Color( 70,  70,  75),  // dark grey outline
        new Color(110, 110, 115),  // mid grey
        new Color(145, 145, 150),  // light grey surface
    };

    // Shelf / Rack (bookshelves in library + equipment racks in gym)
    private static final int[][] SPRITE_SHELF = {
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 3, 4, 5, 3, 4, 1},
        {1, 2, 3, 4, 5, 3, 4, 1},
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 5, 4, 3, 2, 4, 3, 1},
        {1, 5, 4, 3, 2, 4, 3, 1},
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1},
    };
    private static final Color[] PAL_SHELF = {
        null,
        new Color(100,  65,  30),  // dark wood frame
        new Color(180,  60,  60),  // red book / item
        new Color( 60,  90, 180),  // blue book / item
        new Color( 60, 150,  80),  // green book / item
        new Color(200, 160,  40),  // yellow book / item
    };

    // Workbench (workshop bench with tools on top)
    private static final int[][] SPRITE_WORKBENCH = {
        {0, 0, 0, 0, 0, 0, 0, 0},
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 3, 2, 3, 2, 3, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {0, 4, 0, 0, 0, 0, 4, 0},
        {0, 4, 0, 0, 0, 0, 4, 0},
    };
    private static final Color[] PAL_WORKBENCH = {
        null,
        new Color(101,  67,  33),  // dark wood
        new Color(160, 100,  45),  // mid wood surface
        new Color(140, 140, 145),  // metal tool on surface
        new Color( 75,  50,  20),  // dark leg
    };

    // Hospital Bed (white sheets, light blue pillow, metal frame)
    private static final int[][] SPRITE_BED = {
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 4, 4, 4, 4, 4, 4, 1},
        {1, 4, 4, 4, 4, 4, 4, 1},
        {1, 4, 4, 4, 4, 4, 4, 1},
        {0, 5, 0, 0, 0, 0, 5, 0},
        {0, 5, 0, 0, 0, 0, 5, 0},
    };
    private static final Color[] PAL_BED = {
        null,
        new Color(180, 180, 185),  // grey metal frame
        new Color(240, 240, 245),  // white sheets
        new Color(210, 215, 240),  // light blue pillow
        new Color(232, 232, 238),  // white bed surface
        new Color(140, 140, 145),  // metal legs
    };

    // Medicine Cabinet (white box with red cross symbol)
    private static final int[][] SPRITE_CABINET = {
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 2, 3, 3, 2, 2, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 3, 3, 3, 3, 3, 3, 1},
        {1, 2, 3, 3, 3, 3, 2, 1},
        {1, 2, 2, 3, 3, 2, 2, 1},
        {1, 4, 4, 4, 4, 4, 4, 1},
        {1, 1, 1, 1, 1, 1, 1, 1},
    };
    private static final Color[] PAL_CABINET = {
        null,
        new Color(215, 215, 220),  // light grey cabinet body
        new Color(240, 240, 245),  // white panel area
        new Color(200,  45,  45),  // red cross symbol
        new Color(155, 155, 160),  // darker bottom strip
    };

    // Exit open (green door)
    private static final int[][] SPRITE_EXIT_OPEN = {
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 2, 0, 3, 3, 0, 2, 1},
        {1, 2, 3, 4, 4, 3, 2, 1},
        {1, 2, 3, 4, 4, 3, 2, 1},
        {1, 2, 0, 3, 3, 0, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 1, 1, 1, 1, 1, 1, 1},
    };
    private static final Color[] PAL_EXIT_OPEN = {
        null,
        new Color( 10,  92,  40),
        new Color( 20, 160,  74),
        new Color( 40, 204, 100),
        new Color(170, 255, 200),
    };

    // Exit locked (grey door)
    private static final int[][] SPRITE_EXIT_LOCKED = {
        {1, 1, 1, 1, 1, 1, 1, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 2, 0, 3, 3, 0, 2, 1},
        {1, 2, 3, 0, 0, 3, 2, 1},
        {1, 2, 3, 0, 0, 3, 2, 1},
        {1, 2, 0, 3, 3, 0, 2, 1},
        {1, 2, 2, 2, 2, 2, 2, 1},
        {1, 1, 1, 1, 1, 1, 1, 1},
    };
    private static final Color[] PAL_EXIT_LOCKED = {
        null,
        new Color( 40,  40,  40),
        new Color( 80,  80,  80),
        new Color(130, 130, 130),
        new Color(180, 180, 180),
    };

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private final GameEngine engine;
    private final GamePanel  gamePanel;
    private final Timer      gameTimer;
    private CardLayout       cardLayout;

    private JLabel timerLabel;
    private JLabel healthLabel;
    private JLabel coinsLabel;
    private JLabel shovelLabel;
    private JLabel scoreLabel;
    private JPanel endPanel;
    private int    playerAnimFrame  = 0;
    private int    lastKnownHealth  = -1;
    private int    lastKnownCoins   = 0;
    private int    lastKnownShovel  = 0;
    private int    lastShownLevel   = 0;
    private String toastMessage     = null;
    private long   toastUntil       = 0;
    private Color  toastColor       = Color.WHITE;
    private long   damageFlashUntil = 0;
    private long   foodFlashUntil   = 0;
    private long   coinFlashUntil   = 0;
    private long   shovelFlashUntil = 0;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public GameUI() {
        super("Escape \u2013 Prison Break");

        engine = new GameEngine();
        engine.startGame(); // initialises board/player so GamePanel never sees null

        MusicPlayer.start();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        cardLayout = new CardLayout();
        setLayout(cardLayout);

        // --- build game view ---
        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(CANVAS_W, CANVAS_H));
        gamePanel.setBackground(Color.BLACK);

        JPanel hudPanel = new JPanel(new GridLayout(1, 5));
        hudPanel.setBackground(COL_HUD_BG);
        timerLabel  = makeHudLabel("\u23f1  Time: --");
        healthLabel = makeHudLabel("\u2764  HP: --");
        coinsLabel  = makeHudLabel("\uD83E\uDE99  Coins: --");
        shovelLabel = makeHudLabel("\uD83E\uDE9A  Shovel: 0/3");
        scoreLabel  = makeHudLabel("\u2b50  Score: 0");
        hudPanel.add(timerLabel);
        hudPanel.add(healthLabel);
        hudPanel.add(coinsLabel);
        hudPanel.add(shovelLabel);
        hudPanel.add(scoreLabel);

        JPanel gameView = new JPanel(new BorderLayout(0, 0));
        gameView.add(gamePanel, BorderLayout.CENTER);
        gameView.add(hudPanel,  BorderLayout.SOUTH);

        setupKeyBindings();
        gameTimer = new Timer(TICK_MS, e -> autoTick());
        endPanel  = buildEndPanel();

        // --- add cards ---
        add(buildMenuPanel(),    "MENU");
        add(gameView,            "GAME");
        add(endPanel,            "END");
        add(buildLegendPanel(),  "LEGEND");

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // show menu first — game timer does NOT start until Play is clicked
        cardLayout.show(getContentPane(), "MENU");
    }

    private JPanel buildMenuPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                g2.setColor(new Color(15, 15, 20));
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setFont(new Font("SansSerif", Font.BOLD, 72));
                g2.setColor(new Color(230, 120, 20));
                String title = "ESCAPE";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, getHeight() / 2 - 60);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
                g2.setColor(new Color(160, 155, 135));
                String sub = "A Prison Break Game";
                fm = g2.getFontMetrics();
                g2.drawString(sub, (getWidth() - fm.stringWidth(sub)) / 2, getHeight() / 2 - 20);
            }
        };
        panel.setPreferredSize(new Dimension(CANVAS_W, CANVAS_H + 80));
        panel.setLayout(new BorderLayout());

        JButton playBtn = new JButton("PLAY");
        styleMenuButton(playBtn, new Color(230, 120, 20));
        playBtn.setPreferredSize(new Dimension(200, 52));
        playBtn.addActionListener(e -> startGameFromMenu());

        JButton legendBtn = new JButton("HOW TO PLAY");
        styleMenuButton(legendBtn, new Color(50, 90, 130));
        legendBtn.setPreferredSize(new Dimension(200, 44));
        legendBtn.addActionListener(e -> showLegendDialog());

        JButton quitBtn = new JButton("QUIT");
        styleMenuButton(quitBtn, new Color(70, 70, 80));
        quitBtn.setPreferredSize(new Dimension(200, 44));
        quitBtn.addActionListener(e -> System.exit(0));

        Box btnBox = Box.createVerticalBox();
        btnBox.setOpaque(false);
        btnBox.add(Box.createVerticalStrut(8));
        for (JButton b : new JButton[]{playBtn, legendBtn, quitBtn}) {
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnBox.add(b);
            btnBox.add(Box.createVerticalStrut(10));
        }

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnBox);
        panel.add(btnWrapper, BorderLayout.SOUTH);
        return panel;
    }

    private void showLegendDialog() {
        cardLayout.show(getContentPane(), "LEGEND");
    }

    private JPanel buildLegendPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(15, 15, 20));

        // Title
        JLabel title = new JLabel("HOW TO PLAY", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(230, 120, 20));
        title.setBorder(BorderFactory.createEmptyBorder(24, 0, 12, 0));
        title.setOpaque(false);
        panel.add(title, BorderLayout.NORTH);

        // Controls text above the legend
        JLabel keys = new JLabel(
            "<html><center><font color='white'>"
            + "<b>WASD / Arrow Keys</b> — Move &nbsp;&nbsp; "
            + "Collect coins and reach the exit to advance levels.<br>"
            + "Find all 3 shovel parts across levels 1 to 3 to unlock the final exit."
            + "</font></center></html>",
            SwingConstants.CENTER);
        keys.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        keys.setOpaque(false);

        // Legend grid + instructions stacked in center
        JPanel centerStack = new JPanel(new BorderLayout());
        centerStack.setBackground(new Color(15, 15, 20));
        centerStack.add(keys, BorderLayout.NORTH);
        JPanel legendWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        legendWrapper.setBackground(new Color(15, 15, 20));
        legendWrapper.add(buildLegend());
        centerStack.add(legendWrapper, BorderLayout.CENTER);
        panel.add(centerStack, BorderLayout.CENTER);

        // Back button at bottom
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        bottom.setBackground(new Color(15, 15, 20));
        JButton backBtn = new JButton("Back");
        styleMenuButton(backBtn, new Color(70, 70, 80));
        backBtn.addActionListener(e -> cardLayout.show(getContentPane(), "MENU"));
        bottom.add(backBtn);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private void startGameFromMenu() {
        engine.startGame();
        gameTimer.restart();
        cardLayout.show(getContentPane(), "GAME");
        gamePanel.requestFocusInWindow();
        refresh();
    }

    private JPanel buildEndPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                boolean won = engine.isGameWon();
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                // Background
                g2.setColor(won ? new Color(10, 30, 10) : new Color(30, 8, 8));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Title
                g2.setFont(new Font("SansSerif", Font.BOLD, 64));
                g2.setColor(won ? new Color(60, 220, 80) : new Color(220, 50, 50));
                String title = won ? "YOU ESCAPED!" : "GAME OVER";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(title, cx - fm.stringWidth(title) / 2, cy - 110);

                // Score
                g2.setFont(new Font("SansSerif", Font.BOLD, 36));
                g2.setColor(new Color(230, 120, 20));
                String score = "Final Score: " + engine.getScoreManager().getScore();
                fm = g2.getFontMetrics();
                g2.drawString(score, cx - fm.stringWidth(score) / 2, cy - 55);

                // Subtitle
                g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
                g2.setColor(new Color(160, 155, 135));
                String sub = won ? "Congratulations! You broke out of prison!"
                                 : "You were caught or ran out of time.";
                fm = g2.getFontMetrics();
                g2.drawString(sub, cx - fm.stringWidth(sub) / 2, cy - 15);

                // Stats
                g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
                g2.setColor(new Color(120, 120, 130));
                String stats = "Coins: " + engine.getScoreManager().getCoinsCollected()
                        + "   |   Time used: " + engine.getTotalTicks() + " ticks"
                        + "   |   Level reached: " + engine.getCurrentLevel();
                fm = g2.getFontMetrics();
                g2.drawString(stats, cx - fm.stringWidth(stats) / 2, cy + 20);
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(CANVAS_W, CANVAS_H + 80));

        JButton playAgainBtn = new JButton("Play Again");
        styleMenuButton(playAgainBtn, new Color(230, 120, 20));
        playAgainBtn.setPreferredSize(new Dimension(150, 48));
        playAgainBtn.addActionListener(e -> {
            MusicPlayer.stop();
            MusicPlayer.start();
            startGameFromMenu();
        });

        JButton menuBtn = new JButton("Main Menu");
        styleMenuButton(menuBtn, new Color(60, 80, 130));
        menuBtn.setPreferredSize(new Dimension(150, 48));
        menuBtn.addActionListener(e -> {
            MusicPlayer.stop();
            MusicPlayer.start();
            gameTimer.stop();
            cardLayout.show(getContentPane(), "MENU");
        });

        JButton quitBtn = new JButton("Quit");
        styleMenuButton(quitBtn, new Color(70, 70, 80));
        quitBtn.setPreferredSize(new Dimension(150, 48));
        quitBtn.addActionListener(e -> System.exit(0));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnRow.setOpaque(false);
        btnRow.add(playAgainBtn);
        btnRow.add(menuBtn);
        btnRow.add(quitBtn);

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnRow);
        panel.add(btnWrapper, BorderLayout.SOUTH);
        return panel;
    }

    // -----------------------------------------------------------------------
    // Key bindings
    // -----------------------------------------------------------------------

    private void setupKeyBindings() {
        InputMap  im = gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = gamePanel.getActionMap();

        bindMove(im, am, "UP_W",      KeyEvent.VK_W,      0, -1);
        bindMove(im, am, "DOWN_S",    KeyEvent.VK_S,      0,  1);
        bindMove(im, am, "LEFT_A",    KeyEvent.VK_A,     -1,  0);
        bindMove(im, am, "RIGHT_D",   KeyEvent.VK_D,      1,  0);
        bindMove(im, am, "UP_ARR",    KeyEvent.VK_UP,     0, -1);
        bindMove(im, am, "DOWN_ARR",  KeyEvent.VK_DOWN,   0,  1);
        bindMove(im, am, "LEFT_ARR",  KeyEvent.VK_LEFT,  -1,  0);
        bindMove(im, am, "RIGHT_ARR", KeyEvent.VK_RIGHT,  1,  0);

    }

    private void bindMove(InputMap im, ActionMap am,
                          String name, int key, int dx, int dy) {
        im.put(KeyStroke.getKeyStroke(key, 0), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!engine.isGameRunning()) return;
                int bx = engine.getPlayer().getX(), by = engine.getPlayer().getY();
                engine.movePlayer(dx, dy);
                if (engine.getPlayer().getX() != bx || engine.getPlayer().getY() != by)
                    playerAnimFrame ^= 1;
                refresh();
            }
        });
    }

    // -----------------------------------------------------------------------
    // Game loop helpers
    // -----------------------------------------------------------------------

    private void autoTick() {
        if (!engine.isGameRunning()) { gameTimer.stop(); return; }
        engine.updateTick();
        refresh();
        if (!engine.isGameRunning()) { gameTimer.stop(); showEndScreen(); }
    }

    private void refresh() {
        long now = System.currentTimeMillis();
        int currentHealth = engine.getPlayer().getHealth();
        if (lastKnownHealth > 0 && currentHealth < lastKnownHealth)
            damageFlashUntil = now + 500;
        if (lastKnownHealth > 0 && currentHealth > lastKnownHealth)
            foodFlashUntil = now + 500;
        lastKnownHealth = currentHealth;

        int currentCoins = engine.getScoreManager().getCoinsCollected();
        if (currentCoins > lastKnownCoins)
            coinFlashUntil = now + 300;
        lastKnownCoins = currentCoins;

        int currentShovel = engine.getCollectedShovelParts().size();
        if (currentShovel > lastKnownShovel) {
            shovelFlashUntil = now + 600;
            java.util.Set<String> parts = engine.getCollectedShovelParts();
            String[] names = {"Handle", "Stick", "Shovel Head"};
            String found = "Shovel Part";
            for (String name : names) {
                if (parts.contains(name)) { found = name; }
            }
            toastMessage = "You found the " + found + "! (" + currentShovel + "/3)";
            toastUntil   = now + 3000;
            toastColor   = new Color(0, 210, 255);
        }
        lastKnownShovel = currentShovel;

        int level = engine.getCurrentLevel();
        if (level != lastShownLevel) {
            lastShownLevel = level;
            if (level >= 1 && level <= 3) {
                String part = level == 1 ? "Handle" : level == 2 ? "Stick" : "Shovel Head";
                toastMessage = "Find the " + part + " before leaving this level!";
                toastUntil   = now + 4000;
                toastColor   = new Color(255, 220, 80);
            }
        }

        gamePanel.repaint();
        refreshHud();
    }

    private void refreshHud() {
        Player       p  = engine.getPlayer();
        ScoreManager sm = engine.getScoreManager();
        int remaining   = Math.max(0,
                GameEngine.MAX_TICKS_PER_LEVEL - engine.getGameClock().getTime());

        timerLabel.setText("\u23f1  Time: " + remaining + "s");

        int hp = p.getHealth();
        healthLabel.setText("\u2764  HP: " + hp);
        healthLabel.setForeground(hp <= 30 ? Color.RED : Color.WHITE);

        String lock = sm.hasEnoughCoins() ? " \u2713" : " \uD83D\uDD12";
        coinsLabel.setText("\uD83E\uDE99  Coins: "
                + sm.getCoinsCollected() + "/" + sm.getCoinsRequired() + lock);

        java.util.Set<String> parts = engine.getCollectedShovelParts();
        shovelLabel.setText("\uD83E\uDE9A " + parts.size() + "/3  "
                + (parts.contains("Handle")      ? "\u2713" : "\u2717") + " "
                + (parts.contains("Stick")       ? "\u2713" : "\u2717") + " "
                + (parts.contains("Shovel Head") ? "\u2713" : "\u2717"));
        shovelLabel.setForeground(engine.hasFullShovel() ? Color.GREEN : Color.WHITE);

        scoreLabel.setText("\u2b50  Score: " + sm.getScore());
    }

    private void showEndScreen() {
        endPanel.repaint();
        cardLayout.show(getContentPane(), "END");
    }

    // -----------------------------------------------------------------------
    // Legend strip — shows sprite + label for every tile type
    // -----------------------------------------------------------------------

    private JPanel buildLegend() {
        final Object[][] objectives = {
            { "You",               SPRITE_PRISONER,    PAL_PRISONER,           COL_FLOOR },
            { "Cop",               SPRITE_COP,         PAL_COP,                COL_FLOOR },
            { "Trap",              SPRITE_TRAP,        PAL_TRAP,               COL_FLOOR },
            { "Coin",              SPRITE_COIN,        PAL_COIN,               COL_FLOOR },
            { "Food (+HP)",        SPRITE_FOOD,        PAL_FOOD,               COL_FLOOR },
            { "Handle",            SPRITE_HANDLE,      PAL_HANDLE,             COL_FLOOR },
            { "Stick",             SPRITE_STICK,       PAL_STICK,              COL_FLOOR },
            { "Shovel Head",       SPRITE_SHOVELHEAD,  PAL_SHOVELHEAD,         COL_FLOOR },
            { "Exit \u2713",       SPRITE_EXIT_OPEN,   PAL_EXIT_OPEN,          COL_FLOOR },
            { "Exit \uD83D\uDD12", SPRITE_EXIT_LOCKED, PAL_EXIT_LOCKED,        COL_FLOOR },
        };

        final Object[][] mapElements = {
            { "Wall",    SPRITE_WALL,      PAL_WALL,      COL_WALL  },
            { "Floor",   SPRITE_FLOOR,     PAL_FLOOR,     COL_FLOOR },
            { "Door",    SPRITE_DOOR,      PAL_DOOR,      COL_FLOOR },
            { "Gate",    SPRITE_GATE,      PAL_GATE,      COL_FLOOR },
            { "Table",   SPRITE_TABLE,     PAL_TABLE,     COL_FLOOR },
            { "Bars",    SPRITE_BARS,      PAL_BARS,      COL_WALL  },
            { "Barrier", SPRITE_BARRIER,   PAL_BARRIER,   COL_FLOOR },
            { "Shelf",   SPRITE_SHELF,     PAL_SHELF,     COL_FLOOR },
            { "Bench",   SPRITE_WORKBENCH, PAL_WORKBENCH, COL_FLOOR },
            { "Bed",     SPRITE_BED,       PAL_BED,       COL_FLOOR },
            { "Cabinet", SPRITE_CABINET,   PAL_CABINET,   COL_FLOOR },
        };

        JPanel container = new JPanel(new GridLayout(1, 2, 16, 0));
        container.setBackground(COL_LEGEND_BG);
        container.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        container.add(buildLegendSection("Objectives", objectives));
        container.add(buildLegendSection("Map Elements", mapElements));
        return container;
    }

    private JPanel buildLegendSection(String heading, Object[][] entries) {
        final int COLS   = 2;
        final int SZ     = 24;
        final int CELL_W = 130;
        final int CELL_H = 36;
        final int PAD    = 8;

        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(COL_LEGEND_BG);

        JLabel header = new JLabel(heading, SwingConstants.LEFT);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setForeground(new Color(230, 120, 20));
        header.setBorder(BorderFactory.createEmptyBorder(0, PAD, 6, 0));
        section.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                int rows = (entries.length + COLS - 1) / COLS;
                return new Dimension(COLS * CELL_W + 2 * PAD, rows * CELL_H + 2 * PAD);
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics;
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setFont(new Font("SansSerif", Font.PLAIN, 12));
                FontMetrics fm = g.getFontMetrics();

                for (int i = 0; i < entries.length; i++) {
                    int col = i % COLS;
                    int row = i / COLS;
                    int cx  = PAD + col * CELL_W;
                    int cy  = PAD + row * CELL_H;

                    String  label   = (String)  entries[i][0];
                    int[][] sprite  = (int[][]) entries[i][1];
                    Color[] palette = (Color[]) entries[i][2];
                    Color   bg      = (Color)   entries[i][3];

                    int sy = cy + (CELL_H - SZ) / 2;
                    g.setColor(bg);
                    g.fillRect(cx, sy, SZ, SZ);

                    int sRows = sprite.length, sCols = sprite[0].length;
                    float pw = (float) SZ / sCols, ph = (float) SZ / sRows;
                    for (int r = 0; r < sRows; r++) {
                        for (int c = 0; c < sCols; c++) {
                            int idx = sprite[r][c];
                            if (idx == 0 || idx >= palette.length || palette[idx] == null) continue;
                            g.setColor(palette[idx]);
                            g.fillRect(cx + Math.round(c * pw),
                                       sy + Math.round(r * ph),
                                       Math.max(1, Math.round(pw)),
                                       Math.max(1, Math.round(ph)));
                        }
                    }

                    g.setColor(Color.LIGHT_GRAY);
                    int textY = cy + (CELL_H + fm.getAscent() - fm.getDescent()) / 2;
                    g.drawString(label, cx + SZ + 6, textY);
                }
            }
        };
        grid.setBackground(COL_LEGEND_BG);
        section.add(grid, BorderLayout.CENTER);
        return section;
    }

    // -----------------------------------------------------------------------
    // HUD helper
    // -----------------------------------------------------------------------

    private JLabel makeHudLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Monospaced", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        label.setBackground(COL_HUD_BG);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return label;
    }

    // -----------------------------------------------------------------------
    // GamePanel — the grid renderer
    // -----------------------------------------------------------------------

    private class GamePanel extends JPanel {

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Board  board  = engine.getBoard();
            Player player = engine.getPlayer();

            int boardW = board.getBoardWidth();
            int boardH = board.getBoardHeight();
            int panelW = getWidth();
            int panelH = getHeight();

            // Viewport: how many tiles are visible at once
            final int VIEWPORT_W = 21;
            final int VIEWPORT_H = 15;

            int cell = Math.min(panelW / VIEWPORT_W, panelH / VIEWPORT_H);
            cell = Math.min(cell, MAX_CELL_PX);
            cell = Math.max(cell, MIN_CELL_PX);

            // Camera centered on player, clamped so it never shows outside the board
            int camX = Math.max(0, Math.min(player.getX() - VIEWPORT_W / 2, boardW - VIEWPORT_W));
            int camY = Math.max(0, Math.min(player.getY() - VIEWPORT_H / 2, boardH - VIEWPORT_H));

            // Bake camera offset into ox/oy — all draw methods work unchanged
            int ox = (panelW - VIEWPORT_W * cell) / 2 - camX * cell;
            int oy = (panelH - VIEWPORT_H * cell) / 2 - camY * cell;

            drawGrid(g, board, boardW, boardH, cell, ox, oy);
            drawExit(g, cell, ox, oy);
            drawEnemies(g, cell, ox, oy);
            drawEntity(g, player.getX(), player.getY(),
                       COL_PLAYER, "P", cell, ox, oy);
            drawFogOfWar(g, player, boardW, boardH, cell, ox, oy);
            drawVignette(g, panelW, panelH, new Color(180,   0,   0), damageFlashUntil, 500, 180);
            drawVignette(g, panelW, panelH, new Color(  0, 180,  60), foodFlashUntil,   500,  60);
            drawVignette(g, panelW, panelH, new Color(220, 170,   0), coinFlashUntil,   300,  45);
            drawVignette(g, panelW, panelH, new Color(  0, 180, 220), shovelFlashUntil, 600,  70);
            drawToast(g, panelW, panelH);
        }

        private void drawFogOfWar(Graphics2D g, Player player,
                                   int boardW, int boardH,
                                   int cell, int ox, int oy) {
            final int VISION_RADIUS = 2;
            int px = player.getX(), py = player.getY();
            float pcx = ox + px * cell + cell / 2.0f;
            float pcy = oy + py * cell + cell / 2.0f;
            float radiusPx = VISION_RADIUS * cell;
            int panelW = getWidth(), panelH = getHeight();

            // Smooth radial gradient: transparent at the player, gradually
            // darkening outward — no hard edge or blocky per-tile cutoff.
            RadialGradientPaint fog = new RadialGradientPaint(
                pcx, pcy, radiusPx * 1.8f,
                new float[] { 0.00f, 0.35f, 0.55f, 0.72f, 0.85f, 1.00f },
                new Color[] {
                    new Color(0, 0, 0,   0),  // fully clear at player
                    new Color(0, 0, 0,   0),  // clear vision zone
                    new Color(0, 0, 0,  60),  // very soft start of fade
                    new Color(0, 0, 0, 110),  // mid-fade
                    new Color(0, 0, 0, 190),  // heavy fog
                    new Color(0, 0, 0, 235),  // near-black at far edge
                }
            );
            g.setPaint(fog);
            g.fillRect(0, 0, panelW, panelH);
            g.setPaint(null);
        }

        private void drawVignette(Graphics2D g, int panelW, int panelH,
                                   Color color, long flashUntil, long durationMs, int maxAlpha) {
            long remaining = flashUntil - System.currentTimeMillis();
            if (remaining <= 0) return;

            float t = (float) remaining / durationMs;
            int edgeAlpha = (int)(maxAlpha * t);
            int r = color.getRed(), gv = color.getGreen(), b = color.getBlue();

            RadialGradientPaint vignette = new RadialGradientPaint(
                panelW / 2f, panelH / 2f,
                Math.max(panelW, panelH) * 0.75f,
                new float[] { 0.0f, 0.5f, 1.0f },
                new Color[] {
                    new Color(r, gv, b,  0),
                    new Color(r, gv, b,  edgeAlpha / 3),
                    new Color(r, gv, b,  edgeAlpha),
                }
            );
            g.setPaint(vignette);
            g.fillRect(0, 0, panelW, panelH);
            g.setPaint(null);

            repaint(30);
        }

        private void drawToast(Graphics2D g, int panelW, int panelH) {
            if (toastMessage == null) return;
            long remaining = toastUntil - System.currentTimeMillis();
            if (remaining <= 0) { toastMessage = null; return; }

            float alpha = Math.min(1f, remaining / 500f);  // fade out last 500ms
            int bgAlpha  = (int)(180 * alpha);
            int txtAlpha = (int)(255 * alpha);

            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g.getFontMetrics();
            int pad  = 10;
            int tw   = fm.stringWidth(toastMessage);
            int th   = fm.getAscent() + fm.getDescent();
            int tx   = pad + 8;
            int ty   = panelH - pad - th - 8;

            g.setColor(new Color(20, 20, 20, bgAlpha));
            g.fillRoundRect(tx - pad, ty - fm.getAscent() - pad / 2,
                            tw + pad * 2, th + pad, 10, 10);

            g.setColor(new Color(toastColor.getRed(), toastColor.getGreen(), toastColor.getBlue(), txtAlpha));
            g.drawString(toastMessage, tx, ty);

            repaint(30);
        }

        /**
         * Draws every cell tile, including all new tile types introduced in
         * Phase 3: GATE, BARRIER, SHELF, WORKBENCH, BED, CABINET.
         */
        private void drawGrid(Graphics2D g, Board board,
                              int boardW, int boardH,
                              int cell, int ox, int oy) {
            for (int x = 0; x < boardW; x++) {
                for (int y = 0; y < boardH; y++) {
                    Cell c = board.getCell(x, y);
                    int cx = ox + x * cell;
                    int cy = oy + y * cell;

                    String tileType = (c == null) ? "WALL" : c.getTileType();

                    switch (tileType) {
                        case "DOOR":
                            g.setColor(COL_FLOOR);
                            g.fillRect(cx, cy, cell, cell);
                            if (cell >= 8) drawPixelArt(g, cx, cy, cell, SPRITE_DOOR, PAL_DOOR);
                            break;

                        case "GATE":
                            g.setColor(COL_FLOOR);
                            g.fillRect(cx, cy, cell, cell);
                            if (cell >= 8) drawPixelArt(g, cx, cy, cell, SPRITE_GATE, PAL_GATE);
                            break;

                        case "TABLE":
                            g.setColor(COL_WALL);
                            g.fillRect(cx, cy, cell, cell);
                            if (cell >= 8) drawPixelArt(g, cx, cy, cell, SPRITE_TABLE, PAL_TABLE);
                            break;

                        case "BARS":
                            g.setColor(COL_WALL);
                            g.fillRect(cx, cy, cell, cell);
                            if (cell >= 8) drawPixelArt(g, cx, cy, cell, SPRITE_BARS, PAL_BARS);
                            break;

                        case "BARRIER":
                            g.setColor(new Color(90, 90, 95));
                            g.fillRect(cx, cy, cell, cell);
                            if (cell >= 8) drawPixelArt(g, cx, cy, cell, SPRITE_BARRIER, PAL_BARRIER);
                            break;

                        case "SHELF":
                            g.setColor(new Color(80, 52, 24));
                            g.fillRect(cx, cy, cell, cell);
                            if (cell >= 8) drawPixelArt(g, cx, cy, cell, SPRITE_SHELF, PAL_SHELF);
                            break;

                        case "WORKBENCH":
                            g.setColor(new Color(90, 58, 25));
                            g.fillRect(cx, cy, cell, cell);
                            if (cell >= 8) drawPixelArt(g, cx, cy, cell, SPRITE_WORKBENCH, PAL_WORKBENCH);
                            break;

                        case "BED":
                            g.setColor(new Color(200, 200, 210));
                            g.fillRect(cx, cy, cell, cell);
                            if (cell >= 8) drawPixelArt(g, cx, cy, cell, SPRITE_BED, PAL_BED);
                            break;

                        case "CABINET":
                            g.setColor(new Color(190, 190, 195));
                            g.fillRect(cx, cy, cell, cell);
                            if (cell >= 8) drawPixelArt(g, cx, cy, cell, SPRITE_CABINET, PAL_CABINET);
                            break;

                        case "WALL":
                            g.setColor(COL_WALL);
                            g.fillRect(cx, cy, cell, cell);
                            if (cell >= 8) drawPixelArt(g, cx, cy, cell, SPRITE_WALL, PAL_WALL);
                            break;

                        default: // FLOOR
                            g.setColor(COL_FLOOR);
                            g.fillRect(cx, cy, cell, cell);
                            if (cell >= 8) drawPixelArt(g, cx, cy, cell, SPRITE_FLOOR, PAL_FLOOR);
                            break;
                    }

                    // Trap overlay on top of base tile
                    if (c != null && c.getTrap() != null && cell >= 16) {
                        drawPixelArt(g, cx, cy, cell, SPRITE_TRAP, PAL_TRAP);
                    }

                    // Item overlay on top of base tile
                    if (c != null && !c.isBlocked() && c.hasItem()) {
                        Item item = c.getItem();
                        if (item instanceof ShovelPart) {
                            ShovelPart sp = (ShovelPart) item;
                            switch (sp.getPartName()) {
                                case "Handle":
                                    drawPixelArt(g, cx, cy, cell, SPRITE_HANDLE, PAL_HANDLE); break;
                                case "Stick":
                                    drawPixelArt(g, cx, cy, cell, SPRITE_STICK, PAL_STICK); break;
                                case "Shovel Head":
                                    drawPixelArt(g, cx, cy, cell, SPRITE_SHOVELHEAD, PAL_SHOVELHEAD); break;
                            }
                        } else if ("FOOD".equals(item.getType())) {
                            drawPixelArt(g, cx, cy, cell, SPRITE_FOOD, PAL_FOOD);
                        } else {
                            drawPixelArt(g, cx, cy, cell, SPRITE_COIN, PAL_COIN);
                        }
                    }

                    // Grid lines
                    if (cell >= 10) {
                        g.setColor(COL_GRID_LINE);
                        g.drawRect(cx, cy, cell, cell);
                    }
                }
            }
        }

        private void drawExit(Graphics2D g, int cell, int ox, int oy) {
            int ex = engine.getExitX();
            int ey = engine.getExitY();
            int cx = ox + ex * cell;
            int cy = oy + ey * cell;

            ScoreManager sm = engine.getScoreManager();
            boolean coinsOk  = sm.hasEnoughCoins();
            boolean shovelOk = engine.getCurrentLevel() < GameEngine.TOTAL_LEVELS
                               || engine.hasFullShovel();
            boolean unlocked = coinsOk && shovelOk;

            if (cell >= 16) {
                drawPixelArt(g, cx, cy, cell,
                        unlocked ? SPRITE_EXIT_OPEN   : SPRITE_EXIT_LOCKED,
                        unlocked ? PAL_EXIT_OPEN      : PAL_EXIT_LOCKED);
            } else {
                g.setColor(unlocked ? COL_EXIT : COL_EXIT_LOCKED);
                g.fillRect(cx, cy, cell, cell);
            }
        }

        private void drawEnemies(Graphics2D g, int cell, int ox, int oy) {
            for (Cop cop : engine.getCops()) {
                drawEntity(g, cop.getX(), cop.getY(), COL_COP, "C", cell, ox, oy);
                if (cop.isChasing() && cell >= 16) {
                    drawAlertMark(g, cop.getX(), cop.getY(), cell, ox, oy);
                }
            }
        }

        private void drawAlertMark(Graphics2D g, int gx, int gy,
                                   int cell, int ox, int oy) {
            final int[][] EXCLAIM = {
                {0, 1, 0}, {0, 1, 0}, {0, 1, 0}, {0, 1, 0},
                {0, 0, 0}, {0, 1, 0}, {0, 1, 0},
            };
            final Color[] PAL_EXCLAIM = { null, new Color(255, 30, 30) };

            int sz  = Math.max(6, cell / 2);
            int cx  = ox + gx * cell + (cell - sz) / 2;
            int cy  = oy + gy * cell - sz - 2;

            float pw = (float) sz / 3;
            float ph = (float) sz / 7;

            g.setColor(Color.WHITE);
            g.fillOval(cx - 2, cy - 2, sz + 4, sz + 4);
            g.setColor(new Color(180, 0, 0));
            g.drawOval(cx - 2, cy - 2, sz + 4, sz + 4);

            for (int row = 0; row < 7; row++) {
                for (int col = 0; col < 3; col++) {
                    int idx = EXCLAIM[row][col];
                    if (idx == 0 || PAL_EXCLAIM[idx] == null) continue;
                    g.setColor(PAL_EXCLAIM[idx]);
                    g.fillRect(cx + Math.round(col * pw),
                               cy + Math.round(row * ph),
                               Math.max(1, Math.round(pw)),
                               Math.max(1, Math.round(ph)));
                }
            }
        }

        private void drawEntity(Graphics2D g, int gx, int gy,
                                Color colour, String label,
                                int cell, int ox, int oy) {
            int cx = ox + gx * cell;
            int cy = oy + gy * cell;

            if (cell >= 10) {
                int copFrame = (int)((System.currentTimeMillis() / 250) % 2);
                switch (label) {
                    case "P":
                        drawPixelArt(g, cx, cy, cell,
                            playerAnimFrame == 0 ? SPRITE_PRISONER : SPRITE_PRISONER_WALK,
                            PAL_PRISONER); break;
                    case "C":
                        drawPixelArt(g, cx, cy, cell,
                            copFrame == 0 ? SPRITE_COP : SPRITE_COP_WALK,
                            PAL_COP); break;
                    default:
                        drawPixelArt(g, cx, cy, cell, SPRITE_COP, PAL_COP); break;
                }
            } else {
                int pad = Math.max(1, cell / 8);
                g.setColor(colour);
                g.fillOval(cx + pad, cy + pad, cell - 2*pad, cell - 2*pad);
                g.setColor(colour.darker());
                g.drawOval(cx + pad, cy + pad, cell - 2*pad, cell - 2*pad);
            }
        }

        private void drawPixelArt(Graphics2D g, int cx, int cy,
                                  int cell, int[][] sprite, Color[] palette) {
            int rows = sprite.length;
            int cols = sprite[0].length;
            int pad  = Math.max(1, cell / 10);
            float pw = (float)(cell - 2 * pad) / cols;
            float ph = (float)(cell - 2 * pad) / rows;

            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int idx = sprite[row][col];
                    if (idx == 0 || idx >= palette.length || palette[idx] == null) continue;
                    g.setColor(palette[idx]);
                    g.fillRect(cx + pad + Math.round(col * pw),
                               cy + pad + Math.round(row * ph),
                               Math.max(1, Math.round(pw)),
                               Math.max(1, Math.round(ph)));
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    public static void launch() {
        SwingUtilities.invokeLater(GameUI::new);
    }

    private static void styleMenuButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}