package cn.spirit.go.web.config;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.controller.AuthController;
import cn.spirit.go.controller.GameController;
import cn.spirit.go.web.RedisSession;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterConfig {

    private static final Logger log = LoggerFactory.getLogger(RouterConfig.class);

    public static Router init(Vertx vertx, RedisSession session) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(session);

        router.errorHandler(500, ctx -> {
            log.error("500", ctx.failure());
            ctx.response().setStatusCode(500).end();
        });

        authController(router, session);
        return router;
    }

    private static void authController(Router router, RedisSession session) {
        AuthController authController = new AuthController();
        GameController gameController = new GameController();
        router.get("/api/ping").handler(ctx -> {
            RestContext.success(ctx, true);
        });
        router.post("/api/auth/signin").handler(authController::signIn);
        router.post("/api/auth/signup").handler(authController::signUp);
        router.post("/api/auth/signup/code").handler(authController::sendSignUpCode);

        router.get("/api/game/search").handler(gameController::searchGame);
        router.post("/api/game/create").handler(session::verify).handler(gameController::createGame);
        router.post("/api/game/join/:code").handler(session::verify).handler(gameController::joinGame);
    }

}
