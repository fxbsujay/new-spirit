package cn.spirit.go;

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

    @Test
    @DisplayName("Update Info")
    void sampleServer(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        vertx.deployVerticle(new Application()).onComplete(testContext.succeeding(id -> {
            client.post(8899, "localhost", "/api/auth/signin")
                    .sendJson(JsonObject.of("username", username, "password", password)).onSuccess(resp -> {
                                log.info("Sign in success, {}", resp.getHeader("set-cookie"));
                                vertx.fileSystem().readFile("upload.jpg").onSuccess(img -> {
                                    client.post(8899, "localhost", "/api/account/edit")
                                            .putHeader("Cookie", resp.getHeader("set-cookie"))
                                            .sendMultipartForm(MultipartForm.create()
                                                    .attribute("nickname", "test")
                                                    .binaryFileUpload("image", "upload.jpg", img, "image/jpg")
                                            )
                                            .onSuccess(res -> {
                                                log.info("edit success, res = {}", res.statusCode());
                                                testContext.completeNow();
                                            });
                                });
                    });
        }));
    }
}
