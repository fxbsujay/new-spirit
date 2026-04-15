package cn.spirit.go.service;

import cn.spirit.go.common.enums.GameReason;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.enums.GameWinner;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.model.*;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.socket.ClientManger;
import cn.spirit.go.web.socket.PackageType;
import cn.spirit.go.web.socket.SocketPackage;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class GameRoomService implements Handler<RoutingContext> {

    private final Logger log = LoggerFactory.getLogger(GameRoomService.class);

    private final ClientManger clientManger;

    /**
     * 房间信息
     */
    private final Map<String, Room> rooms = new HashMap<>();

    /**
     * 用户房间
     * username -> code[]
     */
    private final Map<String, Set<String>> userRooms = new HashMap<>();


    public GameRoomService(Router router, ClientManger clientManger) {
        this.clientManger = clientManger;
        router.route("/api/ws/:code").handler(this);
    }

    /**
     * 创建房间
     * @param info  基本信息
     * @param white 白棋玩家用户名
     * @param black 黑棋玩家用户名
     * @return 房间号
     */
    public String createRoom(RoomInfo info, String white, String black) {
        Room room = new Room();
        room.whiteRemainder = info.duration.longValue();
        room.blackRemainder = info.duration.longValue();
        room.white = white;
        room.black = black;
        rooms.put(info.code, room);
        clientManger.sendToUser(SocketPackage.build(PackageType.GAME_START, info.code), white, black);

        addUserRoom(info.code, white);
        addUserRoom(info.code, black);
        return info.code;
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
    private void move(String username, String code, Integer x, Integer y) {
        Room room = get(code);
        if (null == room) {
            return;
        }
        // 判断参数合法性
        if ((!room.black.equals(username) && !room.white.equals(username)) || x < 0 || y < 0 || x >= room.info.boardSize || y >= room.info.boardSize) {
            return;
        }

        GameStep step = new GameStep(x, y);
        if (room.steps.isEmpty()) {
            // 黑棋先手，是否是黑方
            if (!room.black.equals(username)) {
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
            if (room.isWhiteNow()) {
                if (!room.white.equals(username)) {
                    return;
                } else {
                    winner = GameWinner.BLACK;
                }
            } else {
                if (!room.black.equals(username)) {
                    return;
                } else {
                    winner = GameWinner.WHITE;
                }
            }
            // TODO 判断是否为禁入点

            // 时间有限制并且前两手已经下完
            if (room.info.type != GameType.NONE && size > 1) {
                // 落子是否超时或违规
                long time = room.remainingTime(step.timestamp);
                log.info("time={}",time);
                if (winner == GameWinner.BLACK) {
                    time += room.whiteRemainder;
                    room.whiteRemainder = time;
                    log.info("W time={}", room.whiteRemainder);

                } else {
                    time += room.blackRemainder;
                    room.blackRemainder = time;
                    log.info("B time={}",room.blackRemainder);
                }

                if (time <= 0) {
                    // TODO 超时结算
                    end(code, winner, GameReason.TIMEOUT);
                    return;
                } else {
                    // TODO 定时任务 {time} 毫秒后未走，游戏结束
//                    long timer = AppContext.vertx.setTimer(time, id -> {
//
//                    });
                }
            }
        }

        room.steps.add(step);
        // TODO 提子
        log.info("[{}] - add a step to the game {}, username={}, x={}, y={}, ", room.white.equals(username) ? 'W' : 'B', code, username, x, y);
        send(code, SocketPackage.build(PackageType.GAME_STEP, username,  JsonObject.of("whiteRemainder", room.whiteRemainder, "blackRemainder", room.blackRemainder, "step", step)));
    }

    /**
     * 游戏结束
     *
     * @param code 编号
     * @param winner 胜利方
     * @param reason 胜利原因
     */
    public Future<Void> end(String code, GameWinner winner, GameReason reason) {
        return null;
    }

    /**
     * 获取房间
     */
    public Room get(String code) {
        return rooms.get(code);
    }


    private void addUserRoom(String code, String usernames) {
        Set<String> codes = userRooms.get(usernames);
        if (null != codes) {
            codes.add(code);
        } else {
            codes = new HashSet<>();
            codes.add(code);
            userRooms.put(usernames, codes);
        }
    }

    public Set<String> getUserRoomCodes(String username) {
        return userRooms.get(username);
    }

    @Override
    public void handle(RoutingContext ctx) {
        ctx.request().toWebSocket().onSuccess(ws -> SessionStore.validate(ctx).onSuccess(session -> {
            String code = ctx.pathParam("code");
            if (RegexUtils.mismatchGameCode(code)) {
                ws.close();
                return;
            }
            RoomSocket socket = new RoomSocket(session, ws);
            boolean flag = connection(code, socket);
            if (!flag) {
                ws.close();
                return;
            }
            log.info("game socket join success, code: {}, username: {}", code, session.username);
            ws.textMessageHandler(text -> {
                SocketPackage pck;
                try {
                    pck = Json.decodeValue(text, SocketPackage.class);
                } catch (DecodeException e) {
                    log.error("Failed to parse websocket message packet, from: {}, sessionId: {}", session.username, session.sessionId);
                    ws.close();
                    return;
                }
                pck.sender = session.username;
                switch (pck.type) {
                    case GAME_STEP:
                        Map<String, Object> obj = (Map) pck.data;
                        Integer x = (Integer) obj.get("x");
                        Integer y = (Integer) obj.get("y");
                        if (RegexUtils.mismatchGameCode(code) || x == null || y == null) {
                            ws.close();
                            return;
                        }
                        move(pck.sender, code, x, y);
                        break;
                    case GAME_CHAT:
                        send(code, pck);
                        break;
                    default:
                        log.error("Illegal websocket message packet type, from: {}, sessionId: {}", session.username, session.sessionId);
                        ws.close();
                        return;
                }
            });
            ws.closeHandler(e -> {
                disconnection(code, socket);
            });
        }).onFailure(e -> {
            ws.close();
        }));
    }


    /**
     * 游戏网络连接
     * @param code      房间号
     * @param socket    Socket
     * @return          是否连接成功
     */
    private boolean connection(String code, RoomSocket socket) {
        Room room = get(code);
        if (null == room || (!room.white.equals(socket.username) && !room.black.equals(socket.username))) {
            return false;
        }
        boolean flag = room.sockets.add(socket);
        if (flag) {
            send(code, SocketPackage.build(PackageType.GAME_JOIN, socket.username, code));
        }
        return flag;
    }

    /**
     * 玩家断开网络连接
     */
    private void disconnection(String code, RoomSocket socket) {
        Room room = get(code);
        if (null == room || (!room.white.equals(socket.username) && !room.black.equals(socket.username))) {
            return;
        }
        boolean flag = room.sockets.remove(socket);
        if (flag) {
            send(code, SocketPackage.build(PackageType.GAME_EXIT, socket.username, code));
        }
    }

    /**
     * 发送消息
     *
     * @param pack          消息包
     * @param code          对局编号
     */
    private void send(String code, SocketPackage pack) {
        Room room = rooms.get(code);
        if (room == null || (!room.white.equals(pack.sender) && !room.black.equals(pack.sender))) {
            return;
        }
        String msg = Json.encode(pack);
        for (RoomSocket socket : room.sockets) {
            socket.send(msg);
        }
    }

}
