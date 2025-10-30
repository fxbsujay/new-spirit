package cn.spirit.go.web;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Future;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class SessionStore {

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

    public void handle(RoutingContext ctx) {
        handle(ctx, true);
    }
    /**
     * 身份验证
     * @param ctx       路由上下文
     * @param isLogged  是否必须是登录用户
     */
    public void handle(RoutingContext ctx, Boolean isLogged) {
        validate(ctx).onSuccess(u -> {
            if (isLogged && u.isGuest) {
                ctx.response().setStatusCode(401).end();
                return;
            }
            ctx.put(SESSION_USER, u);
            ctx.next();
        }).onFailure(cause -> ctx.response().setStatusCode(401).end());
    }

    public static Future<UserSession> validate(RoutingContext ctx) {
        String sid = getSessionId(ctx);
        if (StringUtils.isBlank(sid) || sid.length() != 32) {
            UserSession session = new UserSession();
            session.isGuest = true;
            String newSid = setSessionCookie(ctx);
            session.sessionId = newSid;
            session.username = newSid;
            return Future.succeededFuture(session);
        }
        return getSession(sid).compose(u -> {
            if (null == u) {
                UserSession session = new UserSession();
                session.isGuest = true;
                session.sessionId = sid;
                session.username = sid;
                return Future.succeededFuture(session);
            } else {
                refreshSession(sid);
                return Future.succeededFuture(u);
            }
        });
    }

    public static String setSessionCookie(RoutingContext ctx) {
        String sid = StringUtils.uuid();
        Cookie cookie = Cookie.cookie(SESSION_COOKIE_NAME, sid);
        cookie.setPath("/api");
        cookie.setMaxAge(AUTH_SESSION_EXPIRE);
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
     * @return Void
     */
    public static Future<Void> logged(RoutingContext ctx, String username) {
        String sessionId = setSessionCookie(ctx);
        String value = username + ";" + ctx.request().remoteAddress().hostAddress() ;
        return AppContext.REDIS.setex(AUTH_SESSION + sessionId, String.valueOf(AUTH_SESSION_EXPIRE), value).map(r -> null);
    }

    /**
     * 退出
     */
    public static void logout(String sessionId) {
        AppContext.REDIS.del(List.of(AUTH_SESSION + sessionId));
    }

    public static String getSessionId(RoutingContext ctx) {
        Cookie cookie = ctx.request().getCookie(SESSION_COOKIE_NAME);
        if (null == cookie) {
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
                userSession.ip = value[1];
                userSession.isGuest = false;
                return userSession;
            }
            return null;
        });
    }
}
