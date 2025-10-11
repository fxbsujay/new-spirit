package cn.spirit.go.service;

import cn.spirit.go.model.dto.GameRoomDTO;
import cn.spirit.go.model.entity.GameEntity;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.socket.ClientManger;
import cn.spirit.go.web.socket.PackageType;
import cn.spirit.go.web.socket.SocketPackage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameRoomService {

    private final ClientManger clientManger = AppContext.getBean(ClientManger.class);

    /**
     * 房间信息
     */
    private final Map<String, GameRoomDTO> rooms = new HashMap<>();

    /**
     * 订阅信息
     * sessionId:游戏编号
     */
    private final Map<String, Set<String>> subscribers = new HashMap<>();

    /**
     * 玩家落子
     *
     * @param code      房间编号
     * @param username  玩家用户名
     * @param step      0412-B-1760178234 纵坐标横坐标-棋子类型B黑W白-落子时间戳
     */
    public boolean addStep(String code, String username, String step) {
        GameRoomDTO room = rooms.get(code);
        if (null == room) {
            return false;
        }

        if (room.steps.isEmpty()) {
            // TODO 没有落子，判断是不是黑棋先行
        }

        String lastStep = room.steps.get(room.steps.size() - 1);
        // TODO 最后一步必须是另一方的落子，并判断落子位置是否重叠了


        return true;
    }

    /**
     * 订阅这场比赛
     *
     * @param code        对局编号
     * @param sessionId   加入房间的客户端
     */
    public boolean subscribe(String sessionId, String code) {
        GameRoomDTO room = rooms.get(code);
        if (null == room) {
            return false;
        }

        Set<String> codes = subscribers.get(sessionId);
        if (null == codes) {
            codes = new HashSet<>();
            codes.add(code);
            subscribers.put(sessionId, codes);
        } else {
            codes.add(code);
        }
        return true;
    }

    /**
     * 取消订阅
     *
     * @param code        对局编号
     * @param sessionId   加入房间的客户端
     */
    public void unsubscribe(String sessionId, String code) {
        Set<String> codes = subscribers.get(sessionId);
        if (null != codes) {
            codes.remove(code);
        }
    }

    /**
     * 取消客户端的全部订阅，如客户端断线
     */
    public void unsubscribeAll(String sessionId) {
        subscribers.remove(sessionId);
    }

    /**
     * 添加房间，创建房间后通知玩家游戏开始，随后进入游戏界面，通知服务器玩家已进入房间，黑旗先行，双方都落子后游戏正式开始，不可取消
     * 进入游戏界面客户端发送加入房间通知，关闭游戏界面发生退出房间通知
     */
    public String add(GameEntity info) {
        GameRoomDTO dto = new GameRoomDTO();
        dto.info = info;
        rooms.put(info.code, dto);
        clientManger.sendToUser(SocketPackage.build(PackageType.GAME_START, info.code), info.white, info.black);
        return info.code;
    }


    /**
     * 关闭房间
     */
    public void close(String code) {
        rooms.remove(code);
    }

    /**
     * 获取房间
     */
    public GameRoomDTO get(String code) {
        return rooms.get(code);
    }

}
