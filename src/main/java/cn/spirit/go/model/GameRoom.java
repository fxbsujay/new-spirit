package cn.spirit.go.model;

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
