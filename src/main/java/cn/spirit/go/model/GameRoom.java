package cn.spirit.go.model;

import cn.spirit.go.common.enums.GameType;
import io.vertx.core.json.JsonObject;
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
     * 白棋玩家信息
     */
    public Player white;

    /**
     * 黑棋玩家信息
     */
    public Player black;

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

    public static class Player {

        public String nickname;

        public String username;

        public String avatar;

        public Integer rating;

        public JsonObject toJson() {
            return JsonObject.of(
                    "nickname", nickname,
                    "username",username,
                    "avatar", avatar,
                    "rating", rating);
        }
    }

}
