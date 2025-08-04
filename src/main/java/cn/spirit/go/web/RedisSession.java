package cn.spirit.go.web;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.ext.web.RoutingContext;
import java.util.List;

public class RedisSession implements Handler<RoutingContext> {

    /**
     * Redis session key
     */
    private static final String AUTH_SESSION = "AUTH:SESSION:";

    /**
     * Request cookie key
     */
    private static final String COOKIE_NAME = "sid";

    /**
     * session
     */
    private static final String SESSION_USER = "su";

    /**
     * Session过期时间 2 周
     */
    private static final String AUTH_SESSION_EXPIRE = "1209600";

    @Override
    public void handle(RoutingContext ctx) {
        Cookie cookie = ctx.request().getCookie(COOKIE_NAME);
        if (cookie == null) {
           setCookie(ctx);
        } else {
            ctx.put(COOKIE_NAME, cookie.getValue());
        }
        ctx.next();
    }

    public void verify(RoutingContext ctx) {
        get(getSessionId(ctx)).onSuccess(u -> {
            if (null == u) {
                ctx.response().setStatusCode(401).end();
            } else {
                ctx.put(SESSION_USER, u);
                ctx.next();
            }
        }).onFailure(cause -> ctx.response().setStatusCode(401).end());
    }

    public static Cookie setCookie(RoutingContext ctx) {
        Cookie cookie = Cookie.cookie(COOKIE_NAME, StringUtils.uuid());
        cookie.setSameSite(CookieSameSite.STRICT);
        cookie.setHttpOnly(true);
        ctx.response().addCookie(cookie);
        ctx.put(COOKIE_NAME, cookie.getValue());
        return cookie;
    }

    /**
     * 登录
     */
    public static Future<Void> logged(RoutingContext ctx, String username) {
        Cookie cookie = setCookie(ctx);
        String key = AUTH_SESSION + cookie.getValue();
        String value = username + ";" + ctx.request().remoteAddress().hostAddress();
        return AppContext.REDIS.setex(key, AUTH_SESSION_EXPIRE, value).map(r -> null);
    }

    /**
     * 退出
     */
    public static void logout(String sessionId) {
        AppContext.REDIS.del(List.of(AUTH_SESSION + sessionId));
    }

    public static String getSessionId(RoutingContext ctx) {
        Cookie cookie = ctx.request().getCookie(COOKIE_NAME);
        if (cookie == null) {
            return ctx.get(COOKIE_NAME);
        }
        return cookie.getValue();
    }

    public static UserSession sessionUser(RoutingContext ctx) {
        return ctx.get(SESSION_USER);
    }

    /**
     * 获取用户信息
     */
    public static Future<UserSession> get(String sessionId) {
        return AppContext.REDIS.get(AUTH_SESSION + sessionId).map(r -> {
            if (null != r) {
                UserSession userSession = new UserSession();
                String[] value = r.toString().split(";");
                userSession.sessionId = sessionId;
                userSession.username = value[0];
                userSession.ip = value[1];
                return userSession;
            }
            return null;
        });
    }
}
