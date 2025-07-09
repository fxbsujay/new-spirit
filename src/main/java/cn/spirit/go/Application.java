package cn.spirit.go;

import cn.spirit.go.config.AppContext;
import cn.spirit.go.config.RouterConfig;
import cn.spirit.go.socket.SocketHandler;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.Router;
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
        log.info("Application starting...");
        DatabindCodec.mapper().registerModule(new JavaTimeModule());

        AppContext.init(vertx);
        RouterConfig routerConfig = new RouterConfig();
        Router router = routerConfig.init(vertx);

        return vertx.createHttpServer().requestHandler(router).listen(8899).onSuccess(http -> {
            log.info("HTTP server started on port {}",  http.actualPort());
        });
    }
}
