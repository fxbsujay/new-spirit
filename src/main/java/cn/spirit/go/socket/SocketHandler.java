package cn.spirit.go.socket;

import cn.spirit.go.config.AppContext;
import cn.spirit.go.model.dto.SessionDTO;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SocketHandler.class);

    private final ClientManger clientManger = AppContext.getBean(ClientManger.class);

    public void handle(SessionDTO session, ServerWebSocket ws) {
        if (clientManger.connect(session, ws)) {
            ws.textMessageHandler(text -> {
                SocketPackage<?> pck;
                try {
                    pck = Json.decodeValue(text, SocketPackage.class);
                } catch (DecodeException e) {
                    log.error("Failed to parse websocket message packet, from: {}, sessionId: {}", session.username, session.sessionId);
                    clientManger.close(session);
                    return;
                }

                switch (pck.type) {
                    case SYS:
                        break;
                    default:
                        log.error("Illegal websocket message packet type, from: {}, sessionId: {}", session.username, session.sessionId);
                        clientManger.close(session);
                        return;
                }

            });
            ws.closeHandler(e -> {
                clientManger.cancel(session);
            });

        } else {
            ws.close();
        }
    }
}
