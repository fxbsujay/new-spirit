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
import io.vertx.ext.web.RoutingContext;
import java.util.List;

public class AuthController extends BaseController {

    public UserService service = new UserService();

    public void signIn(RoutingContext ctx) {

        JsonObject body = ctx.body().asJsonObject();

        String username = body.getString("username");
        String password = body.getString("password");

        if (!RegexUtils.matches(password, RegexUtils.PASSWORD)) {
            ctx.fail(400);
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
                    });
        }).onFailure(exception -> {
           exception.printStackTrace(System.err);
           fail(ctx, RestStatus.ACCOUNT_NOT_EXIST);
        });
    }
}
