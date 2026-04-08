package cn.spirit.go.controller;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.*;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.dao.GameDao;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.model.GamePlay;
import cn.spirit.go.model.GameRoom;
import cn.spirit.go.model.CasualGameInfo;
import cn.spirit.go.service.GameManager;
import cn.spirit.go.service.GoMatchService;
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
import java.util.Set;

public class GameController {

    private final Logger log = LoggerFactory.getLogger(GameController.class);

    private final UserDao userDao = AppContext.getBean(UserDao.class);

    private final GameDao gameDao = AppContext.getBean(GameDao.class);

    private final GameManager gameManager = AppContext.getBean(GameManager.class);

    private final GoMatchService matchService = AppContext.getBean(GoMatchService.class);

    public GameController(Router router, SessionStore sessionHandle) {
        router.get("/api/game/search").handler(ctx -> sessionHandle.handle(ctx, false)).handler(this::searchGame);
        router.get("/api/game/playing").handler(sessionHandle::handle).handler(this::playing);
        router.post("/api/game/create").handler(sessionHandle::handle).handler(this::createGame);
        router.post("/api/game/cancel").handler(sessionHandle::handle).handler(this::cancelGame);
        router.post("/api/game/join/:code").handler(sessionHandle::handle).handler(this::joinGame);
        router.get("/api/game/info/:code").handler(sessionHandle::handle).handler(this::info);
        router.post("/api/game/end/:code").handler(sessionHandle::handle).handler(this::endGame);
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
     * 查询自己的对局
     */
    public void playing(RoutingContext ctx) {
        UserSession session = SessionStore.sessionUser(ctx);
        Set<String> codes = matchService.userRoomCodes(session.username);

        if (null == codes || codes.isEmpty()) {
            RestContext.success(ctx, new JsonArray());
            return;
        }
        JsonArray list = new JsonArray();

        for (String code : codes) {
            GameRoom room = matchService.get(code);
            if (null == room) {
                continue;
            }
            list.add(JsonObject.of(
                    "info", room.info,
                    "steps", room.steps,
                    "white", room.white.toJson().put("remainder", room.whiteRemainder),
                    "black", room.black.toJson().put("remainder", room.blackRemainder)));
        }

        RestContext.success(ctx, list);
    }

    /**
     * 查询对局
     */
    public void info(RoutingContext ctx) {
        String code = ctx.pathParam("code");
        if (RegexUtils.mismatchGameCode(code)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        GameRoom room = matchService.get(code);
        if (null == room) {
            gameDao.findOne(JsonObject.of("code", code), "code", "white", "black").onSuccess(game -> {
                if (null == game) {
                    RestContext.fail(ctx, RestStatus.GAME_NOT_EXIST);
                } else {
                    String white = game.getString("white");
                    String black = game.getString("black");
                    JsonObject query = JsonObject.of("$in", JsonArray.of(white, black));
                    userDao.findAll(query, "username", "nickname", "avatar", "rating").onSuccess(users -> {
                        JsonObject obj = JsonObject.of("info", game);
                        for (JsonObject user : users) {
                            if (user.getString("username").equals(white)) {
                                obj.put("white", user);
                            } else {
                                obj.put("black", user);
                            }
                        }
                        RestContext.success(ctx, obj);
                    }).onFailure(e -> {
                        log.error(e.getMessage(), e);
                        RestContext.fail(ctx);
                    });
                }
            });
        } else {
            RestContext.success(ctx, JsonObject.of(
                    "info", room.info,
                    "steps", room.steps,
                    "white", room.white.toJson().put("remainder", room.whiteRemainder),
                    "black", room.black.toJson().put("remainder", room.blackRemainder)));
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

        if (null == dto.duration || dto.duration <= 0 || null == dto.stepDuration || dto.stepDuration < 0) {
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
                    RestContext.success(ctx);
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
            GamePlay entity = new GamePlay();
            entity.code = code;
            entity.boardSize = game.boardSize;
            entity.mode = GameMode.CASUAL;
            entity.type = game.type;
            entity.duration = game.duration;
            entity.stepDuration = game.stepDuration;
            entity.timestamp = game.timestamp;
            entity.startTime = System.currentTimeMillis();

            // 查询用户信息
            JsonObject query = JsonObject.of("username", JsonObject.of("$in", JsonArray.of(game.username, username)));
            userDao.findAll(query, "username", "nickname", "avatar", "rating").onSuccess(users -> {
                GameRoom.Player[] players = new GameRoom.Player[users.size()];
                for (int i = 0; i < users.size(); i++) {
                    JsonObject user = users.get(i);
                    GameRoom.Player p = new GameRoom.Player();
                    p.username = user.getString("username");
                    p.avatar = user.getString("avatar");
                    p.nickname = user.getString("nickname");
                    p.rating = user.getInteger("rating");
                    players[i] = p;
                }
                boolean flag = System.currentTimeMillis() % 2 == 0;
                if (flag) {
                    matchService.add(entity, players[0], players[1]);
                } else {
                    matchService.add(entity, players[1], players[0]);
                }
                RestContext.success(ctx, code);
            }).onFailure(e -> {
                log.error("{}: {}", e.getMessage(), code);
                RestContext.fail(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            });
        }).onFailure(e -> {
            log.error("{}: {}", e.getMessage(), code);
            RestContext.fail(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        });
    }

    /**
     * 认输结束游戏,
     */
    public void endGame(RoutingContext ctx) {
        String code = ctx.pathParam("code");
        if (RegexUtils.mismatchGameCode(code)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        UserSession session = SessionStore.sessionUser(ctx);
        GameRoom room = matchService.get(code);
        if (null == room || (!room.white.username.equals(session.username) && !room.black.username.equals(session.username))) {
            RestContext.fail(ctx, RestStatus.GAME_NOT_EXIST);
            return;
        }
        GameWinner winner = room.white.username.equals(session.username) ? GameWinner.WHITE : GameWinner.BLACK;

        matchService.end(code, winner, GameReason.SURRENDER).onSuccess(v -> RestContext.success(ctx))
                .onFailure(e -> {
                    log.error("{}: {}", e.getMessage(), code);
                    RestContext.fail(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                });
    }

}
