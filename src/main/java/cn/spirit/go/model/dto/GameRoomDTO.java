package cn.spirit.go.model.dto;

import cn.spirit.go.model.entity.GameEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameRoomDTO {

    /**
     * 游戏信息
     */
    public GameEntity info;

    /**
     * 观战席
     */
    public Set<String> sessionIds = new HashSet<>();

    /**
     * 步骤
     */
    public List<String> steps = new ArrayList<>();

    /**
     * 聊天历史消息
     */
    public List<String> messages = new ArrayList<>();
}
