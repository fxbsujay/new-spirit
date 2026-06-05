package cn.spirit.go.dao;

import cn.spirit.go.common.util.SqlUtils;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;

import java.util.List;

public class GameDao {

    public Future<JsonObject> findOne(JsonObject query, String ...fields) {
        return AppContext.MONGO.findOne("game", query, SqlUtils.fields(fields));
    }

    public Future<List<JsonObject>> findAll(JsonObject query, String ...fields) {
        return AppContext.MONGO.findWithOptions("game", query, SqlUtils.findOpts(fields));
    }

    public Future<List<JsonObject>> find(JsonObject query, FindOptions options) {
        return AppContext.MONGO.findWithOptions("game", query, options);
    }

    public Future<String> save(JsonObject obj) {
        return AppContext.MONGO.save("game", obj);
    }

}
