package cn.spirit.go.web;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class SessionStore implements Handler<RoutingContext> {

    /**
     * Redis session key
     */
    private static final String AUTH_SESSION = "AUTH:SESSION:";

    /**
     * Request cookie key
     */
    private static final String SESSION_COOKIE_NAME = "sid";

    /**
     * session
     */
    private static final String SESSION_USER = "session";

    /**
     * Session过期时间 2 周
     */
    private static final long AUTH_SESSION_EXPIRE = 1209600;

    private static final Logger log = LoggerFactory.getLogger(SessionStore.class);

    @Override
    public void handle(RoutingContext ctx) {
        String sessionId = getSessionId(ctx);
        if (StringUtils.isBlank(sessionId) || sessionId.length() != 32) {
            ctx.response().setStatusCode(401).end();
            return;
        }

        getSession(sessionId).onSuccess(u -> {
            if (null == u) {
                ctx.response().setStatusCode(401).end();
            } else {
                ctx.put(SESSION_USER, u);
                refreshSession(sessionId);
                ctx.next();
            }
        }).onFailure(cause -> ctx.response().setStatusCode(401).end());
    }

    public static String setSessionCookie(RoutingContext ctx) {
        String sid = StringUtils.uuid();
        Cookie cookie = Cookie.cookie(SESSION_COOKIE_NAME, sid);
        cookie.setPath("/api");
        cookie.setMaxAge(AUTH_SESSION_EXPIRE);
        cookie.setHttpOnly(true);
        ctx.response().addCookie(cookie);
        ctx.put(SESSION_USER, cookie.getValue());
        log.info("session id {}", sid);
        return sid;
    }

    public static void refreshSession(String sessionId) {
        AppContext.REDIS.expire(List.of(AUTH_SESSION + sessionId, String.valueOf(AUTH_SESSION_EXPIRE)));
    }

    /**
     * 用户登录
     * @param username  用户名
     * @param score     分数
     * @return Void
     */
    public static Future<Void> logged(RoutingContext ctx, String username, Integer score, Boolean isGuest) {
        String sessionId = setSessionCookie(ctx);
        String value = username + ";" + score + ";" + ctx.request().remoteAddress().hostAddress() + ";" + isGuest;
        return AppContext.REDIS.setex(AUTH_SESSION + sessionId, String.valueOf(AUTH_SESSION_EXPIRE), value).map(r -> null);
    }

    /**
     * 退出并重置session
     */
    public static void logout(String sessionId) {
        AppContext.REDIS.del(List.of(AUTH_SESSION + sessionId));
    }

    public static String getSessionId(RoutingContext ctx) {
        Cookie cookie = ctx.request().getCookie(SESSION_COOKIE_NAME);
        if (cookie == null) {
            return ctx.get(SESSION_COOKIE_NAME);
        }
        return cookie.getValue();
    }

    public static UserSession sessionUser(RoutingContext ctx) {
        return ctx.get(SESSION_USER);
    }

    /**
     * 获取用户信息
     */
    public static Future<UserSession> getSession(String sessionId) {
        return AppContext.REDIS.get(AUTH_SESSION + sessionId).map(r -> {
            if (null != r) {
                UserSession userSession = new UserSession();
                String[] value = r.toString().split(";");
                userSession.sessionId = sessionId;
                userSession.username = value[0];
                userSession.score = Integer.parseInt(value[1]);
                userSession.ip = value[2];
                userSession.isGuest = Boolean.parseBoolean(value[3]);
                return userSession;
            }
            return null;
        });
    }
}
