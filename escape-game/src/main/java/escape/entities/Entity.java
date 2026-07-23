package escape.entities;

import escape.board.Board;

/**
 * Abstract base class for the entities that exist in the game.
 */
public abstract class Entity {
    protected int x;
    protected int y;
    protected Board board;

    /**
     * Constructor for the entity, showcases their starting position on the board.
     * @param x the x coordinate of this entity.
     * @param y the y coordinate of this entity.
     * @param board the game board where this entity exist.
     */
    public Entity(int x, int y, Board board) {
        this.x = x;
        this.y = y;
        this.board = board;
    }

    /**
     * for moving the entity to a new x and y coordinate. 
     * @param distanceX the new offset for x coordinate.
     * @param distanceY the new offset for y coordinate.
     */
    public void move(int distanceX, int distanceY) {};

    /**
     * Returns the entity's current x-coordinate.
     * 
     * @return the x-coordinate of this entity.
     */
    public int getX(){
        return this.x;
    }

    /**
     * Returns the entity's current y-coordinate.
     * 
     * @return the y-coordinate of this entity.
     */
    public int getY(){
        return this.y;
    }
}
