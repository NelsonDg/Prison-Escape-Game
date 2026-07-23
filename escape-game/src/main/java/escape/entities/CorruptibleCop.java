package escape.entities;
import java.util.Iterator;

import escape.board.Board;
import escape.items.Item;
/**
 * A type of cop but does not move and can be bribed by player to progress into the game further.
 */
public class CorruptibleCop extends Cop {
    /**
     * Constructor for this corruptible cop
     * 
     * @param x the location of the cop in the x axis.
     * @param y the location of the cop in the y axis.
     * @param board the board where the cop exists.
     */
    public CorruptibleCop(int x, int y, Board board) {
        super(x, y, board);
    }

    /**
     * Function that gives the player the item in order to progress in the game.
     * 
     * @param player the player 
     */
    public void giveEscapeItem(Player player) {
        //if item is the right bribery item
        boolean hasItem = false;
        for (Item item: player.viewItems()) {
            if (item.getType().equals("BRIBE")) {
                hasItem = true;
                break;
                // player.viewItems().remove(item); //remove the item now
            }
        }

        if (hasItem) {
        Iterator<Item> it = player.viewItems().iterator();

        while (it.hasNext()) {
            Item item = it.next();
            if (item.getType().equals("BRIBE")) {
                it.remove(); // safe removal
            }
        }
        System.out.println("The cop was bribed...");
        player.viewItems().add(new Item("KEY", 50, "ESCAPEKEY"));
        System.out.println("SUCCESSFULLY BRIBED COP!");
        return;
    }
    
    System.out.println("Failed to bribe the cop...");
    }
}
