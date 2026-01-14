package cn.spirit.go.model;

import cn.spirit.go.common.enums.ChessPiece;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.web.config.AppContext;
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
    public boolean isTimeout(long timestamp, ChessPiece piece) {
        if (info.type == GameType.NONE || steps.size() <= 2) {
            return false;
        }
        return (remainingTime(timestamp) + (steps.size() % 2 == 0 ? blackRemainder : whiteRemainder) + info.duration) <= 0;
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
                long time = remainingTime(step.timestamp);
                if (size % 2 == 0) {
                    long remainder = blackRemainder + time;
                    if (remainder <= 0) {
                        // TODO 黑方超时 白方胜
                        log.info("Black's time limit expired; White wins, time={}", blackRemainder);
                        return false;
                    } else {
                        blackRemainder = remainder;
                        // TODO 定时任务 {whiteRemainder} 毫秒后白棋未走起则黑棋胜，游戏结束
                    }
                } else {
                    long remainder = whiteRemainder + time;
                    if (remainder <= 0) {
                        // TODO 白方超时 黑方胜
                        log.info("White's time limit expired; Black wins time={}", whiteRemainder);
                        return false;
                    } else {
                        whiteRemainder = remainder;
                        // TODO 定时任务 {blackRemainder} 毫秒后黑棋未走起则白棋胜，游戏结束
                        AppContext.vertx.setTimer(blackRemainder, id -> {
                            log.info("id={}", id);
                            if (whiteRemainder <= 0) {

                            }
                        });
                    }

                }

                log.info("B time={}分钟{}秒", (blackRemainder) / 1000 / 60 % 60, (blackRemainder) / 1000 % 60);
                log.info("W time={}分钟{}秒", (whiteRemainder ) / 1000 / 60 % 60, (whiteRemainder) / 1000 % 60);
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
