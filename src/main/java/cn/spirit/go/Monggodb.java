package cn.spirit.go;

import cn.spirit.go.common.enums.UserStatus;
import cn.spirit.go.model.entity.UserEntity;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.launcher.application.VertxApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Monggodb extends VerticleBase {


    private static final Logger log = LoggerFactory.getLogger(Monggodb.class);

    @Override
    public Future<?> start() throws Exception {

        JsonObject mongoconfig = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "test");
        MongoClient mongoClient = MongoClient.createShared(vertx, mongoconfig);


        UserEntity entity = new UserEntity();
        entity.username = "root";
        entity.password = "F521.wojiaofxb";
        entity.nickname = "nickname";
        entity.avatar = "golang";
        entity.email = "email";
        entity.status = UserStatus.NORMAL;


        long st = System.currentTimeMillis();
        JsonObject entries = new JsonObject();
        if (entity.username != null) {
            entries.put("username", entity.username);
        }
        if (entity.password != null) {
            entries.put("password", entity.password);
        }
        if (entity.nickname != null) {
            entries.put("nickname", entity.nickname);
        }
        if (entity.avatar != null) {
            entries.put("avatar", entity.avatar);
        }
        if (entity.email != null) {
            entries.put("email", entity.email);
        }
        if (entity.status != null) {
            entries.put("status", entity.status);
        }


        log.info("time = {}", System.currentTimeMillis() - st);
        return mongoClient.save("products", entries)
                .compose(id -> {
                    System.out.println("Inserted id: " + id);
                    log.info("time = {}", System.currentTimeMillis() - st);
                    return mongoClient.find("products", new JsonObject().put("itemId", "12345"));
                }).compose(r -> {
                    log.info("time = {}", System.currentTimeMillis() - st);
                    return  mongoClient.save("products", entries);
                })
                .onSuccess(res -> {
                    log.info("time = {}", System.currentTimeMillis() - st);
                    System.out.println("Product removed ");
                });
    }

    public static void main(String[] args) {
        VertxApplication.main(new String[]{Monggodb.class.getName()});
    }
}
