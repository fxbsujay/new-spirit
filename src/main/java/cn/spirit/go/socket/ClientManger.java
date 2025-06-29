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
     * 用户与Session
     * <用户名, SessionId>
     */
    private final Map<String, List<String>> clients = new ConcurrentHashMap<>();

    /**
     * 客户端
     * <sessionId, socket>
     */
    private final Map<String, WebSocket> sockets = new ConcurrentHashMap<>();


    /**
     * 创建连接
     * @param info      客户端 Session
     * @param socket    客户端
     * @return          是否注册成功
     */
    public boolean connect(SessionDTO info, WebSocket socket) {
        List<String> sessionIds = clients.computeIfAbsent(info.username, k -> new ArrayList<>());
        if (sessionIds.contains(info.sessionId)) {
            log.warn("WebSocket with session ID {} has already been registered", info.sessionId);
            return false;
        }
        log.info("WebSocket registration successful with session ID {}", info.sessionId);
        sessionIds.add(info.sessionId);
        sockets.put(info.sessionId, socket);
        return true;
    }

    /**
     * 关闭连接
     * @param info 客户端 Session
     * @param isClear 是否清空用户的所有连接，否则则只清空sessionID对应的连接
     */
    public void close(SessionDTO info, boolean isClear) {
        if (isClear) {
            List<String> sessionIds = clients.remove(info.username);
            if (sessionIds != null) {
                for (String sessionId : sessionIds) {
                    WebSocket webSocket = sockets.remove(sessionId);
                    if (webSocket != null) {
                        webSocket.close();
                    }
                }
            }
            log.info("WebSocket clear successful with username  {}", info.username);
        } else {
            List<String> sessionIds = clients.get(info.username);
            if (sessionIds != null && sockets.containsKey(info.sessionId)) {
                WebSocket webSocket = sockets.remove(info.sessionId);
                webSocket.close();
                log.info("WebSocket close successful with session ID {}", info.sessionId);
            } else {
                log.warn("WebSocket with session ID {} not found", info.sessionId);
            }
        }
    }

    /**
     * 发送消息
     *
     * @param pack      消息包
     * @param receivers 接收方
     */
    public void send(SocketPackage<?> pack, String ...receivers) {
        String msg = Json.encode(pack);
        log.info("Sending message to {}, package: {}", Arrays.toString(receivers), msg);
        for (String receiver : receivers) {
            List<String> sessionIds = clients.get(receiver);
            if (sessionIds != null) {
                for (String sessionId : sessionIds) {
                    WebSocket webSocket = sockets.get(sessionId);
                    if (webSocket != null) {
                        webSocket.writeFinalTextFrame(msg);
                    }
                }
            }
        }
    }
}
