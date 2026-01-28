package cn.spirit.go.service;

import cn.spirit.go.common.LockConstant;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.enums.GameWinner;
import cn.spirit.go.dao.GameDao;
import cn.spirit.go.model.GamePlay;
import cn.spirit.go.model.GameRoom;
import cn.spirit.go.model.GameSocket;
import cn.spirit.go.model.GameStep;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.socket.ClientManger;
import cn.spirit.go.web.socket.PackageType;
import cn.spirit.go.web.socket.SocketPackage;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.function.Supplier;

public class GameRoomService {

    private final Logger log = LoggerFactory.getLogger(GameRoomService.class);


    private final ClientManger clientManger = AppContext.getBean(ClientManger.class);

    private final GameDao gameDao = AppContext.getBean(GameDao.class);

    /**
     * 房间信息
     */
    private final Map<String, GameRoom> rooms = new HashMap<>();

    /**
     * 用户房间
     * username -> code[]
     */
    private final Map<String, Set<String>> userRooms = new HashMap<>();

    /**
     * 订阅信息
     * sessionId -> code[]
     */
    private final Map<String, Set<String>> subscribers = new HashMap<>();

    /**
     * 添加房间，创建房间后通知玩家游戏开始，随后进入游戏界面，通知服务器玩家已进入房间，黑旗先行，双方都落子后游戏正式开始，不可取消
     * 进入游戏界面客户端发送加入房间通知，关闭游戏界面发生退出房间通知
     */
    public String add(GamePlay info) {
        GameRoom dto = new GameRoom();
        dto.info = info;
        dto.whiteRemainder = info.duration.longValue();
        dto.blackRemainder = info.duration.longValue();
        rooms.put(info.code, dto);
        clientManger.sendToUser(SocketPackage.build(PackageType.GAME_START, info.code), info.white, info.black);
        addUserRoom(info.code, info.white, info.black);
        return info.code;
    }

    private void addUserRoom(String code, String ...usernames) {
        for (String username : usernames) {
            Set<String> codes = userRooms.get(username);
            if (null != codes) {
                codes.add(code);
            } else {
                codes = new HashSet<>();
                codes.add(code);
                userRooms.put(username, codes);
            }
        }
    }

    /**
     * 加入房间 只允许对局双方玩家进入
     */
    public boolean joinRoom(String code, GameSocket socket) {
        GameRoom room = rooms.get(code);
        if (null == room || (!room.info.white.equals(socket.username) && !room.info.black.equals(socket.username))) {
            return false;
        }
        boolean flag = room.sockets.add(socket);
        if (flag) {
            send(code, SocketPackage.build(PackageType.GAME_JOIN, socket.username, code));
        }
        return flag;
    }

    /**
     * 退出房间
     */
    public void exitRoom(String code, GameSocket socket) {
        GameRoom room = rooms.get(code);
        if (null == room || (!room.info.white.equals(socket.username) && !room.info.black.equals(socket.username))) {
            return;
        }
        boolean flag = room.sockets.remove(socket);
        if (flag) {
            send(code, SocketPackage.build(PackageType.GAME_EXIT, socket.username, code));
        }
    }

    /**
     * 玩家落子
     * 0412-1760178234 横坐标纵坐标(x,y)-落子时间戳
     *
     * @param code      房间编号
     * @param username  玩家用户名
     * @param x         纵坐标
     * @param y         纵坐标
     */
    public void addStep(String username, String code, Integer x, Integer y) {
        GameRoom room = rooms.get(code);
        if (null == room) {
            return;
        }
        // 判断参数合法性
        if ((!room.info.black.equals(username) && !room.info.white.equals(username)) || x < 0 || y < 0 || x >= room.info.boardSize || y >= room.info.boardSize) {
            return;
        }

        GameStep step = new GameStep(x, y);

        if (room.steps.isEmpty()) {
            // 黑棋先手，是否是黑方
            if (!room.info.black.equals(username)) {
                return;
            }
        } else {
            // 判断棋子是否重叠
            if (room.steps.contains(step)) {
                return;
            }

            int size = room.steps.size();
            GameWinner winner;
            // 判断当前应该是是哪一方落子
            if (size % 2 == 1) {
                if (!room.info.white.equals(username)) {
                    return;
                } else {
                    winner = GameWinner.BLACK;
                }
            } else {
                if (!room.info.black.equals(username)) {
                    return;
                } else {
                    winner = GameWinner.WHITE;
                }
            }

            // 时间有限制并且前两手已经下完
            if (room.info.type != GameType.NONE && size > 1) {
                // 落子是否超时或违规
                long time = room.remainingTime(step.timestamp);
                if (winner == GameWinner.BLACK) {
                    time += room.whiteRemainder;
                    room.whiteRemainder = time;
                } else {
                    time += room.blackRemainder;
                }
                log.info("W time={}, B time={}", room.whiteRemainder,room.blackRemainder);
                if (time <= 0) {
                    // TODO 超时结算
                    end(code, winner);
                    return;
                } else {
                    // TODO 定时任务 {time} 毫秒后未走，游戏结束
                }
            }
        }

        room.steps.add(step);
        log.info("[{}] - add a step to the game {}, username={}, x={}, y={}, ", room.info.white.equals(username) ? 'W' : 'B', code, username, x, y);
    }

    /**
     * 获取房间
     */
    public GameRoom get(String code) {
        return rooms.get(code);
    }

    /**
     * 玩家是否在对局中
     * @param code      对局编号
     * @param username  用户名
     */
    public boolean isOnline(String code, String username) {
        GameRoom room = rooms.get(code);
        if (null == room || (!room.info.white.equals(username) && !room.info.black.equals(username))) {
            return false;
        }
        for (GameSocket socket : room.sockets) {
            if (!socket.getConnection().isClosed() && socket.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 发送消息
     *
     * @param pack          消息包
     * @param code          对局编号
     */
    public void send(String code, SocketPackage pack) {
        GameRoom room = rooms.get(code);
        if (room == null || (!room.info.white.equals(pack.sender) && !room.info.black.equals(pack.sender))) {
            return;
        }
        String msg = Json.encode(pack);
        for (GameSocket socket : room.sockets) {
            socket.send(msg);
        }
    }

    /**
     * 游戏结束
     *
     * @param code 编号
     */
    public void end(String code, GameWinner winner) {
        GameRoom room = rooms.remove(code);
        if (null == room) {
            return;
        }
        Set<String> whiteCodes = userRooms.get(room.info.white);
        if (null != whiteCodes) {
            whiteCodes.remove(code);
            if (whiteCodes.isEmpty()) {
                userRooms.remove(room.info.white);
            }
        }
        Set<String> blackCodes = userRooms.get(room.info.black);
        if (null != blackCodes) {
            blackCodes.remove(code);
            if (blackCodes.isEmpty()) {
                userRooms.remove(room.info.black);
            }
        }
        String msg = Json.encode(SocketPackage.build(PackageType.GAME_END, code));
        for (GameSocket socket : room.sockets) {
            socket.send(msg);
            socket.getConnection().close();
        }

        room.info.winner = winner;
        JsonObject obj = JsonObject.mapFrom(room.info);
        obj.put("steps", room.steps);
        gameDao.insert(obj).onSuccess(id -> {
            log.info("Game End, winner={}, id={}", winner, id);
        });

    }

    /**
     * 订阅这场比赛
     *
     * @param code        对局编号
     * @param sessionId   加入房间的客户端
     */
    public boolean subscribe(String sessionId, String code) {
        GameRoom room = rooms.get(code);
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

    private <T> Future<T> lock(String code, Supplier<Future<T>> block) {
       return AppContext.vertx.sharedData().withLock(LockConstant.ROOM_LOCK + code, 1000, block);
    }

}
