package escape.entities;
import java.util.ArrayList;

import escape.board.Board;
import escape.board.Cell;
import escape.items.Item;

/**
 * The main character of this game, basically the prisoner that the user will play as.
 */
public class Player extends Entity {
    private ArrayList<Item> bag;
    private int health;
    private static final int MAX_HEALTH = 100;

    /**
     * Constructs the player with the given starting position.
     * @param x current x-coordinate of the player.
     * @param y current y-coordinate of the player.
     * @param board the game board where the player exist.
     */
    public Player(int x, int y, Board board) {
        super(x, y, board);
        this.bag = new ArrayList<Item>();
        this.health = MAX_HEALTH;
        this.board = board;
    }

    /**
     * To move the entity to the next movement, unless if the next cell is blocked, then 
     * the player would not change distance.
     * 
     * @param distX the new x-coordinate of the user.
     * @param distY the new y-coordinate of the user.
     */
    @Override
    public void move(int distX, int distY) {
        int newX = this.x + distX;
        int newY = this.y + distY;

        Cell newCell = board.getCell(newX, newY);
        if (newCell != null) {
            if(newCell.isBlocked()) {
                return;
            }
            this.x = newX;
            this.y = newY;
        }
    }

    /**
     * To add item to the player's inventory when stepping on a grid that contains an item.
     */
    public void collectItem() {
        Cell cell = this.board.getCell(this.x, this.y);
        if (cell != null && cell.getItem() != null) {
            Item item = cell.getItem();
            this.bag.add(item);
            System.out.println("Collected: " + item);
        }
    }

    /**
     * To view what items the user currently have.
     * 
     * @return the items the user currently have.
     */
    public ArrayList<Item> viewItems() {
        System.out.println(this.bag.isEmpty());
        return bag;
    }

    /**
     * To return how much health the user currently have.
     * 
     * @return current health of the player.
     */
    public int getHealth() {
        return this.health;
    }

    /**
     * Restores health by the given amount, capped at 100.
     *
     * @param amount the number of HP to restore.
     */
    public void heal(int amount) {
        this.health = Math.min(MAX_HEALTH, this.health + amount);
        System.out.println("Healed +" + amount + " HP! HP: " + this.health);
    }

    /**
     * A function to update the player's health and check if the player would be considered dead.
     *
     * @param damage the amount of damage the user dealt from an enemy.
     */
    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            System.out.println("you are dead");
        } else {
            System.out.println("You took damage!");
        }
    }
}
