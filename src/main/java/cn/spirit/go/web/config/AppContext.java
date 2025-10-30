package cn.spirit.go.web.config;

import cn.spirit.go.dao.GameDao;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.service.GameRoomService;
import cn.spirit.go.service.GameWaitService;
import cn.spirit.go.web.socket.ClientManger;
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
import java.util.HashMap;
import java.util.Map;

public class AppContext {

    private static final Logger log = LoggerFactory.getLogger(AppContext.class);

    public static Vertx vertx;

    /**
     * 机器编码
     */
    public static String MAC_CODE = "1";

    /**
     * 数据库连接池
     */
    public static Pool SQL_POOL;

    /**
     * Redis Api
     */
    public static RedisAPI REDIS;

    /**
     * 邮件客户端
     */
    public static MailClient MAIL;

    /**
     * 单例对象
     */
    private static final Map<Class<?>, Object> beans = new HashMap<>();

    public static void addBean(Object bean) {
        beans.put(bean.getClass(), bean);
    }

    public static <T> T getBean(Class<T> clazz) {
        return (T) beans.get(clazz);
    }

    public static void init(Vertx vertx) {
        AppContext.vertx = vertx;
        MySQLConnectOptions conOpt = new MySQLConnectOptions()
                .setHost("8.133.248.55")
                .setPort(3306)
                .setDatabase("golang")
                .setUser("root")
                .setPassword("F521.wojiaofxb");

        PoolOptions poolOpt = new PoolOptions()
                .setMaxSize(10);
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

        addBean(new ClientManger());

        addBean(new UserDao());
        addBean(new GameDao());

        addBean(new GameWaitService());
        addBean(new GameRoomService());
        log.info("AppContext init success");
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
