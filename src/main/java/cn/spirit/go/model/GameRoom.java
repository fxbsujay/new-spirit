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
    public Long whiteRemainder = 0L;

    /**
     * 黑-每一步剩余时间的累计
     */
    public Long blackRemainder = 0L;

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
     * 步骤操作是否超时
     */
    public boolean isTimeout(long timestamp) {
        if (info.type == GameType.NONE || steps.size() <= 2) {
            return false;
        }
        return (remainingTime(timestamp) + (steps.size() % 2 == 0 ? blackRemainder : whiteRemainder) + info.duration) <= 0;
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
            if (size > 1) {
                if (size % 2 == 0) {
                    long remainder = blackRemainder + remainingTime(step.timestamp);
                    if (remainder + info.duration < 0) {
                        // TODO 黑方超时 白方胜
                        log.info("Black's time limit expired; White wins, time={}", blackRemainder);
                        return false;
                    } else {
                        blackRemainder = remainder;
                    }
                } else {
                    long remainder = whiteRemainder + remainingTime(step.timestamp);
                    whiteRemainder += remainingTime(step.timestamp);
                    if (remainder + info.duration < 0) {
                        // TODO 白方超时 黑方胜
                        log.info("White's time limit expired; Black wins time={}", whiteRemainder);
                        return false;
                    } else {
                        whiteRemainder = remainder;
                    }
                }

                log.info("B time={}分钟{}秒", (blackRemainder + info.duration) / 1000 / 60 % 60, (blackRemainder + info.duration) / 1000 % 60);
                log.info("W time={}分钟{}秒", (whiteRemainder + info.duration) / 1000 / 60 % 60, (whiteRemainder + info.duration) / 1000 % 60);
            }
        }

        return steps.add(step);
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
