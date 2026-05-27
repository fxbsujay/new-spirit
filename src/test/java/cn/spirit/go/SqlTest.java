package cn.spirit.go;

import cn.spirit.go.dao.UserDao;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
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
    void test1(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new Application()).onComplete(testContext.succeeding(id -> {
            UserDao dao = AppContext.getBean(UserDao.class);
            dao.updateRating("admin1", 10, "admin2",-10).onSuccess(res -> {
                log.info("update rating successful {}", Json.encode(res));
                testContext.completeNow();
            });
       }));
    }
}
