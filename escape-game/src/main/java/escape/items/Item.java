package escape.items;

/**
 * Represents an item that exists in the game and can be collected by the player.
 */
public class Item {
    private int value; 
    private String name;
    private String itemType;
    
    /**
     * Constructor for the item class.
     * 
     * @param value the value of this item
     * @param type specifies what type of item this is.
     */
    public Item(String name, int value, String type) {
        this.name = name;
        this.value = value;
        this.itemType = type;
    }

    /**
     * Grabs the value of this item.
     * 
     * @return returns the value of the item.
     */
    public int getValue() {
        return this.value;
    }
    
    public String getName() {
        return this.name;
    }

    /**
     * Grabs the type of this item.
     * 
     * @return the type of this item.
     */
    public String getType() {
        return this.itemType;
    }
    
}