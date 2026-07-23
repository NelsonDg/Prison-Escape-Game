package escape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import escape.board.Board;
import escape.entities.Cop;
import escape.entities.Player;

import static org.junit.jupiter.api.Assertions.*;

public class CopTest {
    private Board board;
    private Player player;
    private Cop cop;

    /**
     * The set up for testing the code
     */
    @BeforeEach
    private void setup() {
        this.board = new Board(1);
        this.player = new Player(9, 9, board);
        this.cop = new Cop(7, 7, board);
    }

    /**
     * To test that the cop actually sees the player when in vision
     */
    @Test
    public void copSeesPlayer() {
        player = new Player(7,5, board);
        assertTrue(cop.seesPlayer(player));
    }

    /**
     * to test that the cop properly deals damage to the player
     */
    @Test
    public void copDealsDamageToPlayer() {
        cop.dealDamage(player);
        assertEquals(90, player.getHealth());
    }

    /**
     * Test that checks if the cop can deal multiple damage at player
     */
    @Test
    public void copDealsDamageMultipletimes() {
        cop.dealDamage(player);
        cop.dealDamage(player);
        assertEquals(80, player.getHealth());
    }

    /**
     * To see that the cop normally moves when user is not in vision.
     */
    @Test
    void copPatrolsWhenPlayerNotVisible() {
        int startX = cop.getX();
        cop.move(player); 
        assertEquals(startX + 1, cop.getX()); 
    }

    /**
     * To see that the cop goes back to patrolling after hitting the player
     */
    @Test
    void copResetsToPatrolAfterHittingPlayer() {
        player = new Player(6, 5, board);
        cop.move(player);
        assertFalse(cop.getIsCopChasing());
        assertFalse(cop.seesPlayer(player));
    }
}
