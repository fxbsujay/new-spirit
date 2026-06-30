package cn.spirit.go.web.config;

import io.vertx.core.json.Json;

public class Config {

    public Server server;

    public Mongodb mongodb;

    public Redis redis;

    public Mail mail;

    public static class Server {

        public Integer port = 8899;

        public Long bodyLimit = 10 * 1024 * 1024L;

        public String storageFilePath = "./static";
    }

    public static class Mongodb {

        public String url;

        public String db;
    }


    public static class Redis {

        public String url;

        public String password;
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
