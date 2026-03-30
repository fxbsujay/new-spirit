package cn.spirit.go;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Lock;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("LockTest")
@ExtendWith(VertxExtension.class)
public class LockTest {

    private static final Logger log = LoggerFactory.getLogger(LockTest.class);

    @Test
    @DisplayName("with")
    void with(Vertx vertx, VertxTestContext testContext) {

        long st = System.currentTimeMillis();
        Future<Object> t1 = vertx.sharedData().withLock("name1", 200, () -> {
            log.info("Task T1");
            boolean flag = true;
            while (flag) {
                if (System.currentTimeMillis() - st > 10000) {
                    flag = false;
                }
            }
            return Future.succeededFuture();
        }).onFailure(e -> {
            log.info("Error T1");
        });

        Future<Object> t2 = vertx.sharedData().withLock("name2", 200, () -> {
            log.info("Task T2");
            boolean flag = true;
            while (flag) {
                if (System.currentTimeMillis() - st > 1000) {
                    flag = false;
                }
            }
            return Future.succeededFuture();
        }).onFailure(e -> {
            log.info("Error T2");
        });

        Future.all(t1, t2).await();
        testContext.completeNow();
    }

    @Test
    @DisplayName("with")
    void lock(Vertx vertx, VertxTestContext testContext) {
        Future<Lock> compose1 = vertx.sharedData().getLockWithTimeout("name", 200).onComplete(r  -> {
            if (r.succeeded()) {
                log.info("Task T3");
                Lock lock = r.result();
                // release the lock after 1 second
                vertx.setTimer(1000, l -> lock.release());
            } else {
                log.error("Error T3");
            }
        });

        Future<Lock> compose2 = vertx.sharedData().getLockWithTimeout("name", 200).onComplete(r  -> {
            if (r.succeeded()) {
                log.info("Task T4");
                Lock lock = r.result();
                // release the lock after 1 second
                vertx.setTimer(1000, l -> lock.release());
            } else {
                log.error("Error T4");
            }
        }).onFailure(e -> {
            log.error("Error T5");
        });

        Future.all(compose1, compose2).await();
        testContext.completeNow();
    }

}
