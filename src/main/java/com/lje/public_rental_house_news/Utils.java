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

public class Utils {

    private static ObjectMapper mapper;

    private static OkHttpClient sClient;

    static {
        JsonFactory jf = new JsonFactory();
        jf.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper = new ObjectMapper(jf);
    }

    public static List<PathInfo> loadPathList() {
        try{
            File file = new File(Objects.requireNonNull(HouseNews.class.getClassLoader().getResource("path.json")).getFile());
            InputStream in = new FileInputStream(file);
            return mapper.readValue(in,mapper.getTypeFactory().constructCollectionType(List.class, PathInfo.class));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
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
