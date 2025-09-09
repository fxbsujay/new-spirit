package cn.spirit.go.web.config;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.controller.AuthController;
import cn.spirit.go.controller.GameController;
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

        router.route().handler(BodyHandler.create());
        router.errorHandler(500, ctx -> {
            log.error("500", ctx.failure());
            ctx.response().setStatusCode(500).end();
        });

        authController(router);
        return router;
    }

    private static void authController(Router router) {
        SessionStore sessionHandle = new SessionStore();
        AuthController authController = new AuthController();
        GameController gameController = new GameController();
        router.get("/api/ping").handler(RestContext::success);
        router.route("/api/ws").handler(sessionHandle).handler(new SocketHandler());

        router.post("/api/auth/signin").handler(authController::signIn);
        router.post("/api/auth/signup").handler(authController::signUp);
        router.post("/api/auth/signup/code").handler(authController::sendSignUpCode);
        router.post("/api/auth/info").handler(authController::info);

        router.get("/api/game/search").handler(sessionHandle).handler(gameController::searchGame);
        router.post("/api/game/create").handler(sessionHandle).handler(gameController::createGame);
        router.post("/api/game/join/:code").handler(sessionHandle).handler(gameController::joinGame);
    }

}
