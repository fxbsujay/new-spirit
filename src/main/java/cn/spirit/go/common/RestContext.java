package cn.spirit.go.common;

import cn.spirit.go.common.enums.RestStatus;
import cn.spirit.go.common.enums.UserIdentity;
import cn.spirit.go.model.dto.SessionDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.shareddata.Lock;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

public class RestContext<P, T> {

    private final RoutingContext ctx;

    private P body;

    public RestContext(RoutingContext ctx, Class<P> cls) {
        this.ctx = ctx;
        if (cls != null) {
            body = body(cls);
        }
    }

    public RoutingContext getContext() {
        return ctx;
    }

    public Future<Lock> lock(String name) {
        return ctx.vertx().sharedData().getLockWithTimeout(name, 3000L);
    }

    public String params(String name) {
        return ctx.queryParams().get(name);
    }

    public RestContext(RoutingContext ctx) {
        this(ctx, null);
    }

    public P body() {
        return body;
    }

    public P body(Class<P> cls) {
        if (null == body) {
            body = ctx.body().asPojo(cls);
        }
        return body;
    }

    public void setBody(P param) {
        this.body = param;
    }

    public void success(T data) {
        defaultResponse(new RestResponse(200, data, "SUCCESS"));
    }

    public void fail() {
        fail(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    public void fail(HttpResponseStatus status) {
        defaultResponse(new RestResponse(status));
    }

    public void fail(RestStatus status) {
        defaultResponse(new RestResponse(status));
    }

    private void defaultResponse(RestResponse response) {
        defaultResponse(this.ctx, response);
    }

    private static void defaultResponse(RoutingContext ctx, RestResponse data) {
        String json;
        try {
            json = DatabindCodec.mapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ctx.response().putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8").end(json);
    }

    private static class RestResponse {

        public Integer code;

        public Object data;

        public String msg;

        public RestResponse(Integer code, Object data, String msg) {
            this.code = code;
            this.data = data;
            this.msg = msg;
        }

        public RestResponse(Integer code, String msg) {
            this(code, null, msg);
        }

        public RestResponse(HttpResponseStatus status) {
            this(status.code(), status.reasonPhrase());
        }

        public RestResponse(RestStatus status) {
            this(status.getCode(), status.getMessage());
        }
    }

    public static void success(RoutingContext ctx, Object data) {
        defaultResponse(ctx, new RestResponse(200, data, "SUCCESS"));
    }

    public static void fail(RoutingContext ctx, HttpResponseStatus status) {
        defaultResponse(ctx, new RestResponse(status));
    }

    public static void fail(RoutingContext ctx, RestStatus status) {
        defaultResponse(ctx, new RestResponse(status));
    }

    public static void fail(RoutingContext ctx) {
        fail(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    public static void setLogged(RoutingContext ctx, Integer id, String username, String nickname, Integer score) {
        Session session = ctx.session();
        session.put("identity", UserIdentity.Logged);
        session.put("id", id);
        session.put("username", username);
        session.put("nickname", nickname);
        session.put("score", score);
        session.put("time", System.currentTimeMillis());
    }

    public static SessionDTO sessionUser(RoutingContext ctx) {
        SessionDTO dto = new SessionDTO();
        Session session = ctx.session();

        UserIdentity identity = session.get("identity");
        if (null == identity) {
            identity = UserIdentity.TOURIST;
            session.put("identity", identity);
        }

        String username = session.get("username");
        if (null == username) {
            username = "匿名用户";
            session.put("username", username);
        }

        String nickname = session.get("nickname");
        if (null == nickname) {
            nickname = "匿名用户";
            session.put("nickname", nickname);
        }

        Integer score = session.get("score");
        if (null == score) {
            score = 700;
            session.put("score", score);
        }

        dto.identity = identity;
        dto.id = session.get("id");
        dto.username = username;
        dto.nickname = nickname;
        dto.source = score;
        return dto;
    }

    public SessionDTO sessionUser() {
        return sessionUser(ctx);
    }
}
