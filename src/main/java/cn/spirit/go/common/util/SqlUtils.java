package cn.spirit.go.common.util;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;

public class SqlUtils {

    public static FindOptions findOpts(String ...fields) {
        return new FindOptions().setFields(fields(fields));
    }

    public static JsonObject fields(String ...fields) {
        JsonObject obj = JsonObject.of("_id", 0);
        for (String field : fields) {
            obj.put(field, 1);
        }
        return obj;
    }
}
