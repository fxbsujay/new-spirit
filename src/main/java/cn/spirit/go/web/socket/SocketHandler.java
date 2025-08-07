package cn.spirit.go.web.socket;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler implements Handler<ServerWebSocket> {

    private final Logger log = LoggerFactory.getLogger(SocketHandler.class);

    private final ClientManger clientManger = AppContext.getBean(ClientManger.class);

    public void handle(ServerWebSocket ws) {
        String[] split = new String[]{"1", "2"};
        if (split.length != 2) {
            ws.close();
            return;
        }
        String sessionId = split[1];
        if (StringUtils.isBlank(sessionId)) {
            ws.close();
            return;
        }


        SessionStore.getSession(sessionId).onSuccess(session -> {


        }).onFailure(event -> {
            log.error(event.getMessage());
            ws.close();
        });

//        if (clientManger.connect(session, ws)) {
//            ws.textMessageHandler(text -> {
//                SocketPackage<?> pck;
//                try {
//                    pck = Json.decodeValue(text, SocketPackage.class);
//                } catch (DecodeException e) {
//                    log.error("Failed to parse websocket message packet, from: {}, sessionId: {}", session.username, session.sessionId);
//                    clientManger.close(session);
//                    return;
//                }
//                switch (pck.type) {
//                    case SYS:
//                        break;
//                    default:
//                        log.error("Illegal websocket message packet type, from: {}, sessionId: {}", session.username, session.sessionId);
//                        clientManger.close(session);
//                        return;
//                }
//            });
//            ws.closeHandler(e -> {
//                clientManger.cancel(session);
//            });
//        } else {
//            ws.close();
//        }
    }
}
