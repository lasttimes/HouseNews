package com.lje.public_rental_house_news;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.List;
import java.util.Objects;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Utils {

    private static ObjectMapper mapper;

    private static OkHttpClient sClient;

    static {
        JsonFactory jf = new JsonFactory();
        jf.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper = new ObjectMapper(jf);
    }

    @SuppressWarnings("SameParameterValue")
    static void loadProperties(Properties properties, String resourceFileName) throws IOException {
        File propsFile = new File(Objects.requireNonNull(HouseNews.class.getClassLoader().getResource(resourceFileName)).getFile());
        try (InputStream in = new FileInputStream(propsFile)) {
            properties.load(in);
        }
    }

    public static List<PathInfo> loadPathList() {
        File file = new File(Objects.requireNonNull(HouseNews.class.getClassLoader().getResource("path.json")).getFile());
        try (InputStream in = new FileInputStream(file)) {
            return mapper.readValue(in, mapper.getTypeFactory().constructCollectionType(List.class, PathInfo.class));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendMail(String toAddress, String subject, String content) throws IOException, MessagingException {
        // 163 邮箱
        final Properties props = new Properties();
        loadProperties(props,"mail.properties");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        String username = props.getProperty("username");
                        String password = props.getProperty("password");
                        return new PasswordAuthentication(username, password);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(props.getProperty("username")));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(toAddress));
        message.setSubject(subject);
        message.setText(content);

        Transport.send(message);

        System.out.println("Done");
    }

    public static String getHtmlBodyText(Logger logger, String url) {
        if (sClient == null) {
            sClient = new OkHttpClient();
        }
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = sClient.newCall(request).execute();
            String body = response.body() != null ? response.body().string() : null;
            if (logger != null) {
                if (body == null) {
                    logger.warn("request return empty:" + url);
                } else {
                    logger.info("request succeed:" + url);
                }
            }
            return body;
        } catch (Exception e) {
            if (logger != null) {
                logger.warn(e.getMessage() + " " + url);
            }
            return null;
        }
    }
}
