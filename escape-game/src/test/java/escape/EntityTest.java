package escape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import escape.board.Board;
import escape.entities.Player;

import static org.junit.jupiter.api.Assertions.*;

public class EntityTest {
    private Board board;
    private Player player;

    /**
     * Initialization for setting up the board and player for testing
     */
    @BeforeEach
    private void setup() {
        this.board = new Board(1);
        this.player = new Player(9, 9, this.board);
    }

    /**
     * Check to see if the player can actually properly move to the right
     */
    @Test
    public void playerWalkRight() {
        this.player.move(1,0);
        assertEquals(10, this.player.getX());
        assertEquals(9, this.player.getY());
    }

    /**
     * Check to see if the player can actually properly move to the left
     */
    @Test
    public void playerWalkLeft() {
        this.player.move(-1,0);
        assertEquals(8, this.player.getX());
        assertEquals(9, this.player.getY());
    }

    /**
     * Check to see if the player can move up
     */
    @Test
    public void playerWalkUp() {
        this.player.move(0,1);
        assertEquals(9, this.player.getX());
        assertEquals(10, this.player.getY());
    }

    /**
     * Check to see if the player can move down
     */
    @Test
    public void playerWalkDown() {
        this.player.move(0,-1);
        assertEquals(9, this.player.getX());
        assertEquals(8, this.player.getY());
    }

    /**
     * Check to see if the player that the player does not walk into the wall
     */
    @Test
    public void playerTriesToMoveIntoBlockedGrid() {
        board.getCell(10, 9).setWall();
        this.player.move(1, 0);
        assertEquals(9, player.getX());
        assertEquals(9, player.getY());
    }

    /**
     * Check to see that the player does not walk out of bounds
     */
    @Test
    public void playerTriesToWalkOutOfBounds() {
        this.player = new Player(0, 0, board);
        player.move(-1, 0);
        assertEquals(0, player.getX());
        assertEquals(0, player.getY());
    }
}
