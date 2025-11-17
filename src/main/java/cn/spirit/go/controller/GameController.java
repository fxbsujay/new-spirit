package cn.spirit.go.controller;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.enums.RestStatus;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.dao.GameDao;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.model.dto.GameWaitDTO;
import cn.spirit.go.model.entity.GameEntity;
import cn.spirit.go.service.GameRoomService;
import cn.spirit.go.service.GameWaitService;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.UserSession;
import cn.spirit.go.web.config.AppContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class GameController {

    private final Logger log = LoggerFactory.getLogger(GameController.class);

    private final UserDao userDao = AppContext.getBean(UserDao.class);

    private final GameDao gameDao = AppContext.getBean(GameDao.class);

    private final GameWaitService gameWaitService = AppContext.getBean(GameWaitService.class);

    private final GameRoomService gameRoomService = AppContext.getBean(GameRoomService.class);

    public GameController(Router router, SessionStore sessionHandle) {
        router.get("/api/game/search").handler(ctx -> sessionHandle.handle(ctx, false)).handler(this::searchGame);
        router.post("/api/game/create").handler(sessionHandle::handle).handler(this::createGame);
        router.post("/api/game/join/:code").handler(sessionHandle::handle).handler(this::joinGame);
        router.post("/api/game/cancel").handler(sessionHandle::handle).handler(this::cancelGame);
    }

    /**
     * 搜索对局
     */
    public void searchGame(RoutingContext ctx) {
        String like = ctx.request().getParam("like");
        GameType type = GameType.convert(ctx.request().getParam("type"));
        UserSession session = SessionStore.sessionUser(ctx);
        List<GameWaitDTO> games = gameWaitService.searchGames(session.isGuest ? null : session.username, like, type, 10);
        RestContext.success(ctx, games);
    }

    /**
     * 查询对局
     */
    public void info(RoutingContext ctx) {
        // 查询对局

    }

    /**
     * 创建对局 休闲或好友
     */
    public void createGame(RoutingContext ctx) {
        GameWaitDTO dto = ctx.body().asPojo(GameWaitDTO.class);
        if (null == dto.type || null == dto.mode || null == dto.boardSize || (!GameMode.CASUAL.equals(dto.mode) && !GameMode.FRIEND.equals(dto.mode))) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (null == dto.duration || dto.duration <= 0 || null == dto.stepDuration || dto.stepDuration < 0) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (GameType.SHORT.equals(dto.type) && (dto.duration > 180 || dto.stepDuration > 180)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        } else if (GameType.LONG.equals(dto.type) ) {
            if (dto.duration > 14 || dto.stepDuration > 0) {
                RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            dto.stepDuration = 0;
        } else {
            dto.duration = 0;
            dto.stepDuration = 0;
        }

        UserSession session = SessionStore.sessionUser(ctx);
        userDao.selectByUsername(session.username).onSuccess(user -> {
            dto.score = 800;
            dto.nickname = user.nickname;
            gameWaitService.addGame(session, dto).onSuccess(flag -> {
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
     * 创建自定义对局,
     */
    public void joinGame(RoutingContext ctx) {
        String code = ctx.pathParam("code");
        if (!RegexUtils.matches(code, "[A-Z0-9]{5,}")) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        UserSession session = SessionStore.sessionUser(ctx);
        GameWaitDTO g = gameWaitService.get(code);
        if (null == g || g.username.equals(session.username)) {
            RestContext.fail(ctx, RestStatus.GAME_NOT_EXIST);
            return;
        }

        gameWaitService.removeGame(g.username).onSuccess(game -> {
            if (null == game || !game.code.equals(code)) {
                RestContext.fail(ctx, RestStatus.GAME_NOT_EXIST);
            } else {
                // 对局的基本信息存在数据库中
                GameEntity entity = new GameEntity();
                entity.code = code;
                entity.boardSize = game.boardSize;
                entity.mode = game.mode;
                entity.type = game.type;
                entity.duration = game.duration;
                entity.stepDuration = game.stepDuration;
                if (System.currentTimeMillis() % 2 == 0) {
                    entity.white = session.username;
                    entity.black = game.username;
                } else {
                    entity.white = game.username;
                    entity.black = session.username;
                }
                RestContext.success(ctx, gameRoomService.add(entity));
            }
        }).onFailure(e -> {
            log.error("{}: {}", e.getMessage(), code);
            RestContext.fail(ctx, HttpResponseStatus.LOCKED);
        });
    }

    /**
     * 取消游戏
     */
    public void cancelGame(RoutingContext ctx) {
        UserSession session = SessionStore.sessionUser(ctx);
        gameWaitService.removeGame(session.username).onSuccess(game -> {
            RestContext.success(ctx, game != null);
        }).onFailure(__ -> {
            RestContext.fail(ctx, HttpResponseStatus.LOCKED);
        });
    }
}
