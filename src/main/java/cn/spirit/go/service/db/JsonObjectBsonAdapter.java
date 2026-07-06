package cn.spirit.go.service.db;

import io.vertx.core.json.JsonObject;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

public class JsonObjectBsonAdapter implements Bson {

    private final JsonObject obj;

    public JsonObjectBsonAdapter(JsonObject obj) {
        this.obj = obj;
    }

    public <C> BsonDocument toBsonDocument(Class<C> documentClass, CodecRegistry codecRegistry) {

        return new BsonDocumentWrapper<>(this.obj, codecRegistry.get(JsonObject.class));
    }
}
