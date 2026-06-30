package cn.spirit.go;

import cn.spirit.go.service.GameRankedService;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.config.Config;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Consumer;

@DisplayName("GameRankedService")
@ExtendWith(VertxExtension.class)
public class RankedTest {

    private static final Logger log = LoggerFactory.getLogger(RankedTest.class);

    @Test
    @DisplayName("Ranking")
    void useRanking(Vertx vertx, VertxTestContext testContext) {
        AppContext.init(vertx, new Config());

        GameRankedService service = new GameRankedService();

        Consumer<String> matchSuccess = user -> {
            log.info("match success: {}", user);
        };

        Thread t1 = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                String name = "A-" + i;
                service.ranking(name, i, matchSuccess);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                String name = "A-" + i;
                service.ranking(name, i, matchSuccess);
            }
        });

        t1.start();
        t2.start();
        testContext.completeNow();
    }
}
