package cn.spirit.go.dao;

import cn.spirit.go.service.db.MongoStream;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.bson.conversions.Bson;
import java.util.List;

public class GameDao {

    private final MongoStream client;

    public GameDao(MongoStream client) {
        this.client = client;
    }


    public Future<List<JsonObject>> findPage(Bson query, JsonObject fields, int page) {
        return client.findPage("game", query, fields, page, 10);
    }

    public Future<String> insert(JsonObject obj) {
        return client.insertOne("game", obj);
    }

}
