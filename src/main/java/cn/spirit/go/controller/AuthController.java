package cn.spirit.go.controller;

import cn.spirit.go.common.RedisConstant;
import cn.spirit.go.common.enums.RestStatus;
import cn.spirit.go.common.enums.UserStatus;
import cn.spirit.go.common.util.RandomUtils;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.common.util.SecurityUtils;
import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.config.AppContext;
import cn.spirit.go.model.dto.SignDTO;
import cn.spirit.go.model.dto.UserDTO;
import cn.spirit.go.model.entity.UserEntity;
import cn.spirit.go.service.UserService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class AuthController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public UserService service = new UserService();

    /**
     * 登录
     */
    public void signIn(RoutingContext ctx) {

        JsonObject body = ctx.body().asJsonObject();

        String username = body.getString("username");
        String password = body.getString("password");

        if (!RegexUtils.matches(password, RegexUtils.PASSWORD)) {
            fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        Future<UserEntity> future;
        if (RegexUtils.matches(username, RegexUtils.EMAIL)) {
            future = service.selectByEmail(username);
        } else if (RegexUtils.matches(password, RegexUtils.USERNAME)) {
            future = service.selectByUsername(username);
        } else {
            fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        future.onSuccess(user -> {
            if (user == null) {
                fail(ctx, RestStatus.ACCOUNT_NOT_EXIST);
                return;
            }

            if (user.status == UserStatus.BANNED) {
                fail(ctx, RestStatus.PASSWORD_WRONG);
                return;
            }

            if (!SecurityUtils.matchesBCrypt(password, user.password)) {
                fail(ctx, RestStatus.EMAIL_CODE_IS_INVALID);
                return;
            }

            String sessionId = StringUtils.uuid();
            String key = RedisConstant.AUTH_SESSION + sessionId;
            AppContext.REDIS
                    .hset(List.of(key, "session", sessionId, "username", username))
                    .compose(v -> AppContext.REDIS.expire(List.of(key, "3000")))
                    .onSuccess(v -> {
                        Cookie cookie = Cookie.cookie("sessionId", sessionId);
                        cookie.setHttpOnly(true);
                        ctx.response().addCookie(cookie);
                        UserDTO u = new UserDTO();
                        u.avatar = user.avatar;
                        u.email = user.email;
                        u.nickname = user.nickname;
                        u.username = user.username;
                        success(ctx, u);
                    }).onFailure(e -> {
                        fail(ctx);
                        log.error(e.getMessage(), e.getCause());
                    });

        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            fail(ctx);
        });
    }

    /**
     * 注册
     */
    public void signUp(RoutingContext ctx) {
        SignDTO dto = ctx.body().asPojo(SignDTO.class);

        if (!RegexUtils.matches(dto.password, RegexUtils.PASSWORD) ||
                !RegexUtils.matches(dto.email, RegexUtils.EMAIL) ||
                !RegexUtils.matches(dto.username, RegexUtils.USERNAME) || StringUtils.isBlank(dto.code)) {
            fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        service.selectByUsernameOrEmail(dto.username, dto.email).onSuccess(user -> {
            if (user != null) {
                if (user.username.equals(dto.username)) {
                    fail(ctx, RestStatus.USERNAME_IS_EXIST);
                } else {
                    fail(ctx, RestStatus.EMAIL_IS_EXIST);
                }
                return;
            }

            String key = RedisConstant.AUTH_CODE_SIGNUP + dto.email;
            AppContext.REDIS.get(key).onSuccess(v -> {
                if (dto.code.equals(v.toString())) {
                    UserEntity entity = new UserEntity();
                    entity.avatar = "avatar";
                    entity.username = dto.username;
                    entity.email = dto.email;
                    entity.nickname = dto.username;
                    entity.password = SecurityUtils.bCrypt(dto.password);
                    entity.status = UserStatus.NORMAL;
                    service.insert(entity).onSuccess(id -> {
                        success(ctx, null);
                        AppContext.REDIS.del(List.of(key));
                    }).onFailure(e -> {
                        log.error(e.getMessage(), e.getCause());
                        fail(ctx);
                    });
                } else {
                    fail(ctx, RestStatus.SIGNUP_CODE_ERROR);
                }
            }).onFailure(e -> {
                fail(ctx, RestStatus.SIGNUP_CODE_INVALID);
            });
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            fail(ctx);
        });
    }

    /**
     * 发送激活码
     */
    public void sendSignUpCode(RoutingContext ctx) {
        SignDTO dto = ctx.body().asPojo(SignDTO.class);

        if (!RegexUtils.matches(dto.password, RegexUtils.PASSWORD) ||
                !RegexUtils.matches(dto.email, RegexUtils.EMAIL) ||
                !RegexUtils.matches(dto.username, RegexUtils.USERNAME)) {
            fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        service.selectByUsernameOrEmail(dto.username, dto.email).onSuccess(user -> {
            if (user != null) {
                if (user.username.equals(dto.username)) {
                    fail(ctx, RestStatus.USERNAME_IS_EXIST);
                } else {
                    fail(ctx, RestStatus.EMAIL_IS_EXIST);
                }
                return;
            }

            String code = RandomUtils.getRandom(5, true);
            AppContext.REDIS.setex(RedisConstant.AUTH_CODE_SIGNUP + dto.email, RedisConstant.CODE_EXPIRE, code).onSuccess(v -> {
                success(ctx, null);
                AppContext.sendMail("注册验证码", dto.email, code, false);
            }).onFailure(e -> {
                log.error(e.getMessage(), e.getCause());
                fail(ctx);
            });
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            fail(ctx);
        });
    }

}
