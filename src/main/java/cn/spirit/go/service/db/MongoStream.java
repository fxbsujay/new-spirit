package cn.spirit.go.service.db;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;

public class MongoStream {


    public MongoStream(String url, String db) {
        MongoClient mongoClient = MongoClients.create(url);

        MongoDatabase database = mongoClient.getDatabase(db);

    }
}
