package cn.spirit.go;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("Application")
@ExtendWith(VertxExtension.class)
public class ServerTest {

    private static final Logger log = LoggerFactory.getLogger(ServerTest.class);

    @Test
    @DisplayName("Web Server")
    void sampleServer(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new Application()).onComplete(testContext.succeeding(id -> {
            log.info("Deployment successful");
            testContext.completeNow();
        }));
    }
}
