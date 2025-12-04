package cn.spirit.go.common;

import cn.spirit.go.common.enums.RestStatus;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.nio.charset.StandardCharsets;

public class RestContext {

    public static void success(RoutingContext ctx, Object data) {
        ctx.response()
                .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8")
                .end(Json.encode(data));
    }

    public static void success(RoutingContext ctx) {
        ctx.response().end();
    }

    public static void fail(RoutingContext ctx, HttpResponseStatus status) {
        ctx.response().setStatusCode(status.code()).end();
    }

    public static void fail(RoutingContext ctx, RestStatus status) {
        ctx.response().setStatusCode(500)
                .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_HTML + ";charset=utf-8")
                .setStatusMessage(status.getCode().toString()).end(status.toString());
    }

    public static void fail(RoutingContext ctx) {
        fail(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
}
