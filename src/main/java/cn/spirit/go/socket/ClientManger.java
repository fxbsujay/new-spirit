package cn.spirit.go.socket;

import cn.spirit.go.model.dto.SessionDTO;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 客户端管理器
 */
public class ClientManger {

    private static final Logger log = LoggerFactory.getLogger(ClientManger.class);

    /**
     * 客户端
     * <sessionId, socket>
     */
    private final Map<String, WebSocket> sockets = new ConcurrentHashMap<>();

    /**
     * 创建连接
     * @param sessionId 客户端 Session
     * @param socket    客户端
     * @return          是否注册成功
     */
    public boolean connect(String sessionId, WebSocket socket) {
        if (sockets.containsKey(sessionId)) {
            return false;
        }
        sockets.put(sessionId, socket);
        return true;
    }

    /**
     * 注销连接
     * @param sessionId 客户端 Session
     */
    public void cancel(String sessionId) {
        WebSocket s = sockets.remove(sessionId);
        if (s != null) {
            log.info("WebSocket cancel successful with session ID {}",sessionId);
        } else {
            log.warn("WebSocket with session ID {} not found", sessionId);
        }
    }

    /**
     * 关闭连接
     * @param sessionId 客户端 Session
     */
    public void close(String sessionId) {
        WebSocket webSocket = sockets.get(sessionId);
        if (webSocket != null) {
            webSocket.close();
        }
        log.info("WebSocket close successful with session ID {}",sessionId);
    }

    /**
     * 发送消息
     *
     * @param pack          消息包
     * @param sessionIds    接收方
     */
    public void send(SocketPackage<?> pack, String ...sessionIds) {
        String msg = Json.encode(pack);
        log.info("Sending message to {}, package: {}", Arrays.toString(sessionIds), msg);
        for (String sessionId : sessionIds) {
            WebSocket webSocket = sockets.get(sessionId);
            if (null != webSocket) {
                webSocket.writeFinalTextFrame(msg);
            }
        }
    }
}
