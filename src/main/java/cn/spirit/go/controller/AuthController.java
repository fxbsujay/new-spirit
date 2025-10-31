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
import cn.spirit.go.model.dto.SignDTO;
import cn.spirit.go.model.entity.UserEntity;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserDao userDao = AppContext.getBean(UserDao.class);

    /**
     * 登录
     */
    public void signIn(RoutingContext ctx) {

        SignDTO dto = ctx.body().asPojo(SignDTO.class);

        String username = dto.username;
        String password = dto.password;

        if (!RegexUtils.matches(password, RegexUtils.PASSWORD)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        Future<UserEntity> future;
        if (RegexUtils.matches(username, RegexUtils.EMAIL)) {
            future = userDao.selectByEmail(username);
        } else if (RegexUtils.matches(username, RegexUtils.USERNAME)) {
            future = userDao.selectByUsername(username);
        } else {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        future.onSuccess(user -> {
            if (null == user) {
                RestContext.fail(ctx, RestStatus.ACCOUNT_NOT_EXIST);
                return;
            }
            if (user.status == UserStatus.BANNED) {
                RestContext.fail(ctx, RestStatus.PASSWORD_WRONG);
                return;
            }

            if (!SecurityUtils.matchesBCrypt(password, user.password)) {
                RestContext.fail(ctx, RestStatus.EMAIL_CODE_IS_INVALID);
                return;
            }

            SessionStore.logged(ctx, username).onSuccess(v -> {
                RestContext.success(ctx);
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
     * 注册
     */
    public void signUp(RoutingContext ctx) {
        SignDTO dto = ctx.body().asPojo(SignDTO.class);

        if (!RegexUtils.matches(dto.password, RegexUtils.PASSWORD) ||
                !RegexUtils.matches(dto.email, RegexUtils.EMAIL) ||
                !RegexUtils.matches(dto.username, RegexUtils.USERNAME) || StringUtils.isBlank(dto.code)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        userDao.selectByUsernameOrEmail(dto.username, dto.email).onSuccess(user -> {
            if (user != null) {
                if (user.username.equals(dto.username)) {
                    RestContext.fail(ctx, RestStatus.USERNAME_IS_EXIST);
                } else {
                    RestContext.fail(ctx, RestStatus.EMAIL_IS_EXIST);
                }
                return;
            }

            String key = RedisConstant.AUTH_CODE_SIGNUP + dto.email;
            AppContext.REDIS.get(key).onSuccess(v -> {
                if (null == v) {
                    RestContext.fail(ctx, RestStatus.SIGNUP_CODE_INVALID);
                } else {
                    if (dto.code.equals(v.toString())) {
                        UserEntity entity = new UserEntity();
                        entity.avatar = "avatar";
                        entity.username = dto.username;
                        entity.email = dto.email;
                        entity.nickname = dto.username;
                        entity.password = SecurityUtils.bCrypt(dto.password);
                        entity.status = UserStatus.NORMAL;
                        userDao.insert(entity).onSuccess(username -> {
                            RestContext.success(ctx, username);
                            AppContext.REDIS.del(List.of(key));
                        }).onFailure(e -> {
                            log.error(e.getMessage(), e.getCause());
                            RestContext.fail(ctx);
                        });
                    } else {
                        RestContext.fail(ctx, RestStatus.SIGNUP_CODE_ERROR);
                    }
                }
            }).onFailure(e -> RestContext.fail(ctx, RestStatus.SIGNUP_CODE_INVALID));
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
        SignDTO dto = ctx.body().asPojo(SignDTO.class);

        if (!RegexUtils.matches(dto.password, RegexUtils.PASSWORD) ||
                !RegexUtils.matches(dto.email, RegexUtils.EMAIL) ||
                !RegexUtils.matches(dto.username, RegexUtils.USERNAME)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        userDao.selectByUsernameOrEmail(dto.username, dto.email).onSuccess(user -> {
            if (user != null) {
                if (user.username.equals(dto.username)) {
                    RestContext.fail(ctx, RestStatus.USERNAME_IS_EXIST);
                } else {
                    RestContext.fail(ctx, RestStatus.EMAIL_IS_EXIST);
                }
                return;
            }

            String code = RandomUtils.getRandom(5, true);
            AppContext.REDIS.setex(RedisConstant.AUTH_CODE_SIGNUP + dto.email, RedisConstant.CODE_EXPIRE, code).onSuccess(v -> {
                RestContext.success(ctx, null);
                AppContext.sendMail("注册验证码", dto.email, code, false);
            }).onFailure(e -> {
                log.error(e.getMessage(), e.getCause());
                RestContext.fail(ctx);
            });
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }

}
