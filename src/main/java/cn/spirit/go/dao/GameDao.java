package cn.spirit.go.dao;

import cn.spirit.go.common.util.SqlUtils;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import java.util.List;

public class GameDao {
    private final MongoClient client;

    public GameDao(MongoClient client) {
        this.client = client;
    }

    public Future<JsonObject> findOne(JsonObject query, String ...fields) {
        return client.findOne("game", query, SqlUtils.fields(fields));
    }

    public Future<List<JsonObject>> findAll(JsonObject query, String ...fields) {
        return client.findWithOptions("game", query, SqlUtils.findOpts(fields));
    }

    public Future<List<JsonObject>> find(JsonObject query, FindOptions options) {
        return client.findWithOptions("game", query, options);
    }

    public Future<String> save(JsonObject obj) {
        return client.save("game", obj);
    }

}
