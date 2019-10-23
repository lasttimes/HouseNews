package com.lje.public_rental_house_news;

import com.avos.avoscloud.AVObject;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import okio.Buffer;
import okio.Okio;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class Utils {

    private static ObjectMapper mapper;

    private static OkHttpClient sClient;

    private static volatile List<PathInfo> PATH_INFO_LIST;

    private static final Object PATH_INFO_LIST_LOCK = new Object();

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
        if (PATH_INFO_LIST == null) {
            synchronized (PATH_INFO_LIST_LOCK) {
                if (PATH_INFO_LIST == null) {
                    File file = new File(Objects.requireNonNull(HouseNews.class.getClassLoader().getResource("path.json")).getFile());
                    try (InputStream in = new FileInputStream(file)) {
                        PATH_INFO_LIST = mapper.readValue(in, mapper.getTypeFactory().constructCollectionType(List.class, PathInfo.class));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return PATH_INFO_LIST;
    }

    /**
     * 发送邮件
     *
     * @param toAddress 接收方邮件地址
     * @param subject   邮件主题
     * @param content   　邮件内容(HTML格式)
     */
    public static void senHTMLdMail(String toAddress, String subject, String content) throws IOException, MessagingException {
        // 163 邮箱
        final Properties props = new Properties();
        loadProperties(props, "mail.properties");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        String username = props.getProperty("username");
                        String password = props.getProperty("password");
                        return new PasswordAuthentication(username, password);
                    }
                });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(props.getProperty("username")));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(toAddress));
        message.setSubject(MimeUtility.encodeText(subject,"UTF-8",null));
        message.setText(content, "UTF-8", "HTML");

        Transport.send(message);
    }

    public static String getHtmlBodyText(Logger logger, String url,String charsetName) {
        if (sClient == null) {
            sClient = new OkHttpClient();
        }
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = sClient.newCall(request).execute();
            if (response.body() == null) {
                return null;
            }
            String body;
            if (charsetName == null){
                body = response.body().string();
            }else{
                Charset charset = Charset.forName(charsetName);
                body = response.body().source().readString(charset);
            }
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
            addErrorLog("connect",url + " " + e.getMessage());
            return null;
        }
    }

    static void addErrorLog(String type, String message) {
        AVObject obj = new AVObject("ErrorLog");
        obj.put("type", type);
        obj.put("message", message);
        obj.saveInBackground();
    }
}
