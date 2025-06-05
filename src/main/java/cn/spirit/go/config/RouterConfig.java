package cn.spirit.go.config;

import cn.spirit.go.controller.AuthController;
import cn.spirit.go.service.UserService;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouterConfig {

    private static final Logger log = LoggerFactory.getLogger(RouterConfig.class);

    public void scanBean() {
        UserService userService = new UserService();
        AppContext.addBean(userService);
    }

    public void init(Router router) {
        scanBean();

        router.route().handler(BodyHandler.create());
        router.route("/api/*").handler(ctx -> {
            log.info("Request path: {}, remote addr: {}", ctx.request().path(), ctx.request().remoteAddress());
            ctx.next();
        });

        router.errorHandler(500, ctx -> {
            log.error("500", ctx.failure());
            ctx.response().setStatusCode(500).end();
        });

        authController(router);
    }

    private void authController(Router router) {

        AuthController controller = new AuthController();

        router.post("/api/auth/signin").handler(controller::signIn);
        router.post("/api/auth/signup").handler(controller::signUp);
        router.post("/api/auth/signup/code").handler(controller::sendSignUpCode);
    }
}
