package cn.spirit.go.common;

import cn.spirit.go.common.enums.RestStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.RoutingContext;

public class RestContext<P, T> {

    private final RoutingContext ctx;

    private P param;

    public RestContext(RoutingContext ctx, Class<P> cls) {
        this.ctx = ctx;
        if (cls != null) {
            params(cls);
        }
    }

    public RestContext(RoutingContext ctx) {
        this(ctx, null);
    }

    public P param() {
        return param;
    }

    public P params(Class<P> cls) {
        if (param == null) {
            param = ctx.body().asPojo(cls);
        }
        return param;
    }

    public void setParams(P param) {
        this.param = param;
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
}
