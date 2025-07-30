package cn.spirit.go.service;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.*;
import cn.spirit.go.common.util.DateUtils;
import cn.spirit.go.config.AppContext;
import cn.spirit.go.dao.GameReadyDao;
import cn.spirit.go.model.dto.GameDTO;
import cn.spirit.go.model.dto.SessionDTO;
import cn.spirit.go.model.entity.GameReadyEntity;
import cn.spirit.go.model.dto.SearchGameDTO;
import io.vertx.core.Future;
import org.slf4j.Logger;
import java.util.List;
import org.slf4j.LoggerFactory;

public class GameService {

    private final Logger log = LoggerFactory.getLogger(GameService.class);

    private Integer dailyGameCount = 0;

    private String dailyTime = DateUtils.getTime("yyyyMMdd");

    private final GameReadyDao gameReadyDao = AppContext.getBean(GameReadyDao.class);

    public void searchGame(RestContext<Void, List<SearchGameDTO>> ctx) {
        SessionDTO session = ctx.sessionUser();
        gameReadyDao.searchPage(
                ctx.params("name"),
                GameMode.convert(ctx.params("mode")),
                GameType.convert(ctx.params("type")),
                UserIdentity.Logged == session.identity ? session.username : null
        ).onSuccess(ctx::success).onFailure(e -> {
            log.error(e.getMessage(), e);
            ctx.fail();
        });
    }

    public void joinGame(RestContext<String, Boolean> ctx) {
        String code = ctx.body();
        gameReadyDao.selectOneByCode(code).onSuccess(game -> {
            if (GameStatus.READY != game.status) {
                ctx.fail(RestStatus.GAME_STARTED);
                return;
            }
            ctx.lock("GAME:" + code, () -> {

            });
        }).onFailure(e -> {
            log.error(e.getMessage(), e);
            ctx.fail(RestStatus.GAME_NOT_EXIST);
        });
    }

    public void cancelGame(RestContext<String, Boolean> ctx) {

    }

    /**
     * 创建对局
     */
    public void createGame(RestContext<GameDTO, Boolean> ctx) {
        GameDTO dto = ctx.body();
        SessionDTO session = ctx.sessionUser();
        gameReadyDao.selectCountByUsername(session.username).compose(size -> {
            if (size > 0) {
                return Future.succeededFuture(null);
            }
            GameReadyEntity entity = new GameReadyEntity();
            entity.name = dto.name;
            entity.code = generateCode();
            entity.status = GameStatus.READY;
            entity.type = dto.type;
            entity.mode = dto.mode;
            entity.boardSize = dto.boardSize;
            entity.duration = dto.duration;
            entity.stepDuration = dto.stepDuration;
            entity.username = session.username;
            entity.score = session.source;
            return gameReadyDao.insert(entity);
        }).onSuccess(id -> {
            if (null == id) {
                ctx.fail(RestStatus.GAME_CREATED);
                return;
            }
            ctx.success(true);
        }).onFailure(e -> {
            log.error(e.getMessage(), e);
            ctx.fail();
        });
    }

    /**
     * 生成对局唯一编码
     * 当前日期 + 机器码 + 当日创建次数
     * 20250608 + 001 + 2
     */
    private String generateCode() {
        String time = DateUtils.getTime("yyyyMMdd");
        if (!time.equals(dailyTime)) {
            dailyTime = time;
            dailyGameCount = 0;
        }
        dailyGameCount++;
        return Long.toString(Long.parseLong(dailyTime.substring(2) + AppContext.MAC_CODE + dailyGameCount), 36).toUpperCase();
    }

    public static void main(String[] args) {
        GameService service = new GameService();
        System.out.println(service.generateCode());
        ;
    }
}
