package cn.spirit.go.service;

import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.model.dto.GameSocket;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.web.socket.SocketPackage;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class RoomSocketHandle {

    private static final Logger log = LoggerFactory.getLogger(RoomSocketHandle.class);

    public GameRoomService roomService = AppContext.getBean(GameRoomService.class);

    public RoomSocketHandle(Router router) {
        router.route("/api/ws/:code").handler(this::handle);
    }

    public void handle(RoutingContext ctx) {
        ctx.request().toWebSocket().onSuccess(ws -> SessionStore.validate(ctx).onSuccess(session -> {
            String code = ctx.pathParam("code");
            if (RegexUtils.mismatchGameCode(code)) {
                ws.close();
                return;
            }
            GameSocket socket = new GameSocket(session, ws);
            boolean flag = roomService.joinRoom(code, socket);
            if (!flag) {
                ws.close();
                return;
            }
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
                    case GAME_STEP:
                        Map<String, Object> obj = (Map) pck.data;
                        Integer x = (Integer) obj.get("x");
                        Integer y = (Integer) obj.get("y");
                        if (RegexUtils.mismatchGameCode(code) || x == null || y == null) {
                            ws.close();
                            return;
                        }
                        roomService.addStep(session.username, code, x, y);
                        break;
                    default:
                        log.error("Illegal websocket message packet type, from: {}, sessionId: {}", session.username, session.sessionId);
                        ws.close();
                        return;
                }
            });
            ws.closeHandler(e -> {
                roomService.exitRoom(code, socket);
            });
        }).onFailure(e -> {
            ws.close();
        }));
    }
}
