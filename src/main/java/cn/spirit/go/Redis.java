package cn.spirit.go;

import cn.spirit.go.common.enums.UserStatus;
import cn.spirit.go.model.entity.UserEntity;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.launcher.application.VertxApplication;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collector;

public class Redis extends VerticleBase {


    private static final Logger log = LoggerFactory.getLogger(MysqlDB.class);

    @Override
    public Future<?> start() throws Exception {

        io.vertx.redis.client.Redis client = io.vertx.redis.client.Redis.createClient(vertx, new RedisOptions().addConnectionString("redis://localhost:6379"));
        RedisAPI REDIS = RedisAPI.api(client);
        long st = System.currentTimeMillis();
        return REDIS.setex("aa", "111", "111").compose(res -> {
            log.info("time = {}", System.currentTimeMillis() - st);
            return REDIS.get("aa");
        }).compose(r -> {
            log.info("time = {}", System.currentTimeMillis() - st);
            return REDIS.set(List.of("cc", "11"));
        }).onSuccess(res -> {
            log.info("time = {}", System.currentTimeMillis() - st);
        });
    }

    public static void main(String[] args) {
        VertxApplication.main(new String[]{Redis.class.getName()});
    }
}
