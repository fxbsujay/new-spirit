package cn.spirit.go.config;

import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.redis.client.*;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppContext {

    private static final Logger log = LoggerFactory.getLogger(AppContext.class);

    public static String TABLE_PREFIX = "t_";

    public static Pool SQL_POOL;

    public static RedisAPI REDIS;

    public static void init(Vertx vertx) {

        MySQLConnectOptions conOpt = new MySQLConnectOptions()
                .setHost("8.133.248.55")
                .setPort(3306)
                .setDatabase("golang")
                .setUser("root")
                .setPassword("F521.wojiaofxb");

        PoolOptions poolOpt = new PoolOptions().setMaxSize(5);
        SQL_POOL = MySQLBuilder
                .pool()
                .with(poolOpt)
                .connectingTo(conOpt)
                .using(vertx)
                .build();


        Redis client = Redis.createClient(vertx, new RedisOptions().addConnectionString("redis://localhost:6379"));
        REDIS = RedisAPI.api(client);
    }
}
