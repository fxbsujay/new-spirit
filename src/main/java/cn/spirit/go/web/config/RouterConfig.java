package cn.spirit.go.web.config;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.controller.AuthController;
import cn.spirit.go.controller.GameController;
import cn.spirit.go.controller.UserController;
import cn.spirit.go.service.RoomSocketHandle;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.socket.SocketHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterConfig {

    private static final Logger log = LoggerFactory.getLogger(RouterConfig.class);

    public static Router init(Vertx vertx) {
        Router router = Router.router(vertx);

        SessionStore sessionHandle = new SessionStore();
        router.get("/api/ping").handler(RestContext::success);
        router.route("/api/ws").handler(new SocketHandler());
        new RoomSocketHandle(router);

        router.route().handler(BodyHandler.create());
        router.errorHandler(500, ctx -> {
            log.error("500", ctx.failure());
            RestContext.fail(ctx);
        });

        new AuthController(router);
        new GameController(router, sessionHandle);
        new UserController(router, sessionHandle);

        return router;
    }
}
