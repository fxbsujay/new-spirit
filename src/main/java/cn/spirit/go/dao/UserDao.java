package cn.spirit.go.dao;

import cn.spirit.go.common.util.SqlUtils;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.BulkOperation;
import io.vertx.ext.mongo.BulkOperationType;

import java.util.Arrays;
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

    public Future<List<JsonObject>> updateRating(String user1, Integer rating1, String user2, Integer rating2) {
        BulkOperation opt1 = new BulkOperation(JsonObject.of(
                "type", BulkOperationType.UPDATE,
                "filter", JsonObject.of("username", user1),
                "document", JsonObject.of("$inc", JsonObject.of("rating", rating1))));
        BulkOperation opt2 = new BulkOperation(JsonObject.of(
                "type", BulkOperationType.UPDATE,
                "filter", JsonObject.of("username", user2),
                "document", JsonObject.of("$inc", JsonObject.of("rating", rating2))));

        return AppContext.MONGO.bulkWrite("game", Arrays.asList(opt1, opt2)).compose(res -> Future.succeededFuture(res.getUpserts()));
    }
}
