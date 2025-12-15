package cn.spirit.go.controller;

import cn.spirit.go.common.RedisConstant;
import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.RestStatus;
import cn.spirit.go.common.util.RandomUtils;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.common.util.SecurityUtils;
import cn.spirit.go.common.util.StringUtils;
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

import java.util.List;

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
        userDao.findOne(JsonObject.of("username", session.username), "nickname", "avatar", "status", "rating").onSuccess(user -> {
            user.put("username", session.username);
            RestContext.success(ctx, user);
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }

    /**
     * 修改个人信息 昵称、头像、
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

        if (!RegexUtils.matches(password, RegexUtils.PASSWORD) ||
                !RegexUtils.matches(email, RegexUtils.EMAIL) ||
                !RegexUtils.matches(code, RegexUtils.CODE)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        UserSession session = SessionStore.sessionUser(ctx);
        String key = RedisConstant.AUTH_CODE_EMAIL + email;

        userDao.findOne(JsonObject.of("username", session.username),  "password").onSuccess(user -> {
            if (!SecurityUtils.matchesBCrypt(password, user.getString("password"))) {
                RestContext.fail(ctx, RestStatus.EMAIL_CODE_IS_INVALID);
            } else {
                AppContext.REDIS.get(key).onSuccess(v -> {
                    if (null == v) {
                        RestContext.fail(ctx, RestStatus.CODE_INVALID);
                    } else {
                        if (code.equals(v.toString())) {
                            userDao.updateEmail(session.username, email).onSuccess(_id -> {
                                RestContext.success(ctx);
                                AppContext.REDIS.del(List.of(key));
                            }).onFailure(e -> {
                                log.error(e.getMessage(), e.getCause());
                                RestContext.fail(ctx);
                            });
                        } else {
                            RestContext.fail(ctx, RestStatus.CODE_ERROR);
                        }
                    }
                }).onFailure(e -> RestContext.fail(ctx, RestStatus.CODE_INVALID));
            }
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }

    /**
     * 发生修改邮箱的验证码
     */
    public void sendUpdateEmailCode(RoutingContext ctx) {
        JsonObject auth = ctx.body().asJsonObject();
        String email = auth.getString("email");
        if (!RegexUtils.matches(email, RegexUtils.EMAIL)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        userDao.findCount(JsonObject.of("email", email)).onSuccess(size -> {
            if (size > 0) {
                RestContext.fail(ctx, RestStatus.EMAIL_IS_EXIST);
            } else {
                String code = RandomUtils.getRandom(5, true);
                AppContext.REDIS.setex(RedisConstant.AUTH_CODE_EMAIL + email, RedisConstant.CODE_EXPIRE, code).onSuccess(v -> {
                    RestContext.success(ctx);
                    AppContext.sendMail("修改邮箱验证", email, code, false);
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
