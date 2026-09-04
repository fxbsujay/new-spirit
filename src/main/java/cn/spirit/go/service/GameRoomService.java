package cn.spirit.go.service;

import cn.spirit.go.common.LockConstant;
import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameReason;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.enums.GameWinner;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.dao.GameDao;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.model.*;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.socket.PackageType;
import cn.spirit.go.web.socket.SocketPackage;
import io.vertx.core.Future;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.function.Supplier;

public class GameRoomService {

    private final Logger log = LoggerFactory.getLogger(GameRoomService.class);

    private final GameDao gameDao = AppContext.getBean(GameDao.class);

    private final UserDao userDao = AppContext.getBean(UserDao.class);

    /**
     * 房间 信息
     */
    private final Map<String, Room> rooms = new HashMap<>();

    /**
     * 用户房间
     * uid -> code[]
     */
    private final Map<String, Set<String>> userRooms = new HashMap<>();

    public GameRoomService(Router router) {
        router.route("/api/ws/:code").handler(this::handle);
    }

    /**
     * 创建房间
     *
     * @param info     基本信息
     * @param whiteUid 白棋玩家用户ID
     * @param blackUid 黑棋玩家用户ID
     * @return 房间 号
     */
    public String createRoom(RoomInfo info, String whiteUid, String blackUid) {
        Room room = new Room(info.boardSize);
        room.info = info;
        room.whiteRemainder = info.duration.longValue();
        room.blackRemainder = info.duration.longValue();
        room.whiteUid = whiteUid;
        room.blackUid = blackUid;
        rooms.put(info.code, room);
        addUserRoom(info.code, whiteUid);
        addUserRoom(info.code, blackUid);
        return info.code;
    }

    /**
     * 玩家落子
     * 0412-1760178234 横坐标纵坐标(x,y)-落子时间戳
     *
     * @param code 房间编号
     * @param uid  玩家ID
     * @param x    纵坐标
     * @param y    纵坐标
     */
    private Future<Room> move(String uid, String code, Integer x, Integer y) {
        GameStep step = new GameStep(x, y);
        return lock(code, () -> {
            Room room = get(code);
            if (null == room) {
                return Future.failedFuture("Room not found");
            }
            // 判断参数合法性
            if ((!room.blackUid.equals(uid) && !room.whiteUid.equals(uid)) || x < 0 || y < 0 || x >= room.info.boardSize || y >= room.info.boardSize) {
                return Future.failedFuture("Illegal argument");
            }
            if (room.steps.isEmpty()) {
                // 黑棋先手，是否是黑方
                if (!room.blackUid.equals(uid)) {
                    return Future.failedFuture("Illegal argument");
                }
                room.board[x][y] = Room.BLACK;
            } else {
                int size = room.steps.size();
                GameWinner winner;
                // 判断当前应该是是哪一方落子
                if (room.isWhiteNow()) {
                    if (!room.whiteUid.equals(uid)) {
                        return Future.failedFuture("Illegal argument");
                    }
                    winner = GameWinner.BLACK;
                } else {
                    if (!room.blackUid.equals(uid)) {
                        return Future.failedFuture("Illegal argument");
                    }
                    winner = GameWinner.WHITE;
                }

                // 提子判气
                if (!room.place(x, y, winner == GameWinner.WHITE ? Room.BLACK : Room.WHITE)) {
                    return Future.failedFuture("No moves allowed");
                }

                // 时间有限制并且前两手已经下完
                if (room.info.type != GameType.NONE && size > 1) {
                    // 落子是否超时或违规
                    long time = room.remainingTime(step.timestamp);
                    log.info("time={}", time);
                    if (winner == GameWinner.BLACK) {
                        time += room.whiteRemainder;
                        room.whiteRemainder = time;
                        log.info("W time={}", room.whiteRemainder);
                    } else {
                        time += room.blackRemainder;
                        room.blackRemainder = time;
                        log.info("B time={}", room.blackRemainder);
                    }

                    if (time <= 0) {
                        return Future.failedFuture("Movement timeout, game over");
                    } else {
                        if (null != room.timerId) {
                            AppContext.vertx.cancelTimer(room.timerId);
                        }
                        AppContext.vertx.setTimer(time, id -> {
                            room.timerId = id;
                            // 超时结束
                            end(code, winner, GameReason.TIMEOUT);
                        });
                    }
                }
            }
            room.steps.add(step);
            return Future.succeededFuture(room);
        });
    }

    /**
     * 游戏结束
     *
     * @param code   编号
     * @param winner 胜利方
     * @param reason 胜利原因
     */
    private void end(String code, GameWinner winner, GameReason reason) {
        lock(code, () -> {
            Room room = rooms.remove(code);
            if (null == room) {
                return Future.failedFuture("Room not found");
            }

            if (reason == GameReason.SURRENDER) {
                if (room.steps.size() <= 1) {
                    // 棋局未开始不允许投降，可取消
                    return Future.failedFuture("The game has not started");
                }
            } else if (reason == GameReason.CANCEL) {
                if (room.info.mode == GameMode.RANK) {
                    // 排位赛不允许取消
                    return Future.failedFuture("The RANK mode cannot cancel the match");
                }
                if (room.steps.size() > 1) {
                    // 棋局已经开始不允许取消
                    return Future.failedFuture("The game has begun");
                }
            }

            Set<String> wCodes = userRooms.get(room.whiteUid);
            wCodes.remove(code);
            if (wCodes.isEmpty()) {
                userRooms.remove(room.whiteUid);
            }
            Set<String> bCodes = userRooms.get(room.blackUid);
            bCodes.remove(code);
            if (bCodes.isEmpty()) {
                userRooms.remove(room.blackUid);
            }
            return Future.succeededFuture(room);
        }).onSuccess(room -> {
            log.info("Game over, code = {}, winner = {}, reason = {}", code, winner, reason);
            send(code, SocketPackage.build(PackageType.GAME_END, JsonObject.of("winner", winner, "reason", reason)));

            if (reason == GameReason.CANCEL) {
                // 取消游戏不保存
                return;
            }
            JsonObject game = new JsonObject();
            game.put("code", code);
            game.put("boardSize", room.info.boardSize);
            game.put("type", room.info.type);
            game.put("mode", room.info.mode);
            game.put("duration", room.info.duration);
            game.put("stepDuration", room.info.stepDuration);
            game.put("startTime", room.info.startTime / 1000);
            game.put("endTime", System.currentTimeMillis() / 1000);
            game.put("whiteUid", room.whiteUid);
            game.put("blackUid", room.blackUid);
            game.put("winner", winner);
            game.put("reason", reason);
            JsonArray board = new JsonArray();
            for (int i = 0; i < room.board.length; i++) {
                for (int j = 0; j < room.board.length; j++) {
                    if (room.board[i][j] != Room.EMPTY) {
                        board.add(String.valueOf(Room.LOCATION[i]) + j + room.board[i][j]);
                    }
                }
            }
            game.put("board", board);
            JsonArray steps = new JsonArray();
            for (GameStep step : room.steps) {
                steps.add(String.valueOf(Room.LOCATION[step.x]) + step.y + "-" + step.timestamp);
            }
            game.put("steps", steps);


            int whiteAddRating = winner == GameWinner.WHITE ? 20 : -20;
            gameDao.insert(game).compose(res -> userDao.updateRating(room.whiteUid, whiteAddRating, room.blackUid, -whiteAddRating))
                    .onSuccess(count -> log.info("Save game success, code = {}", code))
                    .onFailure(e -> log.error("Save game failed, code = {}", code));
        }).onFailure(e -> log.error("Game ended in failure, code = {}, reason = {}. failure message = {}", code, reason, e.getMessage()));
    }

    /**
     * 获取房间
     */
    public Room get(String code) {
        return rooms.get(code);
    }

    private void addUserRoom(String code, String uid) {
        Set<String> codes = userRooms.get(uid);
        if (null != codes) {
            codes.add(code);
        } else {
            codes = new HashSet<>();
            codes.add(code);
            userRooms.put(uid, codes);
        }
    }

    public Set<String> getUserRoomCodes(String uid) {
        return userRooms.get(uid);
    }

    private void handle(RoutingContext ctx) {
        ctx.request().toWebSocket().onSuccess(ws -> SessionStore.validate(ctx).onSuccess(session -> {
            String code = ctx.pathParam("code");
            if (RegexUtils.mismatchGameCode(code)) {
                ws.close();
                return;
            }
            RoomSocket socket = new RoomSocket(session, ws);
            boolean flag = connection(code, socket);
            if (!flag) {
                // 一个用户只能有一个会话在此房间内
                socket.send(Json.encode(SocketPackage.build(PackageType.ROOM_CONNECTION_EXISTS, code)));
                ws.close();
                return;
            }
            log.info("game socket join success, code: {}, uid: {}", code, session.uid);
            ws.textMessageHandler(text -> {
                SocketPackage pck;
                try {
                    pck = Json.decodeValue(text, SocketPackage.class);
                } catch (DecodeException e) {
                    log.error("Failed to parse websocket message packet, from: {}, sessionId: {}", session.uid, session.sId);
                    ws.close();
                    return;
                }
                switch (pck.type) {
                    case ROOM_STEP -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> obj = (Map<String, Object>) pck.data;
                        Integer x = (Integer) obj.get("x");
                        Integer y = (Integer) obj.get("y");
                        if (x == null || y == null) {
                            ws.close();
                            return;
                        }
                        move(session.uid, code, x, y)
                                .onSuccess(room -> {
                                    JsonObject data = JsonObject.of(
                                            "whiteRemainder", room.whiteRemainder,
                                            "blackRemainder", room.blackRemainder,
                                            "step", room.steps.get(room.steps.size() - 1)
                                    );
                                    send(code, SocketPackage.build(PackageType.ROOM_STEP, data));
                                    log.info("[{}] - Add a step to the game {}, uid={}, x = {}, y = {}", room.whiteUid.equals(session.uid) ? 'W' : 'B', code, session.uid, x, y);
                                    room.outPrintBoard();
                                }).onFailure(e -> log.error("Adding step failed, code = {}, x = {}, y = {}, failure message = {},", code, x, y, e.getMessage()));
                    }
                    case GAME_SURRENDER -> {
                        // 投降，棋局未开始则取消
                        Room room = get(code);
                        if (room != null) {
                            GameReason reason = GameReason.valueOf((String) pck.data);
                            if (reason == GameReason.SURRENDER) {
                                if (room.steps.size() <= 1) {
                                    // 棋局未开始不允许投降，可取消
                                    return;
                                }
                            } else if (reason == GameReason.CANCEL) {
                                if (room.info.mode == GameMode.RANK) {
                                    // 排位赛不允许取消
                                    return;
                                }
                                if (room.steps.size() > 1) {
                                    // 棋局已经开始不允许取消
                                    return;
                                }
                            }
                            GameWinner winner = room.whiteUid.equals(session.uid) ? GameWinner.BLACK : GameWinner.WHITE;
                            end(code, winner, reason);
                        }
                    }
                    case GAME_PEACE -> {
                        // 求和
                        Room room = get(code);
                        if (room != null) {
                            if (room.steps.size() <= 1) {
                                // 棋局未开始不允许求和
                                return;
                            }
                            send(code, SocketPackage.build(PackageType.GAME_PEACE, ""));
                        }
                    }
                    case ROOM_CHAT -> send(code, pck);
                    default -> {
                        log.error("Illegal websocket message packet type, from: {}, sessionId: {}", session.uid, session.sId);
                        ws.close();
                    }
                }
            });
            ws.closeHandler(e -> disconnection(code, socket));
        }).onFailure(e -> ws.close()));
    }

    /**
     * 游戏网络连接 必须是玩家
     *
     * @param code   房间 号
     * @param socket Socket
     * @return 是否连接成功
     */
    private boolean connection(String code, RoomSocket socket) {
        Room room = get(code);
        if (null == room || (!room.whiteUid.equals(socket.uid) && !room.blackUid.equals(socket.uid))) {
            return false;
        }
        boolean flag = room.sockets.add(socket);
        if (flag) {
            send(code, SocketPackage.build(PackageType.ROOM_CONNECTION, code));
        }
        return flag;
    }

    /**
     * 玩家断开网络连接
     */
    private void disconnection(String code, RoomSocket socket) {
        Room room = get(code);
        if (null == room || (!room.whiteUid.equals(socket.uid) && !room.blackUid.equals(socket.uid))) {
            return;
        }
        boolean flag = room.sockets.remove(socket);
        if (flag) {
            send(code, SocketPackage.build(PackageType.ROOM_DISCONNECTION, code));
        }
    }

    /**
     * 发送消息
     *
     * @param pack 消息包
     * @param code 对局编号
     */
    private void send(String code, SocketPackage pack) {
        Room room = rooms.get(code);
        if (null == room) {
            log.error("send code {}, but room is null", code);
            return;
        }
        String msg = Json.encode(pack);
        for (RoomSocket socket : room.sockets) {
            socket.send(msg);
        }
    }

    /**
     * 房间锁
     *
     * @param code 房间 号
     */
    private <T> Future<T> lock(String code, Supplier<Future<T>> block) {
        return AppContext.withLock(LockConstant.ROOM_LOCK + code, block);
    }
}
