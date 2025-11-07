package cn.spirit.go;

import cn.spirit.go.common.enums.UserStatus;
import cn.spirit.go.model.entity.UserEntity;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.launcher.application.VertxApplication;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collector;

public class MysqlDB extends VerticleBase {


    private static final Logger log = LoggerFactory.getLogger(MysqlDB.class);

    @Override
    public Future<?> start() throws Exception {
        MySQLConnectOptions conOpt = new MySQLConnectOptions()
                .setHost("8.133.248.55")
                .setPort(3306)
                .setDatabase("golang")
                .setUser("root")
                .setPassword("F521.wojiaofxb");

        PoolOptions poolOpt = new PoolOptions()
                .setMaxSize(10);
        Pool SQL_POOL = MySQLBuilder
                .pool()
                .with(poolOpt)
                .connectingTo(conOpt)
                .using(vertx)
                .build();



        UserEntity entity = new UserEntity();
        entity.username = "root";
        entity.password = "F521.wojiaofxb";
        entity.nickname = "nickname";
        entity.avatar = "golang";
        entity.email = "email";
        entity.status = UserStatus.NORMAL;
        long st = System.currentTimeMillis();
        return SQL_POOL.preparedQuery("INSERT INTO t_user (avatar, nickname, email, status, username, password) VALUES (?, ?, ?, ?, ?, ?)")
                .collecting(Collector.of(() -> null, (v, row) -> {}, (a, b) -> null))
                .execute(Tuple.of(entity.avatar, entity.nickname, entity.email, entity.status, entity.username, entity.password))
                .compose(res -> {
                    System.out.println("Inserted id: " + res.toString());
                    log.info("time = {}", System.currentTimeMillis() - st);
                    return SQL_POOL.query("SELECT * FROM t_user WHERE username = 'root'").execute();
                }).onSuccess(res -> {
                    System.out.println("Inserted id: " + res.toString());
                    log.info("time = {}", System.currentTimeMillis() - st);
                });
    }

    public static void main(String[] args) {
        VertxApplication.main(new String[]{MysqlDB.class.getName()});
    }
}
