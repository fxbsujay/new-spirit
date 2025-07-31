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
            cookie = Cookie.cookie(COOKIE_NAME, StringUtils.uuid());
            cookie.setSameSite(CookieSameSite.STRICT);
            cookie.setHttpOnly(true);
            ctx.response().addCookie(cookie);
        }
        ctx.next();
    }

    /**
     * 访客登录
     */
    public Future<Long> logged(RoutingContext ctx) {
        return logged(ctx, null);
    }

    /**
     * 登录
     */
    public Future<Long> logged(RoutingContext ctx, String username) {
        Cookie cookie = ctx.request().getCookie(COOKIE_NAME);
        if (cookie == null) {
            return Future.failedFuture("Cookie not found");
        }
        boolean isGuest = StringUtils.isEmpty(username);
        String sessionId = cookie.getValue();
        String key = AUTH_SESSION + sessionId;
        return AppContext.REDIS.hset(List.of(key,
                        "isGuest", Boolean.toString(isGuest),
                        "username", isGuest ? sessionId : username,
                        "ip", ctx.request().remoteAddress().hostAddress()))
                .compose(r -> AppContext.REDIS.expire(List.of(key, AUTH_SESSION_EXPIRE)))
                .map(Response::toLong);
    }

    /**
     * 退出
     */
    public void logout(String sessionId) {
        AppContext.REDIS.del(List.of(AUTH_SESSION + sessionId));
    }

    /**
     * 获取用户信息
     */
    public Future<UserSession> get(String sessionId) {
       return AppContext.REDIS.hgetall(AUTH_SESSION + sessionId).map(r -> {
           UserSession userSession = new UserSession();
           Response username = r.get("username");
           if (username != null) {
               userSession.username = username.toString();
           }
           Response isGuest = r.get("isGuest");
           if (isGuest != null) {
               userSession.isGuest = isGuest.toBoolean();
           }
           Response ip = r.get("ip");
           if (ip != null) {
               userSession.ip = ip.toString();
           }
           return userSession;
       });
    }

    public static class UserSession {

        /**
         * 是不是游客
         */
        public Boolean isGuest;

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
                    "isGuest=" + isGuest +
                    ", username='" + username + '\'' +
                    ", ip='" + ip + '\'' +
                    '}';
        }
    }

}
