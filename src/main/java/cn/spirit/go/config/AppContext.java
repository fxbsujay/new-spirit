package cn.spirit.go.config;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.mail.*;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.redis.client.*;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppContext {

    private static final Logger log = LoggerFactory.getLogger(AppContext.class);

    public static Pool SQL_POOL;

    public static RedisAPI REDIS;

    public static MailClient MAIL;

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

        MailConfig mailConfig = new MailConfig()
                .setHostname("smtp.163.com")
                .setPort(465)
                .setSsl(true)
                .setStarttls(StartTLSOptions.REQUIRED)
                .setUsername("fsusured@163.com")
                .setPassword("JDUXN3hwa4GDLywg");

        MAIL = MailClient.createShared(vertx, mailConfig);
    }

    public static Future<MailResult> sendMail(String subject, String to, String content, boolean html) {
        MailMessage message = new MailMessage()
                .setFrom("fsusured@163.com (Spirit Go)")
                .setTo(to)
                .setSubject(subject);
        if (html) {
            message.setHtml(content);
        } else {
            message.setText(content);
        }
        log.info("Send email subject: {}, to: {}, content: {}", subject, to, content);
        return MAIL.sendMail(message);
    }
}
