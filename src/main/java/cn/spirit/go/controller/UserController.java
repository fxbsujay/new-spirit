package cn.spirit.go.controller;

import cn.spirit.go.common.RedisConstant;
import cn.spirit.go.common.RestContext;
import cn.spirit.go.common.enums.RestStatus;
import cn.spirit.go.common.enums.UploadBucket;
import cn.spirit.go.common.util.*;
import cn.spirit.go.dao.GameDao;
import cn.spirit.go.dao.UserDao;
import cn.spirit.go.service.db.MongoStream;
import cn.spirit.go.service.sys.FileStorageSystem;
import cn.spirit.go.service.sys.MailSystem;
import cn.spirit.go.web.SessionStore;
import cn.spirit.go.web.UserSession;
import cn.spirit.go.web.config.AppContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserDao userDao = AppContext.getBean(UserDao.class);

    private final GameDao gameDao = AppContext.getBean(GameDao.class);

    private final FileStorageSystem fileSystem = AppContext.getBean(FileStorageSystem.class);

    private final MailSystem mailSystem = AppContext.getBean(MailSystem.class);

    public UserController(Router router, SessionStore sessionHandle) {
        router.post("/api/user/info").handler(sessionHandle::handle).handler(this::info);
        router.post("/api/user/profile/:username").handler(sessionHandle::handle).handler(this::profile);
        router.post("/api/user/history").handler(sessionHandle::handle).handler(this::history);

        router.post("/api/account/edit").handler(sessionHandle::handle).handler(this::updateInfo);
        router.post("/api/account/password").handler(sessionHandle::handle).handler(this::updatePassword);
        router.post("/api/account/send/code").handler(sessionHandle::handle).handler(this::sendUpdateEmailCode);
        router.post("/api/account/email").handler(sessionHandle::handle).handler(this::updateEmail);
    }

    /**
     * 自己的用户资料
     */
    public void info(RoutingContext ctx) {
        userDao.findOneById(SessionStore.uid(ctx), MongoStream.fields("username", "nickname", "avatar", "email", "status", "rating"))
                .onSuccess(user -> RestContext.success(ctx, user))
                .onFailure(e -> {
                    log.error(e.getMessage(), e.getCause());
                    RestContext.fail(ctx);
                });
    }

    /**
     * 个人资料统计数据
     */
    public void profile(RoutingContext ctx) {
        String username = ctx.pathParam("username");
        if (!RegexUtils.matches(username, RegexUtils.USERNAME)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        userDao.findOneByUsername(username, MongoStream.fields("nickname", "avatar", "status", "rating")).onSuccess(user -> {
            if (null == user) {
                RestContext.fail(ctx, HttpResponseStatus.NOT_FOUND);
                return;
            }
            user.put("username", username);
            // TODO 场次
            user.put("count", 20);
            // 胜率
            user.put("rate", 50.1);
            // 游戏时长 20小时
            user.put("time", 20);
            RestContext.success(ctx, user);
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }

    /**
     * 查询用户的历史对局
     */
    public void history(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String username = body.getString("username");
        Integer page = body.getInteger("page");
        if (!RegexUtils.matches(username, RegexUtils.USERNAME)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (null == page || page < 1) {
            page = 1;
        }

        int limit = 10;

        JsonObject query = JsonObject.of("$or", new JsonArray()
                .add(JsonObject.of("white", username))
                .add(JsonObject.of("black", username)));

        FindOptions opts = SqlUtils.findOpts(0,"board", "steps");

        opts.setSort(JsonObject.of("startTime", -1));
        opts.setSkip((page - 1) * limit);
        opts.setLimit(limit);

        gameDao.find(query, opts).compose(games -> {
            // 查询对局中用户的用户头像昵称
            JsonArray usernames = new JsonArray();
            for (JsonObject game : games) {
                String white = game.getString("white");
                if (!usernames.contains(white)) {
                    usernames.add(white);
                }
                String black = game.getString("black");
                if (!usernames.contains(black)) {
                    usernames.add(black);
                }
            }
            return userDao.findAll(JsonObject.of("username", JsonObject.of("$in", usernames)), "username", "nickname", "rating").compose(users -> {
                Map<String, JsonObject> userMap = new HashMap<>();
                for (JsonObject user : users) {
                    userMap.put(user.getString("username"), user);
                }
                for (JsonObject game : games) {
                    game.put("white", userMap.get(game.getString("white")));
                    game.put("black", userMap.get(game.getString("black")));
                }
                return Future.succeededFuture(games);
            });
        }).onSuccess(games -> {
            RestContext.success(ctx, games);
        }).onFailure(e -> {
            log.error(e.getMessage(), e.getCause());
            RestContext.fail(ctx);
        });
    }

    /**
     * 修改个人信息 昵称、头像、
     */
    public void updateInfo(RoutingContext ctx) {
        String nickname = ctx.request().getParam("nickname");
        if (StringUtils.isBlank(nickname) || nickname.contains(" ")) {
            // 不能有空格
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (!ctx.fileUploads().isEmpty()) {
            FileUpload file = ctx.fileUploads().iterator().next();
            String contentType = file.contentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                // 不是图片类型
                RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            if (file.size() > 1024 * 200) {
                // 大于200KB
                RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            BufferedImage image;
            try {
                image = ImageIO.read(new File(file.uploadedFileName()));
            } catch (IOException e) {
                RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            if (image == null) {
                RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            if (image.getWidth() != image.getHeight()) {
                // 不是正方形图片
                RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            String username = SessionStore.username(ctx);

            fileSystem.upload(UploadBucket.avatar, file)
                    .compose(filename -> userDao.findOneByUsername(username, MongoStream.fields("avatar")).compose(u -> {
                        String avatar = u.getString("avatar");
                        if (StringUtils.isNotBlank(avatar)) {
                            fileSystem.delete(UploadBucket.avatar, avatar);
                        }
                        return Future.succeededFuture(filename);
                    }))
                    .compose(filename -> userDao.updateProfile(username, filename, nickname))
                    .onSuccess(res -> RestContext.success(ctx))
                    .onFailure(e -> {
                        log.error(e.getMessage(), e.getCause());
                        RestContext.fail(ctx);
                    });
        } else {
            // 只修改昵称
            userDao.updateProfile(SessionStore.username(ctx), null, nickname)
                    .onSuccess(res -> RestContext.success(ctx))
                    .onFailure(e -> {
                        log.error(e.getMessage(), e.getCause());
                        RestContext.fail(ctx);
                    });

        }

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

        userDao.findOneById(session.uid, MongoStream.fields("password")).onSuccess(user -> {
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
                AppContext.REDIS.set(Arrays.asList(RedisConstant.AUTH_CODE_EMAIL + email, code, "EX", RedisConstant.CODE_EXPIRE)).onSuccess(v -> {
                    RestContext.success(ctx);
                    mailSystem.send("修改邮箱验证码", email, code, false);
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
                !confirmPassword.equals(newPassword) || newPassword.equals(oldPassword)) {
            RestContext.fail(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        UserSession session = SessionStore.sessionUser(ctx);
        userDao.findOneById(session.uid,  MongoStream.fields("password")).onSuccess(user -> {
            if (!SecurityUtils.matchesBCrypt(oldPassword, user.getString("password"))) {
                RestContext.fail(ctx, RestStatus.EMAIL_CODE_IS_INVALID);
            } else {
                userDao.updatePassword(session.username, SecurityUtils.bCrypt(newPassword)).onSuccess(username -> {
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
