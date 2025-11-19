package cn.spirit.go.dao;

import cn.spirit.go.common.util.SqlUtils;
import cn.spirit.go.web.config.AppContext;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class GameDao {

    public Future<JsonObject> findOne(JsonObject query, String ...fields) {
        return AppContext.MONGO.findOne("game", query, SqlUtils.fields(fields));
    }

    public Future<String> insert(JsonObject obj) {
        obj.put("createdAt", System.currentTimeMillis());
        return AppContext.MONGO.save("game", obj);
    }

}
