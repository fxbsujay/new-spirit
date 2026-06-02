package cn.spirit.go;

import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameReason;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.enums.GameWinner;
import cn.spirit.go.common.util.SqlUtils;
import cn.spirit.go.dao.GameDao;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("Sql")
@ExtendWith(VertxExtension.class)
public class SqlTest {

    private static final Logger log = LoggerFactory.getLogger(SqlTest.class);

    @Test
    @DisplayName("update rating")
    void updateRating(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new Application()).onComplete(testContext.succeeding(id -> {
            UserDao dao = AppContext.getBean(UserDao.class);
            dao.updateRating("admin1", 10, "admin2",-10).onSuccess(res -> {
                log.info("update rating successful {}", Json.encode(res));
                testContext.completeNow();
            });
       }));
    }

    @Test
    @DisplayName("save game")
    void saveGame(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new Application()).onComplete(testContext.succeeding(id -> {
            GameDao dao = AppContext.getBean(GameDao.class);
            JsonArray steps = new JsonArray()
                    .add(new JsonObject().put("x", 2).put("y", 5))
                    .add(new JsonObject().put("x", 3).put("y", 6));

            JsonObject game = new JsonObject();
            game.put("code", "FDACE2");
            game.put("boardSize", 19);
            game.put("type", GameType.SHORT);
            game.put("mode", GameMode.CASUAL);
            game.put("duration", 200);
            game.put("stepDuration", 100);
            game.put("startTime", System.currentTimeMillis());
            game.put("endTime", System.currentTimeMillis());
            game.put("winner", GameWinner.BLACK);
            game.put("reason", GameReason.SURRENDER);
            game.put("board", JsonArray.of("AFB", "CG3"));
            game.put("steps", steps);
            dao.save(game).compose(res -> {
                log.info("Game save {}", res);
                return Future.succeededFuture(res);
            }).onSuccess(res -> {
                log.info("Game over");
                testContext.completeNow();
            }).onFailure(e -> {
                log.error("Game save failed");
            });
        }));
    }

    @Test
    @DisplayName("query game")
    void queryGame(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new Application()).onComplete(testContext.succeeding(id -> {
            int page = 1;
            int limit = 10;
            String username = "admin1";
            GameDao dao = AppContext.getBean(GameDao.class);
            JsonObject query = JsonObject.of("$or", new JsonArray()
                    .add(JsonObject.of("white", username))
                    .add(JsonObject.of("black", username)));

            FindOptions opts = SqlUtils.findOpts(0,"board", "steps");

            opts.setSort(JsonObject.of("startTime", -1));
            opts.setSkip((page - 1) * limit);
            opts.setLimit(limit);
            log.info("Game query");
            dao.find(query, opts).onSuccess(res -> {
                log.info("Game query successful {}", Json.encode(res));
                testContext.completeNow();
            });
        }));
    }
}
