package cn.spirit.go.model.dto;

import java.util.ArrayList;
import java.util.List;

public class GameRoomDTO {

    /**
     * 游戏信息
     */
    public GamePlayDTO info;

    /**
     * 步骤
     */
    public List<String> steps = new ArrayList<>();

    /**
     * 聊天历史消息
     */
    public List<String> messages = new ArrayList<>();
}
