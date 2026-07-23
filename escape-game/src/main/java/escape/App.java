package escape;

import escape.main.GameEngine;
import escape.main.GameUI;

/**
 * Application entry point for the Escape prison-escape game.
 *
 * Launches the Swing graphical interface via {@link GameUI}.
 * All rendering and input handling live in {@link GameUI}; all game logic
 * lives in {@link GameEngine}.
 */
public class App {

    /**
     * Main entry point.  Starts the Swing UI on the Event Dispatch Thread.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        GameUI.launch();
    }
}
