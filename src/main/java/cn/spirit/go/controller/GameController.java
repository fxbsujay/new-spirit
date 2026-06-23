package cn.spirit.go.controller;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.*;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.model.*;
import cn.spirit.go.service.GameManager;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.UserSession;
import cn.spirit.go.web.config.AppContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class GameController {

    private final Logger log = LoggerFactory.getLogger(GameController.class);

    private final UserDao userDao = AppContext.getBean(UserDao.class);

    private final GameManager gameManager = AppContext.getBean(GameManager.class);

    public GameController(Router router, SessionStore sessionHandle) {
        router.get("/api/game/search").handler(ctx -> sessionHandle.handle(ctx, false)).handler(this::searchGame);
        router.post("/api/game/create").handler(sessionHandle::handle).handler(this::createGame);
        router.post("/api/game/cancel").handler(sessionHandle::handle).handler(this::cancelGame);
        router.post("/api/game/join/:code").handler(sessionHandle::handle).handler(this::joinGame);

        router.post("/api/game/ranking").handler(sessionHandle::handle).handler(this::ranking);
        router.post("/api/game/ranking/cancel").handler(sessionHandle::handle).handler(this::cancelRanking);

        router.get("/api/room/ongoing").handler(sessionHandle::handle).handler(this::ongoingRooms);
        router.get("/api/room/info/:code").handler(sessionHandle::handle).handler(this::roomInfo);
    }

    /**
     * 搜索对局
     */
    public void searchGame(RoutingContext ctx) {
        String page = ctx.request().getParam("page");
        if (null == page) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        GameType type = GameType.convert(ctx.request().getParam("type"));
        RestContext.success(ctx, gameManager.searchLobbyGames(SessionStore.sessionUser(ctx), type, Integer.parseInt(page)));
    }

    /**
     * 查询自己进行中的对局
     */
    public void ongoingRooms(RoutingContext ctx) {
        UserSession session = SessionStore.sessionUser(ctx);
        List<Room> rooms = gameManager.searchRooms(session.username);

        if (null == rooms || rooms.isEmpty()) {
            RestContext.success(ctx, Collections.emptyList());
            return;
        }

        Set<String> usernames = new HashSet<>();
        for (Room room : rooms) {
            usernames.add(room.white);
            usernames.add(room.black);
        }
        JsonObject query = JsonObject.of("username", JsonObject.of("$in", usernames));
        userDao.findAll(query, "username", "nickname", "avatar", "rating").onSuccess(users -> {
            JsonArray list = new JsonArray();
            Map<String, JsonObject> userMap = new HashMap<>();
            for (JsonObject user : users) {
                userMap.put(user.getString("username"), user);
            }
            for (Room room : rooms) {
                list.add(JsonObject.of(
                        "info", room.info,
                        "steps", room.steps,
                        "white", userMap.get(room.white).put("remainder", room.whiteRemainder),
                        "black", userMap.get(room.black).put("remainder", room.blackRemainder)));
            }
            RestContext.success(ctx, list);
        }).onFailure(cause -> {
            log.error(cause.getMessage(), cause);
            RestContext.fail(ctx);
        });
    }

    /**
     * 查询对局信息
     */
    public void roomInfo(RoutingContext ctx) {
        String code = ctx.pathParam("code");
        if (RegexUtils.mismatchGameCode(code)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        Room room = gameManager.getRoom(code);
        if (null == room) {

        } else {
            JsonObject query = JsonObject.of("username", JsonObject.of("$in", JsonArray.of(room.white, room.black)));
            userDao.findAll(query, "username", "nickname", "avatar", "rating").onSuccess(users -> {
                JsonObject res = JsonObject.of(
                        "info", room.info,
                        "steps", room.steps);
                for (JsonObject user : users) {
                   if (room.white.equals(user.getString("username"))) {
                       user.put("remainder", room.whiteRemainder);
                       user.put("captured", room.whiteCaptured);
                       res.put("white", user);
                   } else {
                       user.put("remainder", room.blackRemainder);
                       user.put("captured", room.blackCaptured);
                       res.put("black", user);
                   }
                }
                RestContext.success(ctx, res);
            }).onFailure(cause -> {
                log.error(cause.getMessage(), cause);
                RestContext.fail(ctx);
            });

        }
    }

    /**
     * 创建休闲对局
     */
    public void createGame(RoutingContext ctx) {
        CasualGameInfo dto = ctx.body().asPojo(CasualGameInfo.class);
        if (null == dto.type || null == dto.boardSize) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (!dto.type.equals(GameType.NONE) && (null == dto.duration || dto.duration <= 0 || null == dto.stepDuration || dto.stepDuration < 0)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (GameType.SHORT.equals(dto.type)) {
            // 基础时长不能大于180分钟，步长不能大于180秒
            if (dto.duration > 180 || dto.stepDuration > 180) {
                RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            dto.duration *= 60 * 1000;
            dto.stepDuration *= 1000;
        } else if (GameType.LONG.equals(dto.type)) {
            // 基础时长不能大于114天，步长为0
            if (dto.duration > 14 || dto.stepDuration > 0) {
                RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            dto.duration *= 60 * 60 * 24 * 1000;
            dto.stepDuration = 0;
        } else {
            dto.duration = 0;
            dto.stepDuration = 0;
        }

        dto.username = SessionStore.username(ctx);
        userDao.findOne(JsonObject.of("username", dto.username), "nickname", "rating").onSuccess(user -> {
            dto.score = user.getInteger("rating");
            dto.nickname = user.getString("nickname");
            gameManager.createCasualGame(dto).onSuccess(flag -> {
                if (flag) {
                    RestContext.success(ctx, dto.code);
                } else {
                    RestContext.fail(ctx, RestStatus.GAME_CREATED);
                }
            }).onFailure(__ -> RestContext.fail(ctx, HttpResponseStatus.LOCKED));
        }).onFailure(e -> {
            log.error(e.getMessage(), e);
            RestContext.fail(ctx);
        });
    }

    /**
     * 取消游戏
     */
    public void cancelGame(RoutingContext ctx) {
        gameManager.cancelCasualGame(SessionStore.username(ctx))
                .onSuccess(game -> RestContext.success(ctx, game != null))
                .onFailure(__ -> RestContext.fail(ctx, HttpResponseStatus.LOCKED));
    }

    /**
     * 加入自定义对局
     */
    public void joinGame(RoutingContext ctx) {
        String code = ctx.pathParam("code");
        if (RegexUtils.mismatchGameCode(code)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        String username = SessionStore.username(ctx);
        if (null != gameManager.getCasualGameByUsername(username)) {
            RestContext.fail(ctx, RestStatus.GAME_CREATED);
            return;
        }

        CasualGameInfo g = gameManager.getCasualGame(code);
        if (null == g || g.username.equals(username)) {
            RestContext.fail(ctx, RestStatus.GAME_NOT_EXIST);
            return;
        }

        gameManager.cancelCasualGame(g.username).onSuccess(game -> {
            if (null == game || !game.code.equals(code)) {
                RestContext.fail(ctx, RestStatus.GAME_NOT_EXIST);
                return;
            }
            // 对局的基本信息存在数据库中
            RoomInfo info = new RoomInfo();
            info.code = code;
            info.boardSize = game.boardSize;
            info.mode = GameMode.CASUAL;
            info.type = game.type;
            info.duration = game.duration;
            info.stepDuration = game.stepDuration;
            info.startTime = System.currentTimeMillis();
            gameManager.createRoom(info, username, g.username);
            RestContext.success(ctx, code);
        }).onFailure(e -> {
            log.error("{}: {}", e.getMessage(), code);
            RestContext.fail(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        });
    }

    /**
     * 排位比赛
     */
    public void ranking(RoutingContext ctx) {
        String username = SessionStore.username(ctx);
        userDao.findOne(JsonObject.of("username", username), "rating")
                .compose(user -> gameManager.startRanking(username, user.getInteger("rating")))
                .onSuccess(isSuccess -> {
                    RestContext.success(ctx, isSuccess);
                }).onFailure(e -> {
                    log.error(e.getMessage(), e);
                    RestContext.fail(ctx);
                });
    }

    /**
     * 取消排位
     */
    public void cancelRanking(RoutingContext ctx) {
        gameManager.cancelRanking(SessionStore.username(ctx)).onSuccess(ranking -> {
            RestContext.success(ctx);
        }).onFailure(e -> {
            log.error(e.getMessage(), e);
            RestContext.fail(ctx);
        });
    }
}
