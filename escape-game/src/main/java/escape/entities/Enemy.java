package escape.entities;

import escape.board.Board;

/*
* Abstract base class for the enemy types on the board (cop, corruptible cop).
*/
public abstract class Enemy extends Entity {

    /**
     * Constructor for where the enemy is going to start in the board.
     */
    public Enemy(int x, int y, Board board) {
        super(x, y, board);
    }

    /*
    * Abstract class for where enemy deals damage to the player.
    */
   public abstract void dealDamage(Player player);

}
