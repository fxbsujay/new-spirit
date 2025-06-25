package cn.spirit.go.controller;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.config.AppContext;
import cn.spirit.go.model.dto.GameDTO;
import cn.spirit.go.model.dto.SearchGameDTO;
import cn.spirit.go.service.GameService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.ext.web.RoutingContext;
import java.util.List;

public class GameController {

    private final GameService gameService = AppContext.getBean(GameService.class);

    /**
     * 搜索对局
     */
    public void searchGame(RoutingContext context) {
        RestContext<Void, List<SearchGameDTO>> ctx = new RestContext<>(context);
        gameService.searchGame(ctx);
    }

    /**
     * 创建对局
     */
    public void createGame(RoutingContext context) {
        RestContext<GameDTO, Boolean> ctx = new RestContext<>(context, GameDTO.class);

        GameDTO dto = ctx.body();

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

    /**
     * 创建对局
     */
    public void joinGame(RoutingContext context) {
        String code = context.pathParam("code");

        if (!RegexUtils.matches(code, "[A-Z0-9]{5,}")) {
            RestContext.fail(context, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        RestContext<String, Boolean> ctx = new RestContext<>(context);
        ctx.setBody(code);
        gameService.joinGame(ctx);
    }
}
