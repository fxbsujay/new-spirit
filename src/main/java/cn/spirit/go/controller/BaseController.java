package cn.spirit.go.controller;

import cn.spirit.go.common.enums.RestStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.web.RoutingContext;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    public Map<String, Object> body(Integer code, Object data, String msg) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("data", data);
        body.put("msg", msg);
        return body;
    }

    public void success(RoutingContext ctx, Object data) {
        defaultResponse(ctx, body(200, data, "SUCCESS"));
    }

    public void defaultResponse(RoutingContext ctx, Object data) {
        String json;
        try {
            json = DatabindCodec.mapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ctx.response().putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8").end(json);
    }

    public void fail(RoutingContext ctx) {
        fail(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    public void fail(RoutingContext ctx, HttpResponseStatus status) {
        defaultResponse(ctx, body(status.code(), null, status.reasonPhrase()));
    }

    public void fail(RoutingContext ctx, RestStatus status) {
        defaultResponse(ctx, body(status.getCode(), null, status.getMessage()));
    }
}
