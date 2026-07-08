package cn.spirit.go;

import cn.spirit.go.service.db.MongoStream;
import cn.spirit.go.web.config.AppContext;
import com.mongodb.client.model.Filters;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("Mongo db Test")
@ExtendWith(VertxExtension.class)
public class SqlTest {

    private static final Logger log = LoggerFactory.getLogger(SqlTest.class);

    @Test
    @DisplayName("User sql")
    void insert(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new Application()).onComplete(testContext.succeeding(id -> {
            MongoStream stream = AppContext.getBean(MongoStream.class);
            stream.insertOne("user", JsonObject.of("username", "test", "password", "test")).onSuccess(uid -> {
                log.info("insert success, id = {}", uid);
                stream.promiseOne(stream.getCollection("user").deleteOne(Filters.eq(MongoStream.ID_KEY, uid)))
                        .onSuccess(res -> {
                            log.info("delete success, deleted count = {}", res.getDeletedCount());
                            testContext.completeNow();
                        });

            });
        }));
    }
}
