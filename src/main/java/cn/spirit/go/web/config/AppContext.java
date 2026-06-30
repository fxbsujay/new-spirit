package cn.spirit.go.web.config;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.UploadBucket;
import cn.spirit.go.controller.AuthController;
import cn.spirit.go.controller.GameController;
import cn.spirit.go.controller.UserController;
import cn.spirit.go.dao.GameDao;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.service.GameManager;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.socket.ClientManger;
import cn.spirit.go.web.socket.WebSocketHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.CopyOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.*;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.redis.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AppContext {

    private static final Logger log = LoggerFactory.getLogger(AppContext.class);

    public static Vertx vertx;

    public static Config CONFIG;

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

    public static Router init(Vertx vertx, Config config) {
        AppContext.vertx = vertx;
        AppContext.CONFIG = config;

        MONGO = MongoClient.createShared(vertx, JsonObject.of("connection_string", "mongodb://" + config.mongodb.url, "db_name", config.mongodb.db));

        Redis client = Redis.createClient(vertx, new RedisOptions().addConnectionString("redis://" + config.redis.url).setPassword(config.redis.password));
        REDIS = RedisAPI.api(client);

        MailConfig mailConfig = new MailConfig()
                .setHostname(config.mail.host)
                .setPort(config.mail.port)
                .setSsl(true)
                .setStarttls(StartTLSOptions.REQUIRED)
                .setUsername(config.mail.username)
                .setPassword(config.mail.password);

        MAIL = MailClient.createShared(vertx, mailConfig);

        addBean(new ClientManger());

        addBean(new UserDao());
        addBean(new GameDao());


        Router router = Router.router(vertx);
        addBean(new GameManager(router));

        SessionStore sessionHandle = new SessionStore();
        router.get("/api/ping").handler(RestContext::success);

        new WebSocketHandler(router);

        router.route().handler(BodyHandler.create("./upload_temp").setBodyLimit(10 * 1024 * 1024).setDeleteUploadedFilesOnEnd(true));
        router.route("/api/static/*").handler(StaticHandler.create(config.server.storageFilePath));

        router.errorHandler(500, ctx -> {
            log.error("500", ctx.failure());
            RestContext.fail(ctx);
        });

        new AuthController(router);
        new GameController(router, sessionHandle);
        new UserController(router, sessionHandle);

        return router;
    }

    public static void sendMail(String subject, String to, String content, boolean html) {
        MailMessage message = new MailMessage()
                .setFrom(CONFIG.mail.username + " (Spirit Go)")
                .setTo(to)
                .setSubject(subject);
        if (html) {
            message.setHtml(content);
        } else {
            message.setText(content);
        }
        log.info("Send email subject: {}, to: {}, content: {}", subject, to, content);
        MAIL.sendMail(message);
    }

    public static Future<String> upload(UploadBucket bucket, FileUpload file) {
        String filename = file.uploadedFileName().substring(14) + file.fileName().substring(file.fileName().length() - 4);
        return vertx.fileSystem().move(file.uploadedFileName(), CONFIG.server.storageFilePath + "/" + bucket.name() + "/" + filename, new CopyOptions()).compose(res -> Future.succeededFuture(filename));
    }

    public static <T> Future<T> withLock(String name, Supplier<Future<T>> block) {
        return withLock(name, 1000, block);
    }

    public static <T> Future<T> withLock(String name, long timeout, Supplier<Future<T>> block) {
        return vertx.sharedData().withLock(name, timeout, block);
    }
}
