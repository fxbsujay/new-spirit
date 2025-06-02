package cn.spirit.go;

import cn.spirit.go.config.AppContext;
import cn.spirit.go.controller.AuthController;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.launcher.application.VertxApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application extends VerticleBase {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        VertxApplication.main(new String[]{Application.class.getName()});
    }

    @Override
    public Future<?> start() {

        DatabindCodec.mapper().registerModule(new JavaTimeModule());

        AppContext.init(vertx);

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route("/api/*").handler(ctx -> {
            System.out.println("api/*");
            ctx.next();
        });

        AuthController controller = new AuthController();

        router.post("/api/auth/signin").handler(controller::signIn);

        router.errorHandler(500, ctx -> {
            ctx.response().setStatusCode(500).end();
        });

        return vertx.createHttpServer().requestHandler(router).listen(8899).onSuccess(http -> {
            log.info("HTTP server started on port 8888");
            log.error("HTTP server started on port 8888");
            log.debug("HTTP server started on port 8888");
            log.warn("HTTP server started on port 8888");
            Thread thread = new Thread(() -> {
                log.info("HTTP server started on port 8888");

                try {
                    int i = 10 / 0;
                } catch (Exception e) {
                    log.error("Exception", e);
                }
            });
            thread.setName("134243214534534534532534534532");
            thread.start();
        });
    }
}
