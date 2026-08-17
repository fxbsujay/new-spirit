package cn.spirit.go.web.config;

import io.vertx.core.json.Json;

public class Config {

    public Server server = new Server();

    public Mongodb mongodb = new Mongodb();

    public Redis redis = new Redis();

    public Mail mail;

    public static class Server {

        public Integer port = 8899;

        public Long bodyLimit = 10 * 1024 * 1024L;

        public String storageFilePath = "./static";
    }

    public static class Mongodb {

        public String url = "localhost:27017";

        public String db = "spirit";
    }


    public static class Redis {

        public String url = "localhost:6379";

        public String password = "";
    }

    public static class Mail {

        public String host;

        public Integer port;

        public String username;

        public String password;
    }

    @Override
    public String toString() {
        return Json.encode(this);
    }

}
