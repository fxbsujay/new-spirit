package cn.spirit.go.web.socket;

import cn.spirit.go.web.UserSession;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * WebSocket 客户端管理器
 */
public class ClientManger {

    private static final Logger log = LoggerFactory.getLogger(ClientManger.class);

    /**
     * 客户端
     * <username, sessionId, socket>
     */
    private final Map<String, Set<String>> userSessions = new HashMap<>();

    private final Map<String, WebSocket> sockets = new HashMap<>();

    /**
     * 是否有连接
     * @param session 客户端 Session
     */
    public boolean contains(UserSession session) {
        Set<String> sessions = userSessions.get(session.username);
        return null != sessions && sessions.contains(session.sessionId);
    }

    /**
     * 创建连接
     * @param session   客户端 Session
     * @param socket    客户端
     * @return          是否注册成功
     */
    public boolean connect(UserSession session, WebSocket socket) {
        if (contains(session)) {
            return false;
        }
        Set<String> sessions = userSessions.get(session.username);
        if (null == sessions) {
            sessions = new HashSet<>();
            sessions.add(session.sessionId);
            userSessions.put(session.username, sessions);
            sockets.put(session.sessionId, socket);
        } else {
            sessions.add(session.sessionId);
            sockets.put(session.sessionId, socket);
        }

        return true;
    }

    /**
     * 注销连接
     * @param session 客户端 Session
     */
    public void cancel(UserSession session) {
        Set<String> sessions = userSessions.get(session.username);
        if (null != sessions) {
            sessions.remove(session.sessionId);
            sockets.remove(session.sessionId);
        }
    }

    /**
     * 发送消息
     *
     * @param pack          消息包
     * @param usernames     接收方
     */
    public void sendToUser(SocketPackage pack, String ...usernames) {
        String msg = Json.encode(pack);
        log.info("Sending message to usernames: {}, package: {}", Arrays.toString(usernames), msg);
        for (String username : usernames) {
            Set<String> sessions = userSessions.get(username);
            if (null != sessions) {
                send(msg, sessions.toArray(new String[0]));
            }
        }
    }

    /**
     * 发送消息
     *
     * @param pack          消息包
     * @param sessionIds    接收方
     */
    public void sendToSession(SocketPackage pack, String ...sessionIds) {
        String msg = Json.encode(pack);
        log.info("Sending message to sessionIds: {}, package: {}", Arrays.toString(sessionIds), msg);
        send(msg, sessionIds);
    }

    private void send(String msg, String ...sessionIds) {
        for (String sessionId : sessionIds) {
            WebSocket socket = sockets.get(sessionId);
            if (null != socket) {
                socket.writeFinalTextFrame(msg);
            }
        }
    }

}
