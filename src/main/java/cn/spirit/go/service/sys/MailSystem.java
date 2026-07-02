package cn.spirit.go.service.sys;

import cn.spirit.go.web.config.Config;
import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailSystem {

    private static final Logger log = LoggerFactory.getLogger(MailSystem.class);

    public final MailClient client;

    public final String username;

    public MailSystem(Vertx vertx, Config.Mail config) {
        MailConfig mailConfig = new MailConfig()
                .setHostname(config.host)
                .setPort(config.port)
                .setSsl(true)
                .setStarttls(StartTLSOptions.REQUIRED)
                .setUsername(config.username)
                .setPassword(config.password);
        this.client = MailClient.createShared(vertx, mailConfig);
        this.username = config.username;
    }

    public void send(String subject, String to, String content, boolean html) {
        MailMessage message = new MailMessage()
                .setFrom(username + " (Spirit Go)")
                .setTo(to)
                .setSubject(subject);
        if (html) {
            message.setHtml(content);
        } else {
            message.setText(content);
        }
        log.info("Send email subject: {}, to: {}, content: {}", subject, to, content);
        client.sendMail(message);
    }
}
