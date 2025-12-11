package cn.spirit.go.model.dto;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.web.UserSession;
import io.vertx.core.http.ServerWebSocket;
import java.util.Objects;

public class GameSocket {

    public String socketId;

    public String username;

    public String sessionId;

    public ServerWebSocket socket;

    public GameSocket (UserSession session, ServerWebSocket socket) {
        this.sessionId = session.sessionId;
        this.username = session.username;
        this.socket = socket;
        this.sessionId = StringUtils.uuid();
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
}
