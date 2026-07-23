package escape.items;

/**
 * Represents one of three parts needed to assemble a shovel.
 *
 * The three parts are:
 * <ul>
 *   <li>"Handle"     — found in Level 1</li>
 *   <li>"Stick"      — found in Level 2</li>
 *   <li>"Shovel Head"— found in Level 3</li>
 * </ul>
 * Collecting all three by the end of Level 4 is required to win the game.
 */
public class ShovelPart extends Item {

    /** The display name of this specific part (e.g. "Handle"). */
    private final String partName;

    /**
     * Constructs a ShovelPart item.
     *
     * @param partName one of "Handle", "Stick", or "Shovel Head"
     */
    public ShovelPart(String partName) {
        super(partName, 0, "SHOVEL_PART");
        this.partName = partName;
    }

    /**
     * Returns the name of this shovel part.
     *
     * @return part name string, e.g. "Handle"
     */
    public String getPartName() {
        return partName;
    }
}