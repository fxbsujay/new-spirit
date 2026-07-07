package cn.spirit.go;

import cn.spirit.go.common.enums.GameMode;
import cn.spirit.go.common.enums.GameReason;
import cn.spirit.go.common.enums.GameType;
import cn.spirit.go.common.enums.GameWinner;
import cn.spirit.go.dao.GameDao;
import cn.spirit.go.service.db.MongoStream;
import cn.spirit.go.service.db.SingleSubscriber;
import cn.spirit.go.web.config.AppContext;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@DisplayName("Sql")
@ExtendWith(VertxExtension.class)
public class SqlTest {

    private static final Logger log = LoggerFactory.getLogger(SqlTest.class);

    @Test
    @DisplayName("update rating")
    void updateRating(Vertx vertx, VertxTestContext testContext) {

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
                .codecRegistry(MongoStream.commonCodecRegistry)
                .build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase("spirit");
            log.info("---------------------");
            MongoCollection<Document> user = database.getCollection("user");
            Publisher<Document> first = user.find(Filters.eq("username", "admin1")).first();

            PromiseInternal<Document> promise = ((VertxInternal) vertx).promise();

            first.subscribe(new SingleSubscriber<>(promise));


            promise.future().onComplete(r -> {
                log.info(r.result().toJson());
                Publisher<JsonObject> first2 = database.getCollection("game", JsonObject.class).find(Filters.eq("code", "FILT3")).first();
                Flux.from(first2).doOnNext(r2 -> {
                    log.info("2 {}",r2.toString());
                }).blockLast();

                log.info("--------------3-------");
                Publisher<Document> first3 = database.getCollection("game").find(Filters.eq("code", "FILT3")).first();
                Flux.from(first3).doOnNext(r3 -> {
                    log.info("3 {}",r3.toJson());
                    log.info("3 {}",Json.decodeValue(r3.toJson(), JsonObject.class).toString());
                }).blockLast();

            });

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
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
            dao.insert(game).compose(res -> {
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
            String username = "admin1";
            GameDao dao = AppContext.getBean(GameDao.class);
            Bson query = Filters.or(Filters.eq("white", username), Filters.eq("black", username));
            JsonObject fields = MongoStream.exclude(MongoStream.ID_KEY, "board", "steps");
            dao.findPage(query, fields, page).onSuccess(res -> {
                log.info("Game query successful {}", Json.encode(res));
                testContext.completeNow();
            });
        }));
    }
}
