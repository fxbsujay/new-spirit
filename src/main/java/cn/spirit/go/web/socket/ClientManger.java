package cn.spirit.go.web.socket;

import cn.spirit.go.web.UserSession;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.function.Consumer;

/**
 * WebSocket 客户端管理器
 */
public class ClientManger {

    private static final Logger log = LoggerFactory.getLogger(ClientManger.class);

    /**
     * 客户端
     * <username, sessionId, socket>
     */
    private final Map<String, Map<String, WebSocket>> sockets = new HashMap<>();

    /**
     * 客户端端口监听器
     */
    private final List<Consumer<UserSession>> cancelListeners = new ArrayList<>();

    /**
     * 是否有连接
     * @param session 客户端 Session
     */
    public boolean contains(UserSession session) {
        Map<String, WebSocket> sessionSockets = sockets.get(session.username);
        return null != sessionSockets && sessionSockets.containsKey(session.sessionId);
    }

    /**
     * 创建连接
     * @param session 客户端 Session
     * @param socket    客户端
     * @return          是否注册成功
     */
    public boolean connect(UserSession session, WebSocket socket) {
        if (contains(session)) {
            log.warn("WebSocket connection failed with ID {}, username = {}", session.sessionId, session.username);
            return false;
        }
        Map<String, WebSocket> sessionSockets = sockets.get(session.username);
        if (null == sessionSockets) {
            sessionSockets = new HashMap<>();
            sessionSockets.put(session.sessionId, socket);
            sockets.put(session.username, sessionSockets);
        } else {
            sessionSockets.put(session.sessionId, socket);
        }

        log.info("WebSocket connection successful with ID {}, username = {}", session.sessionId, session.username);
        return true;
    }

    /**
     * 注销连接
     * @param session 客户端 Session
     */
    public void cancel(UserSession session) {
        Map<String, WebSocket> sessionSockets = sockets.get(session.username);
        if (null != sessionSockets) {
            WebSocket s = sessionSockets.remove(session.sessionId);
            if (s != null) {
                log.info("WebSocket cancel successful with session ID {}, username = {}", session.sessionId, session.username);
            } else {
                log.warn("WebSocket cancel failed with session ID {} not found, username = {}", session.sessionId, session.username);
            }
            if (sessionSockets.isEmpty()) {
                sockets.remove(session.username);
            }
        }
        for (Consumer<UserSession> listener : cancelListeners) {
            listener.accept(session);
        }
    }

    /**
     * 发送消息
     *
     * @param pack          消息包
     * @param usernames    接收方
     */
    public void send(SocketPackage<?> pack, String ...usernames) {
        String msg = Json.encode(pack);
        log.info("Sending message to {}, package: {}", Arrays.toString(usernames), msg);
        for (String username : usernames) {
            Map<String, WebSocket> sessionSockets = sockets.get(username);
            if (null != sessionSockets) {
                for (WebSocket socket : sessionSockets.values()) {
                    socket.writeFinalTextFrame(msg);
                }
            }
        }
    }

    public void addCancelListener(Consumer<UserSession> cancelListener) {
        cancelListeners.add(cancelListener);
    }
}
