package cn.spirit.go.controller;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.RestStatus;
import cn.spirit.go.common.util.RegexUtils;
import cn.spirit.go.common.util.SecurityUtils;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.model.vo.UserInfoVO;
import cn.spirit.go.service.GameWaitService;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.UserSession;
import cn.spirit.go.web.config.AppContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserDao userDao = AppContext.getBean(UserDao.class);

    private final GameWaitService gameWaitService = AppContext.getBean(GameWaitService.class);

    /**
     * 认证先
     */
    public void info(RoutingContext ctx) {
        UserSession session = SessionStore.sessionUser(ctx);
        userDao.selectByUsername(session.username).onSuccess(user -> {
            UserInfoVO vo = new UserInfoVO();
            vo.username = session.username;
            vo.nickname = user.nickname;
            vo.avatar = user.avatar;
            vo.status = user.status;
            vo.rating = 800;
            RestContext.success(ctx, vo);
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }

    public void userInfo(RoutingContext ctx) {
        String username = ctx.pathParam("username");

    }

    /**
     * 修改密码
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
        userDao.selectByUsername(session.username).onSuccess(user -> {
            if (!SecurityUtils.matchesBCrypt(oldPassword, user.password)) {
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
