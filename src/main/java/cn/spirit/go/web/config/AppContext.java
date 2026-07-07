package cn.spirit.go.web.config;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.controller.AuthController;
import cn.spirit.go.controller.GameController;
import cn.spirit.go.controller.UserController;
import cn.spirit.go.dao.GameDao;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.service.db.MongoStream;
import cn.spirit.go.service.sys.FileStorageSystem;
import cn.spirit.go.service.GameManager;
import cn.spirit.go.service.sys.MailSystem;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.socket.ClientManger;
import cn.spirit.go.web.socket.WebSocketHandler;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
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

    /**
     * 机器编码
     */
    public static String MAC_CODE = "1";

    /**
     * Redis Api
     */
    public static RedisAPI REDIS;

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

        MongoStream mongoStream = new MongoStream(vertx,"mongodb://" +  config.mongodb.url, config.mongodb.db);

        Redis client = Redis.createClient(vertx, new RedisOptions().addConnectionString("redis://" + config.redis.url).setPassword(config.redis.password));
        REDIS = RedisAPI.api(client);
        Router router = Router.router(vertx);
        addBean(new ClientManger());
        addBean(mongoStream);
        addBean(new UserDao(mongoStream));
        addBean(new GameDao(mongoStream));

        addBean(new FileStorageSystem(vertx.fileSystem(), config.server.storageFilePath));
        addBean(new MailSystem(vertx, config.mail));
        addBean(new GameManager(router));

        SessionStore sessionHandle = new SessionStore();
        router.get("/api/ping").handler(RestContext::success);

        new WebSocketHandler(router);

        router.route().handler(BodyHandler.create("./upload_temp").setBodyLimit(config.server.bodyLimit));
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

    public static <T> Future<T> withLock(String name, Supplier<Future<T>> block) {
        return withLock(name, 1000, block);
    }

    public static <T> Future<T> withLock(String name, long timeout, Supplier<Future<T>> block) {
        return vertx.sharedData().withLock(name, timeout, block);
    }
}
