package cn.spirit.go.model;

import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameType;

import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

public class GameRoom {

    /**
     * 游戏信息
     */
    public GamePlay info;

    /**
     * 步骤
     */
    public Set<GameStep> steps = new HashSet<>();

    /**
     * 客户端链接
     */
    public Set<GameSocket> sockets = new HashSet<>();

    /**
     * 白-剩余时间 毫秒 -1 为未开始计时或无需计时
     */
    public Long whiteTime = -1L;

    /**
     * 黑-剩余时间 毫秒 -1 为未开始计时或无需计时
     */
    public Long blackTime = -1L;

    /**
     * 用户操作超时了
     * @param username  玩家用户名
     */
    public boolean isTimeOut(String username) {
        if (GameType.NONE == info.type) {
            // 对局无时间限制或对局处在开始阶段
            return false;
        }

        if (info.white.equals(username)) {

        }

        return false;
    }

    /**
     * 添加步骤并延续对方的超时时间
     * @param step  步骤
     */
    public boolean addStep(GameStep step) {
        if (GameType.NONE != info.type) {

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
