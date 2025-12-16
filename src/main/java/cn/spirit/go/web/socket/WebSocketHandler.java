package cn.spirit.go.web.socket;

import cn.spirit.go.service.GameWaitService;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Handler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负责系统消息的发送, 好友聊天
 */
public class WebSocketHandler implements Handler<RoutingContext> {

    private final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);

    private final ClientManger clientManger = AppContext.getBean(ClientManger.class);

    private final GameWaitService gameWaitService = AppContext.getBean(GameWaitService.class);

    public WebSocketHandler(Router router) {
        router.route("/api/ws").handler(this);
    }

    public void handle(RoutingContext ctx) {
        ctx.request().toWebSocket().onSuccess(ws -> SessionStore.validate(ctx).onSuccess(session -> {
            if (clientManger.connect(session, ws)) {
                ws.closeHandler(e -> {
                    clientManger.cancel(session);
                    if (!clientManger.isOnLine(session.username)) {
                        gameWaitService.removeGame(session.username);
                    }
                });
            } else {
                ws.close();
            }
        }).onFailure(e -> {
            ws.close();
        }));
    }
}
