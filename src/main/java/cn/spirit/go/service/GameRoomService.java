package cn.spirit.go.service;

import cn.spirit.go.model.dto.GamePlayDTO;
import cn.spirit.go.model.dto.GameRoomDTO;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.socket.ClientManger;
import cn.spirit.go.web.socket.PackageType;
import cn.spirit.go.web.socket.SocketPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GameRoomService {

    private final Logger log = LoggerFactory.getLogger(GameRoomService.class);

    private final ClientManger clientManger = AppContext.getBean(ClientManger.class);

    /**
     * 房间信息
     */
    private final Map<String, GameRoomDTO> rooms = new HashMap<>();

    /**
     * 用户房间
     */
    private final Map<String, Set<String>> userRooms = new HashMap<>();

    /**
     * 订阅信息
     * sessionId:游戏编号
     */
    private final Map<String, Set<String>> subscribers = new HashMap<>();

    /**
     * 玩家落子
     * 0412-1760178234 横坐标纵坐标(x,y)-落子时间戳
     *
     * @param code      房间编号
     * @param username  玩家用户名
     * @param x         纵坐标
     * @param y         纵坐标
     */
    public boolean addStep(String username, String code, Integer x, Integer y) {
        log.info("game add step ,username={}, code={}, x={}, y={}", username, code, x, y);
        GameRoomDTO room = rooms.get(code);
        if (null == room) {
            return false;
        }

        if (room.steps.isEmpty()) {
            // TODO 没有落子，判断是不是黑棋先行
        }

        //String lastStep = room.steps.get(room.steps.size() - 1);
        // TODO 最后一步必须是另一方的落子，并判断落子位置是否重叠了


        clientManger.sendToUser(SocketPackage.build(PackageType.GAME_STEP, room.info.black, room.info.white));
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
    public String add(GamePlayDTO info) {
        GameRoomDTO dto = new GameRoomDTO();
        dto.info = info;
        rooms.put(info.code, dto);
        clientManger.sendToUser(SocketPackage.build(PackageType.GAME_START, info.code), info.white, info.black);
        return info.code;
    }

    /**
     * 加入房间
     */
    public void joinRoom(String username, String code) {
        GameRoomDTO room = rooms.get(code);
        if (null == room || (!room.info.white.equals(username) && !room.info.black.equals(username))) {
            return;
        }
        Set<String> codes = userRooms.get(username);
        boolean sendMsg = false;
        if (null == codes) {
            codes = new HashSet<>();
            codes.add(code);
            userRooms.put(username, codes);
            sendMsg = true;
        } else {
            if (!codes.contains(code)) {
                codes.add(code);
                sendMsg = true;
            }
        }
        if (sendMsg) {
            clientManger.sendToUser(SocketPackage.build(PackageType.GAME_JOIN, code), username.equals(room.info.white) ? room.info.black : room.info.white);
        }
    }

    /**
     * 退出房间
     */
    public void exitRoom(String username, String code) {
        GameRoomDTO room = rooms.get(code);
        if (null == room || (!room.info.white.equals(username) && !room.info.black.equals(username))) {
            return;
        }
        Set<String> codes = userRooms.get(username);
        if (null == codes) {
            return;
        }
        boolean remove = codes.remove(code);
        if (remove) {
            clientManger.sendToUser(SocketPackage.build(PackageType.GAME_EXIT, code), username.equals(room.info.white) ? room.info.black : room.info.white);
        }
        if (codes.isEmpty()) {
            userRooms.remove(username);
        }
    }

    public void exitRoom(String username) {
        Set<String> codes = userRooms.remove(username);
        if (null == codes) {
            return;
        }
        for (String code : codes) {
            GameRoomDTO room = rooms.get(code);
            if (null == room) {
                continue;
            }
            clientManger.sendToUser(SocketPackage.build(PackageType.GAME_EXIT, code), username.equals(room.info.white) ? room.info.black : room.info.white);
        }

        if (codes.isEmpty()) {
            userRooms.remove(username);
        }
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
