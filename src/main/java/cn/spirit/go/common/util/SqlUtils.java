package cn.spirit.go.common.util;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;

public class SqlUtils {

    public static FindOptions findFields(String ...fields) {
        JsonObject obj = JsonObject.of("_id", 0);
        for (String field : fields) {
            obj.put(field, 1);
        }
        return new FindOptions().setFields(obj);
    }
}
