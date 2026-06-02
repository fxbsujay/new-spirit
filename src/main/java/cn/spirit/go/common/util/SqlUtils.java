package cn.spirit.go.common.util;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;

public class SqlUtils {

    public static FindOptions findOpts(String ...fields) {
        return findOpts(1, fields);
    }

    public static FindOptions findOpts(int query, String ...fields) {
        return new FindOptions().setFields(fields(query, fields));
    }

    public static JsonObject fields(String ...fields) {
        return fields(1, fields);
    }

    public static JsonObject fields(int query, String ...fields) {
        JsonObject obj = JsonObject.of("_id", 0);
        for (String field : fields) {
            obj.put(field, query);
        }
        return obj;
    }
}
