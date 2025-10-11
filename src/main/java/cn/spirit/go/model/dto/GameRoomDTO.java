package cn.spirit.go.model.dto;

import cn.spirit.go.model.entity.GameEntity;
import java.util.ArrayList;
import java.util.List;

public class GameRoomDTO {

    /**
     * 游戏信息
     */
    public GameEntity info;

    /**
     * 步骤 0412B:1243W
     */
    public List<String> steps = new ArrayList<>();

    /**
     * 聊天历史消息
     */
    public List<String> messages = new ArrayList<>();
}
