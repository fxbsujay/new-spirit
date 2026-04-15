package cn.spirit.go.model;

import cn.spirit.go.common.enums.GameType;

import java.util.*;

public class Room {

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
    public Long whiteRemainder = 0L;

    /**
     * 黑-每一步剩余时间的累计
     */
    public Long blackRemainder = 0L;

    /**
     * 棋盘棋子
     */
    public char[][] board = new char[21][21];

    /**
     * 步骤
     */
    public List<GameStep> steps = new ArrayList<>();

    /**
     * 客户端链接
     */
    public Set<RoomSocket> sockets = new HashSet<>();


    /**
     * 用户这一步操作所用时长
     * @param timestamp  操作时间戳
     * @return 超时多长时间 小于0 为为超时
     */
    public long remainingTime(long timestamp) {
        if (GameType.NONE == info.type) {
            // 对局无时间限制
            throw new RuntimeException("Game type is NONE, Unable to calculate remaining duration");
        }
        int size = steps.size();
        if (size == 0)  {
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

}
