package cn.spirit.test;

import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.launcher.application.VertxApplication;

public class RedisTest extends VerticleBase {

    public static void main(String[] args) {
        VertxApplication.main(new String[]{RedisTest.class.getName()});
    }

    @Override
    public Future<?> start() throws Exception {
        AppContext.init(vertx);

        long l = System.currentTimeMillis();
        AppContext.REDIS.get("33").onSuccess(redis -> {
            System.out.println(System.currentTimeMillis()-l);
        });
        return Future.succeededFuture();
    }
}
