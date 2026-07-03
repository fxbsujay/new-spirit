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

    public Config defaultConfig() {
        Config config = new  Config();
        config.server = new Config.Server();
        config.mongodb = new Config.Mongodb();
        config.mongodb.url = "localhost:27017";
        config.mongodb.db = "spirit";
        config.redis = new Config.Redis();
        config.redis.url = "localhost:6379";
        config.redis.password = "MyRedis123";
        config.mail = new Config.Mail();
        config.mail.host =  "mtp.163.com";
        config.mail.port = 465;
        config.mail.username = "fsusured@163.com";
        config.mail.password = "JDUXN3hwa4GDLywg";
        return config;
    }
}
