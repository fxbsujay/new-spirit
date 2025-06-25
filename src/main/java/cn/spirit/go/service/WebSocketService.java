package cn.spirit.go.service;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WebSocketService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    public void routerHandle(RoutingContext ctx) {
        ctx.request().toWebSocket().onSuccess(ws -> {
            // WebSocket 连接建立成功
            log.info("WebSocket connection established");
            ws.textMessageHandler(text -> {
                log.info("WebSocket text message: {}", text);
                if (text.equals("end")) {
                    ws.end();
                } else {
                    Map<String, Object> data = new HashMap<>();
                    data.put("time", new Date());
                    data.put("2", LocalDateTime.now());
                    data.put("3", LocalTime.now());
                    data.put("4", LocalDate.now());
                    ws.writeBinaryMessage(Buffer.buffer( Json.encode(data)));
                }
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
        });
    }
}
