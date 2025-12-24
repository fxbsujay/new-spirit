package cn.spirit.go.model;

import cn.spirit.go.common.enums.GameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GameRoom {

    private static final Logger log = LoggerFactory.getLogger(GameRoom.class);
    /**
     * 游戏信息
     */
    public GamePlay info;

    /**
     * 步骤
     */
    public List<GameStep> steps = new ArrayList<>();

    /**
     * 客户端链接
     */
    public Set<GameSocket> sockets = new HashSet<>();

    /**
     * 白-每一步剩余时间的累计 为
     */
    public Long whiteRemainingTime = 0L;

    /**
     * 黑-每一步剩余时间的累计
     */
    public Long blackRemainingTime = 0L;

    /**
     * 用户这一步操作的剩余时间
     * @param timestamp  操作时间戳
     * @return 超时多长时间 小于0 为为超时
     */
    public long remainingTime(long timestamp) {
        if (GameType.NONE == info.type) {
            // 对局无时间限制
            throw new RuntimeException("Game type is NONE, Unable to calculate remaining duration");
        }
        int size = steps.size();
        if (size <= 2)  {
            // 对局前两手不计算时长
            throw new RuntimeException("The duration of the first two steps of a game match is not counted");
        }

        // 剩余时间 = 设定的每步加时时长 - (当前时间 - 开始计时时间))
        return info.stepDuration - (timestamp - steps.get(steps.size() - 1).timestamp);
    }

    /**
     * 步骤操作是否超时
     */
    public boolean isTimeout(long timestamp) {
        if (info.type == GameType.NONE || steps.size() <= 2) {
            return false;
        }
        return (remainingTime(timestamp) + (steps.size() % 2 == 0 ? blackRemainingTime : whiteRemainingTime) + info.duration) <= 0;
    }

    public boolean isTimeout() {
        return isTimeout(System.currentTimeMillis());
    }

    /**
     * 添加步骤并延续对方的超时时间
     * @param step  步骤
     */
    public boolean addStep(GameStep step) {
        if (steps.contains(step)) {
            return false;
        }
        if (info.type != GameType.NONE) {
            int size = steps.size();
            if (size > 2) {
                long remainingTime = remainingTime(step.timestamp) + (size % 2 == 0 ? blackRemainingTime : whiteRemainingTime);
                if (size % 2 == 0) {
                    blackRemainingTime += remainingTime;
                    if (blackRemainingTime + info.duration < 0) {
                        // TODO 黑方超时 白方胜
                        log.info("Black's time limit expired; White wins, time={}", blackRemainingTime);
                        return false;
                    }
                } else {
                    whiteRemainingTime += remainingTime;
                    if (whiteRemainingTime + info.duration < 0) {
                        // TODO 白方超时 黑方胜
                        log.info("White's time limit expired; Black wins time={}", whiteRemainingTime);
                        return false;
                    }
                }
            }
        }

        return steps.add(step);
    }

    /**
     * 游戏结束 操作超时, 认输, 棋盘已满
     */
    public void finish() {

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameRoom that = (GameRoom) o;
        return Objects.equals(info.code, that.info.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(info.code);
    }



}
