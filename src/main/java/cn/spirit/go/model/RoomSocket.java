package cn.spirit.go.model;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.web.UserSession;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;

public class RoomSocket {

    private static final Logger log = LoggerFactory.getLogger(RoomSocket.class);

    public String socketId;

    public String uid;

    public String sid;

    private final ServerWebSocket socket;

    public RoomSocket(UserSession session, ServerWebSocket socket) {
        this.sid = session.sId;
        this.uid = session.uid;
        this.socket = socket;
        this.socketId = StringUtils.uuid();
    }

    public ServerWebSocket getConnection() {
        return socket;
    }

    public void send(String msg) {
        if (!socket.isClosed()) {
            socket.writeFinalTextFrame(msg);
            log.info("send msg, target: {}, msg: {}", uid,  msg);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RoomSocket that = (RoomSocket) o;
        return Objects.equals(socketId, that.socketId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(socketId);
    }

}
