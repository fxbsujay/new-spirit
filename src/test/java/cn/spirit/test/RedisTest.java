package cn.spirit.test;

import cn.spirit.go.Application;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("Server")
@ExtendWith(VertxExtension.class)
public class RedisTest {

    private static final Logger log = LoggerFactory.getLogger(RedisTest.class);

    @Test
    @DisplayName("Redis Test")
    public void server(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new Application()).onComplete(testContext.succeeding(id -> {
            log.info("server deployed successfully");
            testContext.completeNow();
        }));
    }

}
