package cn.spirit.go.service;

import cn.spirit.go.model.dto.GameRoomDTO;
import cn.spirit.go.model.entity.GameEntity;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.socket.ClientManger;
import cn.spirit.go.web.socket.PackageType;
import cn.spirit.go.web.socket.SocketPackage;
import java.util.HashMap;
import java.util.Map;

public class GameRoomService {

    private final ClientManger clientManger = AppContext.getBean(ClientManger.class);

    /**
     * 房间信息
     */
    private final Map<String, GameRoomDTO> rooms = new HashMap<>();

    /**
     * 添加房间，创建房间后通知玩家游戏开始，随后进入游戏界面，通知服务器玩家已进入房间，黑旗先行，双方都落子后游戏正式开始，不可取消
     */
    public String add(GameEntity info) {
        GameRoomDTO dto = new GameRoomDTO();
        dto.info = info;
        rooms.put(info.code, dto);
        clientManger.sendToUser(SocketPackage.build(PackageType.GAME_START, info.code), info.white, info.black);
        return info.code;
    }

    /**
     * 加入房间
     *
     * @param code        对局编号
     * @param sessionId   客户端ID
     */
    public void join(String code, String sessionId) {

    }

    public void close(String code) {
        rooms.remove(code);
    }

    public GameRoomDTO get(String code) {
        return rooms.get(code);
    }
}
