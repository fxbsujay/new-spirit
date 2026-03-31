package cn.spirit.go.web.config;

import cn.spirit.go.dao.GameDao;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.service.GoMatchService;
import cn.spirit.go.service.GameLobbyService;
import cn.spirit.go.web.socket.ClientManger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.shareddata.impl.SharedDataImpl;
import io.vertx.ext.mail.*;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.redis.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
    public static MongoClient MONGO;

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

        MONGO = MongoClient.createShared(vertx, new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "spirit"));

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

        addBean(new GameLobbyService());
        addBean(new GoMatchService());
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

    public static <T> Future<T> withLock(String name, Supplier<T> block) {
        return withLock(name, SharedDataImpl.DEFAULT_LOCK_TIMEOUT, block);
    }

    public static <T> Future<T> withLock(String name, long timeout, Supplier<T> block) {
        Promise<T> promise = Promise.promise();
        vertx.sharedData().getLockWithTimeout(name, timeout).onComplete(r  -> {
            if (r.succeeded()) {
                Lock lock = r.result();
                vertx.executeBlocking(block::get).onSuccess(t -> {
                    promise.complete(t);
                    lock.release();
                }).onFailure(e -> {
                    promise.fail(e);
                    lock.release();
                });
            } else {
                promise.fail( new VertxException("Timed out waiting to get lock " + name, true));
            }
        });

        return promise.future();
    }

}
