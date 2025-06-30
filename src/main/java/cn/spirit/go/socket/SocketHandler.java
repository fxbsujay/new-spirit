package cn.spirit.go.socket;

import cn.spirit.go.config.AppContext;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SocketHandler.class);

    private final ClientManger clientManger = AppContext.getBean(ClientManger.class);

    public void handle(ServerWebSocket ws) {

        ws.textMessageHandler(text -> {

        });
        ws.closeHandler(e -> {
            log.info("WebSocket closed: {}", e);
        });
        ws.drainHandler(event -> {
            log.info("WebSocket drain event: {}", event);
        });
        ws.endHandler(event -> {
            log.info("WebSocket end event: {}", event);
        });
        ws.exceptionHandler(event -> {
            log.error("WebSocket exception: {}", event);
        });
    }

}
