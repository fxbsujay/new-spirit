package cn.spirit.go.controller;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.RestStatus;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.common.util.SecurityUtils;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.service.GameWaitService;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.UserSession;
import cn.spirit.go.web.config.AppContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserDao userDao = AppContext.getBean(UserDao.class);

    private final GameWaitService gameWaitService = AppContext.getBean(GameWaitService.class);

    public UserController(Router router, SessionStore sessionHandle) {
        router.post("/api/user/info").handler(sessionHandle::handle).handler(this::info);
    }

    /**
     * 认证先
     */
    public void info(RoutingContext ctx) {
        UserSession session = SessionStore.sessionUser(ctx);
        userDao.findOne(JsonObject.of("username", session.username), "nickname", "avatar", "status").onSuccess(user -> {
            user.put("username", session.username);
            user.put("rating", 800);
            RestContext.success(ctx, user);
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }

    /**
     * 修改个人信息 昵称、头像、
     * @param ctx
     */
    public void updateInfo(RoutingContext ctx) {
    }

    /**
     * 修改邮箱
     * @param ctx 新邮箱、邮箱验证码、密码
     */
    public void updateEmail(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String password = body.getString("password");
        String code = body.getString("code");
        String email = body.getString("email");

        UserSession session = SessionStore.sessionUser(ctx);
        userDao.findOne(JsonObject.of("username", session.username), "password").onSuccess(user -> {
            if (!SecurityUtils.matchesBCrypt(password, user.getString("password"))) {
                RestContext.fail(ctx, RestStatus.EMAIL_CODE_IS_INVALID);
            } else {
                // TODO 修改邮箱
                RestContext.success(ctx);
            }
        });
    }

    /**
     * 修改密码
     * @param ctx 旧密码、新密码
     */
    public void updatePassword(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String oldPassword = body.getString("oldPassword");
        String newPassword = body.getString("newPassword");
        String confirmPassword = body.getString("confirmPassword");

        if (!RegexUtils.matches(oldPassword, RegexUtils.PASSWORD) ||
                !RegexUtils.matches(newPassword, RegexUtils.PASSWORD) ||
                !RegexUtils.matches(confirmPassword, RegexUtils.PASSWORD) ||
                !confirmPassword.equals(newPassword)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        UserSession session = SessionStore.sessionUser(ctx);
        userDao.findOne(JsonObject.of("username", session.username),  "password").onSuccess(user -> {
            if (!SecurityUtils.matchesBCrypt(oldPassword, user.getString("password"))) {
                RestContext.fail(ctx, RestStatus.EMAIL_CODE_IS_INVALID);
            } else {
                userDao.updatePassword(session.username, newPassword).onSuccess(username -> {
                    RestContext.success(ctx);
                }).onFailure(e -> {
                    log.error(e.getMessage(), e.getCause());
                    RestContext.fail(ctx);
                });
            }
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }
}
