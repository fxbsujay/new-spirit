package cn.spirit.go;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("User Api Test")
@ExtendWith(VertxExtension.class)
public class UserApiTest {

    private static final Logger log = LoggerFactory.getLogger(UserApiTest.class);

    public String username = "admin2";

    public String password = "admin2";

    public String token = null;

    public WebClient client;

    Future<String> getToken() {
        if (null == token) {
            return client.post(8899, "localhost", "/api/auth/signin")
                    .sendJson(JsonObject.of("username", username, "password", password)).map(resp -> resp.getHeader("set-cookie"));
        }
        return Future.succeededFuture(token);
    }

    @Test
    @DisplayName("Query Profile")
    void queryProfile(Vertx vertx, VertxTestContext testContext) {
        client = WebClient.create(vertx);
        vertx.deployVerticle(new Application()).onComplete(testContext.succeeding(id -> {
            getToken().onSuccess(token -> {

                Future<String> profile = client.post(8899, "localhost", "/api/user/profile/" + username)
                        .putHeader("Cookie", token)
                        .send()
                        .map(resp -> resp.bodyAsString("utf-8"));

                Future<String> map = client.post(8899, "localhost", "/api/user/history")
                        .putHeader("Cookie", token)
                        .sendJsonObject(JsonObject.of("username", username))
                        .map(resp -> resp.bodyAsString("utf-8"));

                Future.all(profile, map).onSuccess(result -> {
                    log.info("Profile Success: {}", result.resultAt(0).toString());
                    log.info("History Success: {}", result.resultAt(1).toString());
                    testContext.completeNow();
                });

            });
        }));
    }

    @Test
    @DisplayName("Update Info")
    void updateInfo(Vertx vertx, VertxTestContext testContext) {
        client = WebClient.create(vertx);
        vertx.deployVerticle(new Application()).onComplete(testContext.succeeding(id -> {
            getToken().onSuccess(token -> vertx.fileSystem().readFile("upload.jpg").onSuccess(img -> {
                client.post(8899, "localhost", "/api/account/edit")
                        .putHeader("Cookie", token)
                        .sendMultipartForm(MultipartForm.create()
                                .attribute("nickname", "test")
                                .binaryFileUpload("image", "upload.jpg", img, "image/jpg")
                        )
                        .onSuccess(res -> {
                            log.info("edit success, res = {}", res.statusCode());
                            testContext.completeNow();
                        });
            }));
        }));
    }

}
