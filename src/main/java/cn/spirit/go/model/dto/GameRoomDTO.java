package cn.spirit.go.model.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

public class GameRoomDTO {

    /**
     * 游戏信息
     */
    public GamePlayDTO info;

    /**
     * 步骤
     */
    public Set<GameStepDTO> steps = new HashSet<>();

    /**
     * 客户端链接
     */
    public Set<GameSocket> sockets = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameRoomDTO that = (GameRoomDTO) o;
        return Objects.equals(info.code, that.info.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(info.code);
    }

}
