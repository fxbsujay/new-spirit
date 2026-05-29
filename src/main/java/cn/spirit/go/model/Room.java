package cn.spirit.go.model;

import cn.spirit.go.common.enums.GameType;
import java.util.*;

public class Room {

    public static final char EMPTY = '.';

    public static final char BLACK = 'B';

    public static final char WHITE = 'W';

    public static final char[] LOCATION = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    private static final int[][] DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    /**
     * 基本信息
     */
    public RoomInfo info;

    /**
     * 白棋用户
     */
    public String white;

    /**
     * 黑棋用户
     */
    public String black;

    /**
     * 白-每一步剩余时间的累计
     */
    public long whiteRemainder = 0L;

    /**
     * 黑-每一步剩余时间的累计
     */
    public long blackRemainder = 0L;

    /**
     * 白棋提子数量
     */
    public int whiteCaptured = 0;

    /**
     * 黑棋提子数量
     */
    public int blackCaptured = 0;

    /**
     * 棋盘棋子
     */
    public char[][] board;

    /**
     * 定时器ID
     */
    public Long timerId;

    /**
     * 步骤
     */
    public List<GameStep> steps = new ArrayList<>();

    /**
     * 客户端链接
     */
    public Set<RoomSocket> sockets = new HashSet<>();

    public Room(int size) {
        board = new char[size][size];
        for (char[] chars : board) {
            Arrays.fill(chars, '.');
        }
    }

    /**
     * 用户这一步操作所用时长
     *
     * @param timestamp 操作时间戳
     * @return 超时多长时间 小于0 为为超时
     */
    public long remainingTime(long timestamp) {
        if (GameType.NONE == info.type) {
            // 对局无时间限制
            throw new RuntimeException("Game type is NONE, Unable to calculate remaining duration");
        }
        int size = steps.size();
        if (size == 0) {
            // 对局前两手不计算时长
            throw new RuntimeException("The duration of the first two steps of a game match is not counted");
        }

        // 剩余时间 = 设定的每步加时时长 - (当前时间 - 开始计时时间))
        return info.stepDuration - (timestamp - steps.get(steps.size() - 1).timestamp);
    }

    /**
     * 现在是否是白棋走棋
     */
    public boolean isWhiteNow() {
        return steps.size() % 2 == 1;
    }

    /**
     * 落子，返回是否成功
     */
    public boolean place(int x, int y, char color) {
        int size = board.length;
        if (x < 0 || x >= size || y < 0 || y >= size || board[x][y] != EMPTY) {
            return false;
        }
        char opp = color == BLACK ? WHITE : BLACK;
        board[x][y] = color;
        if (steps.size() < 4) {
            return true;
        }

        int captured = removeDeadGroups(board, opp);
        if (captured > 0) {
            if (color == BLACK) {
                blackCaptured += captured;
            } else {
                whiteCaptured += captured;
            }
        } else {
            // 没有气 + 没提子 = 禁入点，撤销
            if (!groupHasLiberty(board, x, y, new boolean[size][size])) {
                board[x][y] = EMPTY;
                return false;
            }
        }

        return true;
    }

    /**
     * 移除所有没有气的棋子，返回提子数量
     */
    private int removeDeadGroups(char[][] board, char color) {
        int size = board.length;
        boolean[][] visited = new boolean[size][size];
        int captured = 0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == color && !visited[i][j]) {
                    List<int[]> group = getGroup(board, i, j, visited);
                    if (!groupHasLiberty(board, group)) {
                        for (int[] p : group) board[p[0]][p[1]] = EMPTY;
                        captured++;
                    }
                }
            }
        }
        return captured;
    }

    /**
     * 判断单个棋子是否有气
     */
    private boolean groupHasLiberty(char[][] board, int x, int y, boolean[][] visited) {
        return groupHasLiberty(board, getGroup(board, x, y, visited));
    }

    /**
     * 判断全部棋子是否有气
     */
    private boolean groupHasLiberty(char[][] board, List<int[]> group) {
        for (int[] p : group) {
            int x = p[0], y = p[1];
            for (int[] d : DIRS) {
                int nx = x + d[0], ny = y + d[1];
                if (inBound(board, nx, ny) && board[nx][ny] == EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取周围的同色棋子
     */
    private List<int[]> getGroup(char[][] board, int x, int y, boolean[][] visited) {
        List<int[]> group = new ArrayList<>();
        if (!inBound(board, x, y) || board[x][y] == EMPTY || visited[x][y]) {
            return group;
        }

        char c = board[x][y];
        Queue<int[]> q = new ArrayDeque<>();
        q.add(new int[]{x, y});
        visited[x][y] = true;

        while (!q.isEmpty()) {
            int[] p = q.poll();
            int cx = p[0], cy = p[1];
            group.add(p);

            for (int[] d : DIRS) {
                int nx = cx + d[0], ny = cy + d[1];
                if (inBound(board, nx, ny) && !visited[nx][ny] && board[nx][ny] == c) {
                    visited[nx][ny] = true;
                    q.add(new int[]{nx, ny});
                }
            }
        }
        return group;
    }

    private boolean inBound(char[][] board, int x, int y) {
        return x >= 0 && x < board.length && y >= 0 && y < board[0].length;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Room that = (Room) o;
        return Objects.equals(info.code, that.info.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(info.code);
    }

    public void outPrintBoard() {
        int size = board.length;

        // 顶部：X 坐标
        System.out.print("   ");
        for (int x = 0; x < size; x++) {
            System.out.printf("%2d", x);
        }
        System.out.println();
        for (int y = 0; y < size; y++) {
            System.out.printf("%2d ", y);
            for (int x = 0; x < size; x++) {
                char c = board[x][y];
                switch (c) {
                    case EMPTY:
                        System.out.print(" .");
                        break;
                    case BLACK:
                        System.out.print(" ●");
                        break;
                    case WHITE:
                        System.out.print(" ○");
                        break;
                    default:
                        System.out.print(" ?");
                }
            }
            System.out.println();
        }
    }

}
