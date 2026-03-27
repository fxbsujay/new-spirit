package cn.spirit.go.web.config;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.util.KataGoUtils;
import cn.spirit.go.controller.AuthController;
import cn.spirit.go.controller.GameController;
import cn.spirit.go.controller.UserController;
import cn.spirit.go.web.socket.RoomSocketHandle;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.socket.WebSocketHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterConfig {

    private static final Logger log = LoggerFactory.getLogger(RouterConfig.class);

    public static Router init(Vertx vertx) {
        Router router = Router.router(vertx);

//        KataGoUtils utils = new KataGoUtils();

        SessionStore sessionHandle = new SessionStore();
        router.get("/api/ping").handler(RestContext::success);


        new WebSocketHandler(router);
        new RoomSocketHandle(router);

        router.route().handler(BodyHandler.create());
        router.errorHandler(500, ctx -> {
            log.error("500", ctx.failure());
            RestContext.fail(ctx);
        });

//        router.post("/api/kata").handler(ctx -> {
//            JsonObject json = ctx.body().asJsonObject();
//            utils.analysis(json).onSuccess(resp -> {
//                log.info("Kata analysis success: {}", resp);
//                ctx.response()
//                        .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8")
//                        .end(resp);
//            }).onFailure(resp -> {
//                log.info(resp.getMessage(), resp.getCause());
//            });
//        });

        new AuthController(router);
        new GameController(router, sessionHandle);
        new UserController(router, sessionHandle);

        return router;
    }
}
