package cn.spirit.go.dao;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.service.db.MongoStream;
import com.mongodb.client.model.Filters;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.BulkOperation;
import io.vertx.ext.mongo.BulkOperationType;
import java.util.List;

public class UserDao {

    private final MongoStream client;

    public UserDao(MongoStream client) {
        this.client = client;
    }

    public Future<String> insert(JsonObject obj) {
        return client.insertOne("user", obj);
    }

    public Future<JsonObject> findOneById(String uid, JsonObject fields) {
        return client.findOne("user", Filters.eq(MongoStream.ID_KEY, uid), fields);
    }

    public Future<JsonObject> findOneByUsernameOrEmail(String username, String email, JsonObject fields) {
        return client.findOne("user", Filters.or(Filters.eq("username", username), Filters.eq("email", email)), fields);
    }

    public Future<JsonObject> findOneByUsername(String username, JsonObject fields) {
        return client.findOne("user", Filters.eq("username", username), fields);
    }

    public Future<JsonObject> findOneByEmail(String email, JsonObject fields) {
        return client.findOne("user", Filters.eq("email", email), fields);
    }

    public Future<Long> findCount(JsonObject query) {
        return null;
    }

    public Future<List<JsonObject>> findAll(JsonObject query, String ...fields) {
        return null;
    }

    public Future<String> updatePassword(String username, String password) {
        return null;
    }

    public Future<String> updateEmail(String username, String email) {
        return null;
    }

    public Future<String> updateProfile(String username, String avatar, String nickname) {
        JsonObject entries = new JsonObject();
        if (StringUtils.isNotBlank(avatar)) {
            entries.put("avatar", avatar);
        }
        if (StringUtils.isNotBlank(nickname)) {
            entries.put("nickname", nickname);
        }
        if (entries.isEmpty()) {
            return Future.failedFuture("nothing to update");
        }
        return null;
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

        return null;
    }
}
