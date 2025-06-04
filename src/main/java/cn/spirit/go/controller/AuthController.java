package cn.spirit.go.controller;

import cn.spirit.go.common.enums.RestStatus;
import cn.spirit.go.common.enums.UserStatus;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.common.util.SecurityUtils;
import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.config.AppContext;
import cn.spirit.go.model.dto.UserDTO;
import cn.spirit.go.model.entity.UserEntity;
import cn.spirit.go.service.UserService;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

            if (user.status == UserStatus.INACTIVE) {
                fail(ctx, RestStatus.ACCOUNT_NOT_EXIST);
                return;
            } else if (user.status == UserStatus.BANNED) {
                fail(ctx, RestStatus.PASSWORD_WRONG);
                return;
            }

            if (!SecurityUtils.matchesBCrypt(password, user.password)) {
                fail(ctx, RestStatus.EMAIL_CODE_IS_INVALID);
                return;
            }

            String sessionId = StringUtils.uuid();
            String kye = "AUTH:SESSION:" + sessionId;
            AppContext.REDIS
                    .hset(List.of(kye, "session", sessionId, "username", username))
                    .compose(v -> AppContext.REDIS.expire(List.of(kye, "3000")))
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
        JsonObject body = ctx.body().asJsonObject();
        String username = body.getString("username");
        String password = body.getString("password");
        String email = body.getString("email");

        if (!RegexUtils.matches(password, RegexUtils.PASSWORD) ||
                !RegexUtils.matches(email, RegexUtils.EMAIL) ||
                !RegexUtils.matches(username, RegexUtils.USERNAME)) {
            fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        service.selectByUsernameOrEmail(username, email).onSuccess(user -> {
            if (user != null) {
                if (user.username.equals(username)) {
                    fail(ctx, RestStatus.USERNAME_IS_EXIST);
                } else {
                    fail(ctx, RestStatus.EMAIL_IS_EXIST);
                }
                return;
            }
            UserEntity entity = new UserEntity();
            entity.avatar = "avatar";
            entity.username = username;
            entity.email = email;
            entity.nickname = username;
            entity.password = SecurityUtils.bCrypt(password);
            entity.status = UserStatus.INACTIVE;

            service.insert(entity)
                    .onSuccess(id -> {
                String key = Base64.getEncoder().encodeToString(String.valueOf(id).getBytes(StandardCharsets.UTF_8));

                success(ctx, null);

                String link = "http://localhost:8891/auth/signup/verify/" + key;
                String html = "您好啊<b>" +
                        username +
                        "</b>，点击下发链接加入Spirit Go。<br/><a href=\"" +
                        link +
                        "\">" +
                        link +
                        "</a><br/>（无法点击链接？试着将它粘贴到你的浏览器！）<br/>点击 --- 联系我们";

                AppContext.sendMail("sendMail", email, html, true).onSuccess(res -> {
                    log.info("sen mail success: {}", res);
                });

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
