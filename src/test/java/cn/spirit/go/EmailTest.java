package cn.spirit.go;

import cn.spirit.go.service.sys.MailSystem;
import cn.spirit.go.web.config.Config;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@DisplayName("Email Test")
@ExtendWith(VertxExtension.class)
public class EmailTest {

    @Test
    @DisplayName("Send")
    void queryProfile(Vertx vertx, VertxTestContext testContext) {
        vertx.fileSystem().readFile("config.json").onSuccess(buffer -> {
            MailSystem system = new MailSystem(vertx, Json.decodeValue(buffer, Config.class).mail);

            system.send("测试", "2693376843@qq.com", "Test", false).onSuccess(result -> {
                testContext.completeNow();
            });
        });

    }
}
