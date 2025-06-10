package cn.spirit.go.controller;

import cn.spirit.go.common.RestContext;
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

public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);
    private final GameService gameService = AppContext.getBean(GameService.class);

    /**
     * 搜索对局
     */
    public void searchGame(RoutingContext ctx) {

    }

    public void createGame(RoutingContext context) {

        RestContext<GameDTO, Boolean> ctx = new RestContext<>(context, GameDTO.class);

        GameDTO dto = ctx.param();

        if (StringUtils.isBlank(dto.name) || dto.name.length() > 30 ||
                null == dto.type || null == dto.mode || null == dto.boardSize) {
            ctx.fail(HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (!GameType.NONE.equals(dto.type)) {
            if (null == dto.duration || dto.duration <= 0 || null == dto.stepDuration || dto.stepDuration <= 0) {
                ctx.fail(HttpResponseStatus.BAD_REQUEST);
                return;
            }
        } else {
            dto.duration = 0;
            dto.stepDuration = 0;
        }

        if (GameMode.RANK.equals(dto.mode)) {
            if (!GameType.SHORT.equals(dto.type)) {
                ctx.fail(HttpResponseStatus.BAD_REQUEST);
                return;
            }

            if (dto.boardSize != 19) {
                ctx.fail(HttpResponseStatus.BAD_REQUEST);
                return;
            }
        }

        gameService.createGame(ctx);
    }
}
