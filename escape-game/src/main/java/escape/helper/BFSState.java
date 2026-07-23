package escape.helper;
import java.util.Queue;

public class BFSState {
    public boolean[][] visited;
    public int[][] stepX;
    public int[][] stepY;
    public Queue<int[]> queue;

    public BFSState(boolean[][] visited, int[][] stepX, int[][] stepY, Queue<int[]> queue) {
        this.visited = visited;
        this.stepX = stepX;
        this.stepY = stepY;
        this.queue = queue;
    }
}
