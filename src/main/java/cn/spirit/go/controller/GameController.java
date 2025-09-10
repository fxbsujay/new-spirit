package cn.spirit.go.controller;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.enums.RestStatus;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.model.dto.GameWaitDTO;
import cn.spirit.go.service.GameWaitService;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.UserSession;
import cn.spirit.go.web.config.AppContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class GameController {

    private final Logger log = LoggerFactory.getLogger(GameController.class);

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

        if (gameWaitService.addGame(SessionStore.sessionUser(ctx), dto)) {
            RestContext.success(ctx);
        } else {
            RestContext.fail(ctx, RestStatus.GAME_CREATED);
        }
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

        ctx.vertx().sharedData().withLock(code, 1000, () -> {
            log.info("---");
            RestContext.success(ctx);
            return Future.succeededFuture();
        }).onFailure(e -> {
            log.error("{}: {}", e.getMessage(), code);
            RestContext.fail(ctx, HttpResponseStatus.LOCKED);
        });
    }


}
