package com.lje.public_rental_house_news;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

class Utils {

    private static ObjectMapper mapper;

    static {
        JsonFactory jf = new JsonFactory();
        jf.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper = new ObjectMapper(jf);
    }

    private static final String PROPS_FILE_NAME = "houseNews.props";

    static void saveProp(Properties props){
        try (OutputStream out = new FileOutputStream(PROPS_FILE_NAME)) {
            props.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Properties loadProp() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(PROPS_FILE_NAME)) {
            props.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return props;
    }

    static List<HouseNews.PathInfo> loadPathList() {
        try{
            File file = new File(Objects.requireNonNull(HouseNews.class.getClassLoader().getResource("path.json")).getFile());
            InputStream in = new FileInputStream(file);
            return mapper.readValue(in,mapper.getTypeFactory().constructCollectionType(List.class, HouseNews.PathInfo.class));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
