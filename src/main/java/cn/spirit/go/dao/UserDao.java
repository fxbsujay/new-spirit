package cn.spirit.go.dao;

import cn.spirit.go.common.enums.UserStatus;
import cn.spirit.go.web.config.AppContext;
import cn.spirit.go.model.entity.UserEntity;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

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
            obj.put("createdAt", entity.createdAt);
        }
        return obj;
    }

    private UserEntity mapping(JsonObject obj) {
        if (null == obj) {
            return null;
        }
        UserEntity entity = new UserEntity();
        entity._id = obj.getString("_id");
        entity.username = obj.getString("username");
        entity.password = obj.getString("password");
        entity.nickname = obj.getString("nickname");
        entity.avatar = obj.getString("avatar");
        entity.email = obj.getString("email");
        entity.status = UserStatus.valueOf(obj.getString("status"));
        entity.createdAt = obj.getLong("createdAt");
        return entity;
    }

    public Future<String> insert(UserEntity entity) {
        entity.createdAt = System.currentTimeMillis();
        return AppContext.MONGO.save("user", mapping(entity));
    }

    public Future<UserEntity> selectByUsername(String username) {
        return AppContext.MONGO.findOne("user", JsonObject.of("username", username), JsonObject.of()).compose(res -> Future.succeededFuture(mapping(res)));
    }

    public Future<UserEntity> selectByEmail(String email) {
        return AppContext.MONGO.findOne("user", JsonObject.of("email", email), JsonObject.of()).compose(res -> Future.succeededFuture(mapping(res)));
    }

    public Future<String> updatePassword(String username, String password) {
        return AppContext.MONGO.updateCollection("user", JsonObject.of("username", username), JsonObject.of("$set", JsonObject.of("password", password)))
                .compose(res -> Future.succeededFuture(username));
    }

    public Future<UserEntity> selectByUsernameOrEmail(String username, String email) {
        JsonObject query = JsonObject.of("$or", new JsonArray()
                        .add(JsonObject.of("username", username))
                        .add(JsonObject.of("email", email)));
        return AppContext.MONGO.findOne("user", query, JsonObject.of()).compose(res -> Future.succeededFuture(mapping(res)));
    }

}
