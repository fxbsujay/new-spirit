package cn.spirit.go.dao;

import cn.spirit.go.common.util.SqlUtils;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.List;

public class UserDao {

    public Future<String> insert(JsonObject obj) {
        obj.put("createdAt", System.currentTimeMillis());
        return AppContext.MONGO.save("user", obj);
    }

    public Future<JsonObject> findOne(JsonObject query, String ...fields) {
        return AppContext.MONGO.findOne("user", query, SqlUtils.fields(fields));
    }

    public Future<Long> findCount(JsonObject query) {
        return AppContext.MONGO.count("user", query);
    }

    public Future<List<JsonObject>> findAll(JsonObject query, String ...fields) {
        return AppContext.MONGO.findWithOptions("user", query, SqlUtils.findOpts(fields));
    }

    public Future<String> updatePassword(String username, String password) {
        return AppContext.MONGO.updateCollection("user", JsonObject.of("username", username), JsonObject.of("$set", JsonObject.of("password", password)))
                .compose(res -> Future.succeededFuture(username));
    }

    public Future<String> updateEmail(String username, String email) {
        return AppContext.MONGO.updateCollection("user", JsonObject.of("username", username), JsonObject.of("$set", JsonObject.of("email", email)))
                .compose(res -> Future.succeededFuture(username));
    }

}
