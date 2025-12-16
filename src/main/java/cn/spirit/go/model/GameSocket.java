package cn.spirit.go.model;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.web.UserSession;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class GameSocket {

    private static final Logger log = LoggerFactory.getLogger(GameSocket.class);

    public String socketId;

    public String username;

    public String sessionId;

    private final ServerWebSocket socket;

    public GameSocket (UserSession session, ServerWebSocket socket) {
        this.sessionId = session.sessionId;
        this.username = session.username;
        this.socket = socket;
        this.socketId = StringUtils.uuid();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GameSocket that = (GameSocket) o;
        return Objects.equals(socketId, that.socketId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(socketId);
    }

    public ServerWebSocket getConnection() {
        return socket;
    }

    public void send(String msg) {
        if (!socket.isClosed()) {
            socket.writeFinalTextFrame(msg);
            log.info("send msg, target: {}, msg: {}", username,  msg);
        }
    }
}
