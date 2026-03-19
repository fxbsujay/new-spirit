package cn.spirit.go.common.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class KataGoUtils {

    private static final String KATAGO_PATH = "F:\\katago\\katago-v1.16.4-eigen-windows-x64\\katago.exe";

    private static final String CONFIG_PATH = "F:\\katago\\katago-v1.16.4-eigen-windows-x64\\analysis_example.cfg";

    private static final String MODEL_PATH = "F:\\katago\\katago-v1.16.4-eigen-windows-x64\\mode.bin.gz";

    private static final Logger log = LoggerFactory.getLogger(KataGoUtils.class);


    public static void main(String[] args) throws IOException {
        List<String> cmd = new ArrayList<>();
        cmd.add(KATAGO_PATH);
        cmd.add("analysis");
        cmd.add("-config");
        cmd.add(CONFIG_PATH);
        cmd.add("-model");
        cmd.add(MODEL_PATH);

        // 启动进程
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process kataGoProcess = pb.start();

        // 创建输入输出流
        BufferedWriter kataGoWriter = new BufferedWriter(
                new OutputStreamWriter(kataGoProcess.getOutputStream(), StandardCharsets.UTF_8)
        );
        BufferedReader kataGoReader = new BufferedReader(
                new InputStreamReader(kataGoProcess.getInputStream(), StandardCharsets.UTF_8)
        );

        String json = JsonObject.of(
                "id", "1",
                "rules", "tromp-taylor",
                "komi", 7.5,
                "boardXSize", 19,
                "boardYSize", 19,
                "moves", JsonArray.of(JsonArray.of("B", "P5"), JsonArray.of("W", "P6"))
        ).encode();

        log.info("query json: {}",json);
        kataGoWriter.write(json + "\n");
        kataGoWriter.flush();

        String responseLine = kataGoReader.readLine();
        if (responseLine == null) {
            throw new IOException("KataGo 无响应，进程可能已退出");
        }

        log.info("responseLine: {}",responseLine);

    }
}
