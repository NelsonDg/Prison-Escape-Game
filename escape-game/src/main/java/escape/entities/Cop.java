package escape.entities;

import java.util.LinkedList;

import escape.board.Board;
import escape.board.Cell;
import escape.helper.BFSState;

/**
 * Class for the cop, the cop will walk back and forth unless it sees the player, then it 
 * will try to chase the player and deal damage.
 */
public class Cop extends Enemy {
    private boolean isCopChasing;
    private boolean horizontalBackAndForth;
    private int movement = 1;
    private static final int DAMAGE = 10;
    private static final int VISION_RANGE = 5;
    private static final int[][] DIRECTIONS = {
        {1,0}, {-1,0}, {0,1}, {0,-1},
        {1,1}, {1,-1}, {-1,1}, {-1,-1}
    };

    /**
     * Constructor for cop.
     * 
     * @param x the location of the cop in the x axis.
     * @param y the location of the cop in the y axis.
     * @param board the board where the cop exists.
     */
    public Cop(int x, int y, Board board) {
        super(x, y, board);
        this.isCopChasing = false;

        Cell left = board.getCell(x-1, y);
        Cell right= board.getCell(x+1, y);

        if (left != null && !left.isBlocked() || right != null && !right.isBlocked()) {
            this.horizontalBackAndForth = true;
        } else {
            this.horizontalBackAndForth = false;
        }
    }

    /**
     * Returns true if this cop is currently chasing the player.
     * Used by GameUI to show the alert exclamation mark above the cop.
     *
     * @return {@code true} if the cop is chasing
     */
    public boolean isChasing() {
        return isCopChasing;
    }

    /**
     * If the player is not in the vision of the cop, then the cop will just walk back and forth.
     */
    public void backAndForth(Player player) {
        int dx;
        int dy;

        if (horizontalBackAndForth) {
            dx = movement;
            dy = 0;
        } else {
            dx = 0;
            dy = movement;
        }

        Cell nextCell = board.getCell(this.x + dx, this.y + dy);
        if (nextCell == null || nextCell.isBlocked()) {
            movement = -movement;
            dx = -dx;
            dy = -dy;
            nextCell = board.getCell(this.x + dx, this.y + dy);
        }

        if (nextCell != null && !nextCell.isBlocked()) {
            this.x += dx;
            this.y += dy;
        }
    }

    /**
     * Just a movement logic where if the player is seen  by the cop, they will chase the player.
     * 
     * @param player - the character playing the game.
     */
    public void chasePlayer(Player player) {
        Cell nextStep = useBFS(player);
        if (nextStep != null) {
            this.x = nextStep.getX();
            this.y = nextStep.getY();
        }
    }
    
    /**
     * To deal damage to the player by subtracting their health by 10.
     * 
     * @param player the character playing the game.
     */
    @Override
    public void dealDamage(Player player) { 
        System.out.println("Cop did " + DAMAGE + " damage");
        player.takeDamage(DAMAGE);
    }

    /**
     * Returns true if the cop has a clear line of sight to the player in any
     * of the four cardinal directions, up to 5 cells away.  A wall tile blocks
     * the sight line in that direction.
     *
     * @param player the character playing the game.
     * @return {@code true} if the player is visible
     */
    public boolean seesPlayer(Player player) {
        int playerX = player.getX();
        int playerY = player.getY();

        // int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] dir : DIRECTIONS) {
            for (int i = 1; i <= VISION_RANGE; i++) {
                int newX = this.x + dir[0] * i;
                int newY = this.y + dir[1] * i;

                Cell cell = board.getCell(newX, newY);
                if (cell == null || cell.isBlocked()) break; // wall blocks this ray

                if (playerX == newX && playerY == newY) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The movement for cop, while the player is not in vision, then cop will just move back and forth, until it sees the player
     * in its vision, then it will start chasing the player.
     * 
     * @param player the character playing the game.
     */
    public void move(Player player) {
        if (!isCopChasing && seesPlayer(player)) {
            this.isCopChasing = true;
        }

        if (this.isCopChasing) {
            chasePlayer(player);
        } else {
            backAndForth(player);
        }
    }

    /**
     * returns the boolean value of if the cop is chasing
     * @return is cop chasing
     */
    public boolean getIsCopChasing() {
        return this.isCopChasing;
    }

    /**
     * Uses BFS logic in order to chase the player via shortest path
     * 
     * @param player that is being chased by the cop
     */
    private Cell useBFS(Player player) {
        BFSState state = initBFS();

        while (!state.queue.isEmpty()) {
            int[] current = state.queue.poll();
            Cell result = nextCellTowardsPlayer(current, state, player);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Initializes BFS to be used to traverse the map and find shortest path.
     * 
     * @return BFSState returns the initialized state of the BFS
     */
    private BFSState initBFS() {
        int rows = board.getBoardHeight();
        int cols = board.getBoardWidth();   

        BFSState state = new BFSState(
            new boolean[cols][rows],
            new int[cols][rows],
            new int[cols][rows],
            new LinkedList<int[]>()
        );
        
        state.visited[this.x][this.y] = true;
        state.queue.add(new int[]{this.x, this.y});
        return state;
    }

    private Cell nextCellTowardsPlayer(int[] current, BFSState state, Player player) {
        int cx = current[0];
        int cy = current[1];

        for (int[] dir: DIRECTIONS) {
            int nx = cx + dir[0];
            int ny = cy + dir[1];

            if (!isValidMove(nx, ny, state.visited)) {
                continue;
            }

            state.visited[nx][ny] = true;
            findNextMove(cx, cy, nx, ny, state);

            if (nx == player.getX() && ny == player.getY()) {
                return board.getCell(state.stepX[nx][ny], state.stepY[nx][ny]);
            }
            state.queue.add(new int[]{nx, ny});
        }
        return null;
    }

    /**
     * Checks if the cell exist or not blocked
     * 
     * @return boolean value whether the next cell is available or not
     */
    private boolean isValidMove(int nx, int ny, boolean[][] visited) {
        Cell cell = board.getCell(nx, ny);
        if (cell == null || cell.isBlocked() || visited[nx][ny]) {
            return false;
        }
        return true;
    }

    /**
     * Method that stores the first move from the start to reach each cell
     * in order to reach the proper next move towards the player
     */
    private void findNextMove(int cx, int cy, int nx, int ny, BFSState state) {
        if (cx == this.x && cy == this.y) {
            state.stepX[nx][ny] = nx;
            state.stepY[nx][ny] = ny;
        } else { 
            state.stepX[nx][ny] = state.stepX[cx][cy];
            state.stepY[nx][ny] = state.stepY[cx][cy];
        }
    }
}
