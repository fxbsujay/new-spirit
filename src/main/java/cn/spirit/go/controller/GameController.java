package cn.spirit.go.controller;

import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.config.AppContext;
import cn.spirit.go.model.dto.GameDTO;
import cn.spirit.go.service.GameService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    private final GameService gameService = AppContext.getBean(GameService.class);

    /**
     * 搜索对局
     */
    public void searchGame(RoutingContext ctx) {

    }

    public void createGame(RoutingContext ctx) {
        GameDTO dto = ctx.body().asPojo(GameDTO.class);

        if (StringUtils.isBlank(dto.name) || dto.name.length() > 30 ||
                null == dto.type || null == dto.mode || null == dto.boardSize) {
            fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (!GameType.NONE.equals(dto.type)) {
            if (null == dto.duration || dto.duration <= 0 || null == dto.stepDuration || dto.stepDuration <= 0) {
                fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
        } else {
            dto.duration = 0;
            dto.stepDuration = 0;
        }

        if (GameMode.RANK.equals(dto.mode)) {
            if (!GameType.SHORT.equals(dto.type)) {
                fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            if (dto.boardSize != 19) {
                fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
        }

        gameService.createGame(dto).onSuccess(res -> {
            success(ctx, res);
        }).onFailure(e -> {
            log.error("create game failed", e);
            fail(ctx);
        });
    }
}
