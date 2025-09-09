package cn.spirit.go.web.socket;

import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.UserSession;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler implements Handler<RoutingContext> {

    private final Logger log = LoggerFactory.getLogger(SocketHandler.class);

    private final ClientManger clientManger = AppContext.getBean(ClientManger.class);

    public SocketHandler() {
        log.info("Web Socket path = /api/ws");
    }

    public void handle(RoutingContext ctx) {
        UserSession session = SessionStore.sessionUser(ctx);
        ctx.request().toWebSocket().onSuccess(ws -> {
            if (clientManger.connect(session.sessionId, ws)) {
                ws.textMessageHandler(text -> {
                    SocketPackage<?> pck;
                    try {
                        pck = Json.decodeValue(text, SocketPackage.class);
                    } catch (DecodeException e) {
                        log.error("Failed to parse websocket message packet, from: {}, sessionId: {}", session.username, session.sessionId);
                        clientManger.close(session.sessionId);
                        return;
                    }
                    switch (pck.type) {
                        case SYS:
                            break;
                        default:
                            log.error("Illegal websocket message packet type, from: {}, sessionId: {}", session.username, session.sessionId);
                            clientManger.close(session.sessionId);
                            return;
                    }
                });
                ws.closeHandler(e -> {
                    clientManger.cancel(session.sessionId);
                });
            } else {
                ws.close();
            }
        });
    }
}
