package cn.spirit.go.web.socket;

import cn.spirit.go.web.UserSession;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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
     * 客户端端口监听器
     */
    private final List<Consumer<UserSession>> cancelListeners = new ArrayList<>();

    /**
     * 是否有连接
     * @param sessionId 客户端 Session
     */
    public boolean contains(String sessionId) {
        return sockets.containsKey(sessionId);
    }

    /**
     * 创建连接
     * @param session 客户端 Session
     * @param socket    客户端
     * @return          是否注册成功
     */
    public boolean connect(UserSession session, WebSocket socket) {
        if (contains(session.sessionId)) {
            log.warn("WebSocket connection failed with ID {}, username = {}", session.sessionId, session.username);
            return false;
        }
        sockets.put(session.sessionId, socket);
        log.info("WebSocket connection successful with ID {}, username = {}", session.sessionId, session.username);
        return true;
    }

    /**
     * 注销连接
     * @param session 客户端 Session
     */
    public void cancel(UserSession session) {
        WebSocket s = sockets.remove(session.sessionId);
        if (s != null) {
            log.info("WebSocket cancel successful with session ID {}, username = {}", session.sessionId, session.username);
        } else {
            log.warn("WebSocket cancel failed with session ID {} not found, username = {}", session.sessionId, session.username);
        }
        for (Consumer<UserSession> listener : cancelListeners) {
            listener.accept(session);
        }
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

    public void addCancelListener(Consumer<UserSession> cancelListener) {
        cancelListeners.add(cancelListener);
    }
}
