package cn.spirit.go.controller;

import cn.spirit.go.common.RedisConstant;
import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.RestStatus;
import cn.spirit.go.common.enums.UserStatus;
import cn.spirit.go.common.util.RandomUtils;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.common.util.SecurityUtils;
import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.dao.UserDao;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserDao userDao = AppContext.getBean(UserDao.class);

    public AuthController(Router router) {
        router.post("/api/auth/signin").handler(this::signIn);
        router.post("/api/auth/signup").handler(this::signUp);
        router.post("/api/auth/signout").handler(this::signOut);
        router.post("/api/auth/signup/code").handler(this::sendSignUpCode);
        router.post("/api/auth/password").handler(this::resetPassword);
        router.post("/api/auth/password/code").handler(this::sendResetPasswordCode);
    }

    /**
     * 登录
     */
    public void signIn(RoutingContext ctx) {
        JsonObject auth = ctx.body().asJsonObject();
        String username = auth.getString("username");
        String password = auth.getString("password");

        if (!RegexUtils.matches(password, RegexUtils.PASSWORD)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        JsonObject query = new JsonObject();
        if (RegexUtils.matches(username, RegexUtils.EMAIL)) {
            query.put("email", username);
        } else if (RegexUtils.matches(username, RegexUtils.USERNAME)) {
            query.put("username", username);
        } else {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        userDao.findOne(query, "status", "password").onSuccess(user -> {
            if (null == user) {
                RestContext.fail(ctx, RestStatus.ACCOUNT_NOT_EXIST);
                return;
            }
            if (UserStatus.valueOf(user.getString("status")) == UserStatus.BANNED) {
                RestContext.fail(ctx, RestStatus.ACCOUNT_BAN);
                return;
            }

            if (!SecurityUtils.matchesBCrypt(password, user.getString("password"))) {
                RestContext.fail(ctx, RestStatus.EMAIL_CODE_IS_INVALID);
                return;
            }

            SessionStore.logged(ctx, username).onSuccess(v -> RestContext.success(ctx)).onFailure(e -> {
                log.error(e.getMessage(), e.getCause());
                RestContext.fail(ctx);
            });

        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }


    /**
     * 注册
     */
    public void signUp(RoutingContext ctx) {
        JsonObject auth = ctx.body().asJsonObject();
        String username = auth.getString("username");
        String password = auth.getString("password");
        String email = auth.getString("email");
        String code = auth.getString("code");

        if (!RegexUtils.matches(password, RegexUtils.PASSWORD) ||
                !RegexUtils.matches(email, RegexUtils.EMAIL) ||
                !RegexUtils.matches(username, RegexUtils.USERNAME) || !RegexUtils.matches(code, RegexUtils.CODE)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        JsonObject query = JsonObject.of("$or", new JsonArray()
                .add(JsonObject.of("username", username))
                .add(JsonObject.of("email", email)));
        userDao.findOne(query, "username").onSuccess(user -> {
            if (user != null) {
                if (username.equals(user.getString("username"))) {
                    RestContext.fail(ctx, RestStatus.USERNAME_IS_EXIST);
                } else {
                    RestContext.fail(ctx, RestStatus.EMAIL_IS_EXIST);
                }
                return;
            }

            String key = RedisConstant.AUTH_CODE_SIGNUP + email;
            AppContext.REDIS.get(key).onSuccess(v -> {
                if (null == v) {
                    RestContext.fail(ctx, RestStatus.CODE_INVALID);
                } else {
                    if (code.equals(v.toString())) {
                        JsonObject obj = JsonObject.of("username", username,
                                "email", email,
                                "password", SecurityUtils.bCrypt(password),
                                "avatar", "https://fxbsujay.github.io/favicon.ico",
                                "nickname", username,
                                "status", UserStatus.NORMAL);
                        userDao.insert(obj).onSuccess(_id -> {
                            RestContext.success(ctx, username);
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
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }

    /**
     * 退出
     */
    public void signOut(RoutingContext ctx) {
        SessionStore.logout(ctx);
        RestContext.success(ctx);
    }

    /**
     * 发送激活码
     */
    public void sendSignUpCode(RoutingContext ctx) {
        JsonObject auth = ctx.body().asJsonObject();
        String username = auth.getString("username");
        String password = auth.getString("password");
        String email = auth.getString("email");

        if (!RegexUtils.matches(password, RegexUtils.PASSWORD) ||
                !RegexUtils.matches(email, RegexUtils.EMAIL) ||
                !RegexUtils.matches(username, RegexUtils.USERNAME)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        JsonObject query = JsonObject.of("$or", new JsonArray()
                .add(JsonObject.of("username", username))
                .add(JsonObject.of("email", email)));
        userDao.findOne(query, "username").onSuccess(user -> {
            if (user != null) {
                if (username.equals(user.getString("username"))) {
                    RestContext.fail(ctx, RestStatus.USERNAME_IS_EXIST);
                } else {
                    RestContext.fail(ctx, RestStatus.EMAIL_IS_EXIST);
                }
                return;
            }

            String code = RandomUtils.getRandom(5, true);
            AppContext.REDIS.setex(RedisConstant.AUTH_CODE_SIGNUP + email, RedisConstant.CODE_EXPIRE, code).onSuccess(v -> {
                RestContext.success(ctx);
                AppContext.sendMail("注册验证码", email, code, false);
            }).onFailure(e -> {
                log.error(e.getMessage(), e.getCause());
                RestContext.fail(ctx);
            });
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }

    /**
     * 发送找回密码验证码
     */
    public void sendResetPasswordCode(RoutingContext ctx) {
        JsonObject auth = ctx.body().asJsonObject();
        String email = auth.getString("email");

        if (!RegexUtils.matches(email, RegexUtils.EMAIL)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        userDao.findCount(JsonObject.of("email", email)).onSuccess(size -> {
            if (size != 1) {
                RestContext.fail(ctx, RestStatus.EMAIL_IS_EXIST);
                return;
            }

            String code = RandomUtils.getRandom(5, true);
            AppContext.REDIS.setex(RedisConstant.AUTH_CODE_PASSWORD + email, RedisConstant.CODE_EXPIRE, code).onSuccess(v -> {
                RestContext.success(ctx);
                AppContext.sendMail("忘记密码", email, code, false);
            }).onFailure(e -> {
                log.error(e.getMessage(), e.getCause());
                RestContext.fail(ctx);
            });
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }

    /**
     * 重置密码
     */
    public void resetPassword(RoutingContext ctx) {
        JsonObject auth = ctx.body().asJsonObject();
        String password = auth.getString("password");
        String email = auth.getString("email");
        String code = auth.getString("code");

        if (!RegexUtils.matches(password, RegexUtils.PASSWORD) ||
                !RegexUtils.matches(email, RegexUtils.EMAIL) ||
                !RegexUtils.matches(code, RegexUtils.CODE)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        userDao.findOne(JsonObject.of("email", email), "username").onSuccess(user -> {
            if (null == user) {
                RestContext.fail(ctx, RestStatus.ACCOUNT_NOT_EXIST);
                return;
            }
            String key = RedisConstant.AUTH_CODE_PASSWORD + email;
            AppContext.REDIS.get(key).onSuccess(v -> {
                if (null == v) {
                    RestContext.fail(ctx, RestStatus.CODE_INVALID);
                } else {
                    if (code.equals(v.toString())) {
                        userDao.updatePassword(user.getString("username"),  SecurityUtils.bCrypt(password)).onSuccess(_id -> {
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
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });

    }

}
