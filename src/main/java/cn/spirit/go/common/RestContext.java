package cn.spirit.go.common;

import cn.spirit.go.common.enums.RestStatus;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class RestContext {

    public static void success(RoutingContext ctx, Object data) {
        defaultResponse(ctx, new RestResponse(200, data, "SUCCESS"));
    }

    public static void success(RoutingContext ctx) {
        defaultResponse(ctx, new RestResponse(200, null, "SUCCESS"));
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

}
