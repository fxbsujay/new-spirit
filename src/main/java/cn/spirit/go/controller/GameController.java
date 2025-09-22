package cn.spirit.go.controller;

import cn.spirit.go.common.RedisConstant;
import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.ChessPiece;
import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.enums.RestStatus;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.model.dto.GameWaitDTO;
import cn.spirit.go.service.GameWaitService;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.UserSession;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.socket.PackageType;
import cn.spirit.go.web.socket.SocketPackage;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class GameController {

    private final Logger log = LoggerFactory.getLogger(GameController.class);

    private final UserDao userDao = AppContext.getBean(UserDao.class);

    private final GameWaitService gameWaitService = AppContext.getBean(GameWaitService.class);

    /**
     * 搜索对局
     */
    public void searchGame(RoutingContext ctx) {
        UserSession session = SessionStore.sessionUser(ctx);
        List<GameWaitDTO> games = gameWaitService.searchGames(
                session.isGuest ? null : session.username,
                ctx.queryParams().get("name"),
                GameMode.convert(ctx.queryParams().get("mode")),
                GameType.convert(ctx.queryParams().get("type")));
        RestContext.success(ctx, games);
    }

    /**
     * 创建对局
     */
    public void createGame(RoutingContext ctx) {
        GameWaitDTO dto = ctx.body().asPojo(GameWaitDTO.class);
        if (StringUtils.isBlank(dto.name) || dto.name.length() > 30 ||
                null == dto.type || null == dto.mode || null == dto.boardSize) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (!GameType.NONE.equals(dto.type)) {
            if (null == dto.duration || dto.duration <= 0 || null == dto.stepDuration || dto.stepDuration <= 0) {
                RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
        } else {
            dto.duration = 0;
            dto.stepDuration = 0;
        }

        if (GameMode.RANK.equals(dto.mode)) {
            if (!GameType.SHORT.equals(dto.type)) {
                RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            if (dto.boardSize != 19) {
                RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
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
            }).onFailure(ex -> {
                RestContext.fail(ctx, HttpResponseStatus.LOCKED);
            });
        }).onFailure(e -> {
            log.error(e.getMessage(), e);
            RestContext.fail(ctx);
        });

    }

    /**
     * 创建对局
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
            if (game == null) {
                RestContext.fail(ctx, RestStatus.GAME_NOT_EXIST);
            } else {
                // 将对局添加到缓存
                AppContext.REDIS.hset(List.of(RedisConstant.GAME_INFO + code,
                        "code", code,
                        "boardSize", game.boardSize.toString(),
                        "type", game.type.name(),
                        "mode", game.mode.name(),
                        "duration", game.duration.toString(),
                        "stepDuration", game.stepDuration.toString(),
                        "camp", System.currentTimeMillis() % 2 == 0 ? ChessPiece.WHITE.name() : ChessPiece.BLACK.name(),
                        "creator", game.username,
                        "contender", session.username
                )).onSuccess(size -> {
                    RestContext.success(ctx, code);
                    // 通知对方游戏开始
                    gameWaitService.getClientManger().send(SocketPackage.build(PackageType.GAME_START, code, session.username), game.username);
                }).onFailure(e -> {
                    log.error("{}: {}", e.getMessage(), code);
                    RestContext.fail(ctx);
                });
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
        }).onFailure(e -> {
            RestContext.fail(ctx, HttpResponseStatus.LOCKED);
        });
    }
}
