package cn.spirit.go.dao;

import cn.spirit.go.common.enums.UserStatus;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.model.entity.UserEntity;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserDao {

    private JsonObject mapping(UserEntity entity) {
        JsonObject obj = JsonObject.of();
        if (entity.username != null) {
            obj.put("username", entity.username);
        }
        if (entity.password != null) {
            obj.put("password", entity.password);
        }
        if (entity.nickname != null) {
            obj.put("nickname", entity.nickname);
        }
        if (entity.avatar != null) {
            obj.put("avatar", entity.avatar);
        }
        if (entity.email != null) {
            obj.put("email", entity.email);
        }
        if (entity.status != null) {
            obj.put("status", entity.status);
        }
        if (entity.createdAt != null) {
            obj.put("createdAt", entity.createdAt.format(DateTimeFormatter.ISO_DATE_TIME));
        }
        return obj;
    }

    private UserEntity mapping(JsonObject obj) {
        UserEntity entity = new UserEntity();
        entity.username = obj.getString("username");
        entity.password = obj.getString("password");
        entity.nickname = obj.getString("nickname");
        entity.avatar = obj.getString("avatar");
        entity.email = obj.getString("email");
        entity.status = UserStatus.valueOf(obj.getString("status"));
        entity.createdAt = LocalDateTime.parse(obj.getString("createdAt"), DateTimeFormatter.ISO_DATE_TIME);
        return entity;
    }

    public Future<String> insert(UserEntity entity) {
        entity.createdAt = LocalDateTime.now();
        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        return AppContext.MONGO.save("user", JsonObject.mapFrom(entity)).compose(id -> Future.succeededFuture(entity.username));
    }

    public Future<UserEntity> selectByUsername(String username) {
        return AppContext.MONGO.findOne("user", JsonObject.of("username", username), JsonObject.of()).compose(res -> {
            if (res == null) {
                return Future.succeededFuture(null);
            } else {
                return Future.succeededFuture(res.mapTo(UserEntity.class));
            }
        });
    }

    public Future<UserEntity> selectByEmail(String email) {
        return AppContext.MONGO.findOne("user", JsonObject.of("email", email), JsonObject.of()).compose(res -> {
            if (res == null) {
                return Future.succeededFuture(null);
            } else {
                return Future.succeededFuture(res.mapTo(UserEntity.class));
            }
        });
    }

    public Future<String> updatePassword(String username, String password) {
        return AppContext.MONGO.updateCollection("user", JsonObject.of("username", username), JsonObject.of("$set", JsonObject.of("password", password)))
                .compose(res -> Future.succeededFuture(username));
    }

    public Future<UserEntity> selectByUsernameOrEmail(String username, String email) {
        JsonObject query = new JsonObject()
                .put("$or", new JsonArray()
                        .add(new JsonObject().put("username", username))
                        .add(new JsonObject().put("email", email)));
        return AppContext.MONGO.findOne("user", query, JsonObject.of()).compose(res -> {
            if (res == null) {
                return Future.succeededFuture(null);
            } else {
                return Future.succeededFuture(res.mapTo(UserEntity.class));
            }
        });
    }

}
