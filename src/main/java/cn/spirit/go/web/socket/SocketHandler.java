package cn.spirit.go.web.socket;

import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.service.GameRoomService;
import cn.spirit.go.service.GameWaitService;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class SocketHandler implements Handler<RoutingContext> {

    private final Logger log = LoggerFactory.getLogger(SocketHandler.class);

    private final ClientManger clientManger = AppContext.getBean(ClientManger.class);

    private final GameWaitService gameWaitService = AppContext.getBean(GameWaitService.class);

    private final GameRoomService gameRoomService = AppContext.getBean(GameRoomService.class);

    public SocketHandler() {
        log.info("Web Socket path = /api/ws");
    }

    public void handle(RoutingContext ctx) {
        ctx.request().toWebSocket().onSuccess(ws -> {
            SessionStore.validate(ctx).onSuccess(session -> {
                if (clientManger.connect(session, ws)) {
                    ws.textMessageHandler(text -> {
                        SocketPackage pck;
                        try {
                            pck = Json.decodeValue(text, SocketPackage.class);
                        } catch (DecodeException e) {
                            log.error("Failed to parse websocket message packet, from: {}, sessionId: {}", session.username, session.sessionId);
                            ws.close();
                            return;
                        }
                        switch (pck.type) {
                            case SYS:
                                break;
                            case GAME_EXIT:
                                gameRoomService.exitRoom(session.username, (String) pck.data);
                                break;
                            case GAME_JOIN:
                                gameRoomService.joinRoom(session.username, (String) pck.data);
                                break;
                            case GAME_STEP:
                                Map<String, Object> obj = (Map) pck.data;
                                String code = (String) obj.get("code");
                                Integer x = (Integer) obj.get("x");
                                Integer y = (Integer) obj.get("y");
                                if (RegexUtils.mismatchGameCode(code) || x == null || y == null) {
                                    ws.close();
                                    return;
                                }
                                gameRoomService.addStep(session.username, code, x, y);
                                break;
                            default:
                                log.error("Illegal websocket message packet type, from: {}, sessionId: {}", session.username, session.sessionId);
                                ws.close();
                                return;
                        }
                    });
                    ws.closeHandler(e -> {
                        clientManger.cancel(session);
                        if (!clientManger.isOnLine(session.username)) {
                            gameWaitService.removeGame(session.username);
                            gameRoomService.exitRoom(session.username);
                        }
                    });
                } else {
                    ws.close();
                }
            }).onFailure(e -> {
                ws.close();
            });
        });
    }
}
