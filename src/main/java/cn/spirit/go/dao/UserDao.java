package cn.spirit.go.dao;

import cn.spirit.go.common.util.StringUtils;
import cn.spirit.go.service.db.MongoStream;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.Updates;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
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

    public Future<Boolean> existsEmail(String email) {
       return client.count("user", Filters.eq("email", email)).map(v -> v > 0);
    }

    public Future<List<JsonObject>> findAllByUids(Iterable<String> uids, JsonObject fields) {
        return client.findAll("user", Filters.in(MongoStream.ID_KEY, uids), fields);
    }

    public Future<Long> updatePassword(String uid, String password) {
        return client.updateOne("user", Filters.eq(MongoStream.ID_KEY, uid), JsonObject.of("password", password));
    }

    public Future<Long> updateEmail(String uid, String email) {
        return client.updateOne("user", Filters.eq(MongoStream.ID_KEY, uid), JsonObject.of("email", email));
    }

    public Future<Long> updateAvatarAndNickname(String uid, String avatar, String nickname) {
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
        return client.updateOne("user", Filters.eq(MongoStream.ID_KEY, uid), entries);
    }

    public Future<Integer> updateRating(String user1, Integer rating1, String user2, Integer rating2) {
        return client.promiseOne(client.db().getCollection("user").bulkWrite(
                List.of(
                        new UpdateManyModel<>(Filters.eq("username", user1), Updates.inc("rating", rating1)),
                        new UpdateManyModel<>(Filters.eq("username", user2), Updates.inc("rating", rating2))
                ))).map(BulkWriteResult::getModifiedCount);
    }
}
