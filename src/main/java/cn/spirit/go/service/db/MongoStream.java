package cn.spirit.go.service.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;
import cn.spirit.go.service.db.codes.JsonObjectCodec;
import org.bson.Document;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;
import java.util.List;

public class MongoStream {

    public static final CodecRegistry commonCodecRegistry = CodecRegistries.fromCodecs(new StringCodec(), new IntegerCodec(), new BooleanCodec(), new DoubleCodec(), new LongCodec(), new BsonDocumentCodec(), new DocumentCodec(), new JsonObjectCodec(JsonObject.of()));

    private final MongoDatabase database;

    private final MongoClient client;

    public VertxInternal vertx;

    public static final String ID_KEY = "_id";

    public MongoStream(Vertx vertx, String url, String db) {
        this.vertx = (VertxInternal)vertx;
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(url))
                .codecRegistry(commonCodecRegistry)
                .build();
        try {
            client = MongoClients.create(settings);
            this.database = client.getDatabase(db);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public MongoDatabase db() {
        return database;
    }

    public MongoClient client() {
        return client;
    }

    public MongoCollection<JsonObject> getCollection(String name) {
        return this.database.getCollection(name, JsonObject.class);
    }

    /**
     * 查询单条文档
     *
     * @param query   查询条件
     * @param fields  返回字段
     */
    public Future<JsonObject> findOne(String collection, Bson query, JsonObject fields) {
        return promiseOne(getCollection(collection).find(query).projection(wrap(fields)).first());
    }

    /**
     * 查询多条
     *
     * @param query     查询条件
     * @param fields    返回字段
     */
    public Future<List<JsonObject>> findAll(String collection, Bson query, JsonObject fields) {
        return promiseMany(getCollection(collection).find(query).projection(wrap(fields)));
    }

    /**
     * 分页查询
     *
     * @param query     查询条件
     * @param fields    返回字段
     * @param page      页码
     * @param size      每页条数
     */
    public Future<List<JsonObject>> findPage(String collection, Bson query, JsonObject fields, int page, int size) {
        return promiseMany(getCollection(collection).find(query).skip(page * size).limit(size).projection(wrap(fields)));
    }

    /**
     * 新增
     * @return  新增的ID
     */
    public Future<String> insertOne(String collection, JsonObject document) {
        return promiseOne(getCollection(collection).insertOne(document)).map((v) -> v.getInsertedId().asObjectId().toString());
    }

    /**
     * 修改
     * @return  修改成功的文档数
     */
    public Future<Long> updateOne(String collection, Bson query, JsonObject obj) {
        return promiseOne(getCollection(collection).updateOne(query, wrap(obj))).map(UpdateResult::getModifiedCount);
    }

    public <T> Future<T> promiseOne(Publisher<T> publisher) {
        SingleSubscriber<T> subscriber = new SingleSubscriber<>(vertx.promise());
        publisher.subscribe(subscriber);
        return subscriber.promise().future();
    }

    public <T> Future<List<T>> promiseMany(Publisher<T> publisher) {
        ManySubscriber<T> subscriber = new ManySubscriber<>(vertx.promise());
        publisher.subscribe(subscriber);
        return subscriber.promise().future();
    }

    public static JsonObjectBsonAdapter wrap(JsonObject jsonObject) {
        return new JsonObjectBsonAdapter(jsonObject);
    }

    public static JsonObject fields(String ...fields) {
        return fields(true, fields);
    }

    public static JsonObject fields(Boolean excludeId, String ...fields) {
        JsonObject query = new JsonObject();
        for (String field : fields) {
            query.put(field, 1);
        }
        if (excludeId) {
            query.put(MongoStream.ID_KEY, 0);
        }
        return query;
    }
}
