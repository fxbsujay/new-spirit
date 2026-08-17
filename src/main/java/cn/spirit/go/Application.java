package cn.spirit.go;

import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.config.Config;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;
import io.vertx.launcher.application.VertxApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application extends VerticleBase {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        log.info("Application starting...");
        VertxApplication.main(new String[]{Application.class.getName(), "-conf", "config.json"});
    }

    @Override
    public Future<?> start() {
        JsonObject conf = config();
        if (null == conf || conf.isEmpty()) {
            throw new RuntimeException("Missing configuration file");
        }
        Config config = conf.mapTo(Config.class);
        return vertx.createHttpServer()
                .requestHandler(AppContext.init(vertx, config))
                .listen(config.server.port)
                .onSuccess(http -> log.info("HTTP server started on port {}", http.actualPort()));
    }
}
