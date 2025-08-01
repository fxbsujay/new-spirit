package cn.spirit.go.web;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.CookieSameSite;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.Response;
import java.util.List;

public class RedisSession implements Handler<RoutingContext> {

    /**
     * Redis session key
     */
    private static final String AUTH_SESSION = "AUTH:SESSION:";

    /**
     * Request cookie key
     */
    private static final String COOKIE_NAME = "sessionId";

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

    public static Cookie setCookie(RoutingContext ctx) {
        Cookie cookie = Cookie.cookie(COOKIE_NAME, StringUtils.uuid());
        cookie.setSameSite(CookieSameSite.STRICT);
        cookie.setHttpOnly(true);
        ctx.response().addCookie(cookie);
        ctx.session().put(COOKIE_NAME, cookie.getValue());
        ctx.put(COOKIE_NAME, cookie.getValue());
        return cookie;
    }

    /**
     * 登录
     */
    public static Future<Long> logged(RoutingContext ctx, String username) {
        Cookie cookie = ctx.request().getCookie(COOKIE_NAME);
        if (cookie == null) {
            cookie = setCookie(ctx);
        }
        String key = AUTH_SESSION + cookie.getValue();
        String value = username + ";" + ctx.request().remoteAddress().hostAddress();
        return AppContext.REDIS.setex(key, AUTH_SESSION_EXPIRE, value).map(Response::toLong);
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

    /**
     * 获取用户信息
     */
    public static Future<UserSession> get(String sessionId) {
        return AppContext.REDIS.get(AUTH_SESSION + sessionId).map(r -> {
            UserSession userSession = new UserSession();
            String[] value = r.toString().split(";");
            userSession.username = value[0];
            userSession.ip = value[1];
            return userSession;
        });
    }

    public static class UserSession {

        /**
         * 用户
         */
        public String username;

        /**
         * 登录IP
         */
        public String ip;

        @Override
        public String toString() {
            return "UserSession{" +
                    "username='" + username + '\'' +
                    ", ip='" + ip + '\'' +
                    '}';
        }
    }

}
