package cn.spirit.go.controller;

import cn.spirit.go.common.RestContext;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.model.vo.UserInfoVO;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.UserSession;
import cn.spirit.go.web.config.AppContext;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserDao userDao = AppContext.getBean(UserDao.class);

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
            vo.email = user.email;
            RestContext.success(ctx, vo);
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }

}
