package cn.spirit.go.common.util;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class KataGoUtils {

    private static final String KATAGO_PATH = "F:\\katago\\katago-v1.16.4-opencl-windows-x64\\katago.exe";

    private static final String CONFIG_PATH = "F:\\katago\\katago-v1.16.4-opencl-windows-x64\\analysis_example.cfg";

    private static final String MODEL_PATH = "F:\\katago\\mode-b18.bin.gz";

    private static final Logger log = LoggerFactory.getLogger(KataGoUtils.class);

    private BufferedWriter writer;

    private BufferedReader reader;

    public KataGoUtils() {
        List<String> cmd = new ArrayList<>();
        cmd.add(KATAGO_PATH);
        cmd.add("analysis");
        cmd.add("-config");
        cmd.add(CONFIG_PATH);
        cmd.add("-model");
        cmd.add(MODEL_PATH);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process kataGoProcess = null;
        try {
            kataGoProcess = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        writer = new BufferedWriter(
                new OutputStreamWriter(kataGoProcess.getOutputStream(), StandardCharsets.UTF_8)
        );
        reader = new BufferedReader(
                new InputStreamReader(kataGoProcess.getInputStream(), StandardCharsets.UTF_8)
        );
    }

    public Future<String> analysis(JsonObject json) {
        return Future.future(promise -> {
            try {
                writer.write(json.encode() + "\n");
                writer.flush();
            } catch (IOException e) {
                promise.fail(e);
            }
            try {
                promise.succeed(reader.readLine());
            } catch (IOException e) {
                promise.fail(e);
            }
        });
    }

    public static void main(String[] args) {
        KataGoUtils utils = new KataGoUtils();
        JsonObject json = JsonObject.of(
                "id", "1",
                "rules", "tromp-taylor",
                "komi", 7.5,
                "boardXSize", 19,
                "boardYSize", 19,
                "moves", JsonArray.of(JsonArray.of("B", "P5"), JsonArray.of("W", "P6"))
        );

        utils.analysis(json).onSuccess(result -> {
            log.info("analysis result: {}", result);
        });

    }
}
