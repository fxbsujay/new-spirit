package cn.spirit.go.common;

import cn.spirit.go.common.enums.RestStatus;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.UserSession;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.Lock;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Supplier;

public class RestContext<P, T> {

    private static final Logger log = LoggerFactory.getLogger(RestContext.class);

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

    public void lock(String name, Runnable runnable) {
        ctx.vertx().sharedData().withLock(name,() -> {
            runnable.run();
            return Future.succeededFuture(name);
        }).onFailure(e -> {
            log.error("{}: {}", e.getMessage(), name);
            fail(HttpResponseStatus.LOCKED);
        });
    }

    public void withLock(String name,  Supplier<Future<T>> block) {
        ctx.vertx().sharedData().withLock(name, block);
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

    public UserSession session() {
        return SessionStore.sessionUser(ctx);
    }

    public String sessionId() {
        return SessionStore.getSessionId(ctx);
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
        ctx.response()
                .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8")
                .end(Json.encode(data));
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
}
