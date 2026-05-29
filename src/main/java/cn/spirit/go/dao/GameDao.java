package cn.spirit.go.dao;

import cn.spirit.go.common.util.SqlUtils;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.List;

public class GameDao {

    public Future<JsonObject> findOne(JsonObject query, String ...fields) {
        return AppContext.MONGO.findOne("game", query, SqlUtils.fields(fields));
    }

    public Future<List<JsonObject>> findAll(JsonObject query, String ...fields) {
        return AppContext.MONGO.findWithOptions("game", query, SqlUtils.findOpts(fields));
    }

    public Future<String> save(JsonObject obj) {
        obj.put("endTime", System.currentTimeMillis());
        return AppContext.MONGO.save("game", obj);
    }

}
