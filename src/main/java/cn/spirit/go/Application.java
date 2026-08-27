package cn.spirit.go;

import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.config.Config;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.launcher.application.VertxApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application extends VerticleBase {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        log.info("Application starting...");
        String[] launcherArgs = new String[args.length + 1];
        launcherArgs[0] = Application.class.getName();
        System.arraycopy(args, 0, launcherArgs, 1, args.length);
        VertxApplication.main(launcherArgs);

    }

    @Override
    public Future<?> start() {
        Config config = loadConfig();
        return vertx.createHttpServer()
                .requestHandler(AppContext.init(vertx, config))
                .listen(config.server.port)
                .onSuccess(http -> log.info("HTTP server started on port {}", http.actualPort()));
    }

    private Config loadConfig() {
        JsonObject conf = config();
        if (conf != null && !conf.isEmpty()) {
            return conf.mapTo(Config.class);
        }

        Buffer buffer = vertx.fileSystem().readFileBlocking("config.json");
        return Json.decodeValue(buffer, Config.class);
    }
}
