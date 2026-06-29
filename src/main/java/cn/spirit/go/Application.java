package cn.spirit.go;

import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.config.Config;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.Json;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
import io.vertx.launcher.application.VertxApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application extends VerticleBase {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        log.info("Application starting...");
        VertxApplication.main(new String[]{Application.class.getName()});
    }

    @Override
    public Future<?> start() {
        return vertx.fileSystem().readFile("config.json").compose(buffer -> {
            DatabindCodec.mapper().registerModule(new JavaTimeModule());
            Router router = AppContext.init(vertx, Json.decodeValue(buffer, Config.class));
            return vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(8899)
                    .onSuccess(http -> {
                        log.info("HTTP server started on port {}",  http.actualPort());
                    });
        });

    }
}
