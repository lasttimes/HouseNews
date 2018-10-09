package com.lje.public_rental_house_news;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineFunctionParam;
import com.avos.avoscloud.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test
 */
public class HouseNews {

    private static Logger logger = LogManager.getLogger();

    private static final OkHttpClient sClient = new OkHttpClient();

    private static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;


    private static final String COL_ORGANIZE_NAME = "organizeName";
    private static final String COL_NEWS_ID = "newsId";

    // 查找对应 html 中的 id ,对比保存的上一次最新id，
    // 如果没有保存记录，或者新id 大于保存id，返回对应 NewsInfo
    private static NewsInfo getLatestNewsInfo(PathInfo pathInfo, String lastId) {
        String htmlBody = getHtmlBodyText(pathInfo.url);
        if (htmlBody == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(pathInfo.regex);
        Matcher m = pattern.matcher(htmlBody);

        if (m.find()) {
            NewsInfo info = new NewsInfo();
            info.href = m.group(1);
            info.id = m.group(2);
            if (info.id == null) {
                return null;
            }
            if (lastId == null
                    || info.id.compareTo(lastId) > 0) {
                return info;
            }
        }
        return null;
    }

    private static String getHtmlBodyText(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = sClient.newCall(request).execute();
            return response.body() != null ? response.body().string() : null;
        } catch (Exception e) {
            logger.warn(" request " + url + " " + e.getMessage());
            return null;
        }
    }

    // 从配置文件读取上次成功更新时间，如是超出1小时，则再次请求更新
    @EngineFunction("checkLatestUpdateTime")
    public static void checkLatestUpdateTime() {
        List<PathInfo> pathInfoList = Utils.loadPathList();
        if (pathInfoList == null) {
            logger.error("checkLatestUpdateTime: pathInfoList null");
            return;
        }
        for (PathInfo pathInfo : pathInfoList) {
            // 查询最后更新时间
            AVQuery<AVObject> query = new AVQuery<>("LatestUpdate");
            query.whereEqualTo(COL_ORGANIZE_NAME, pathInfo.name);
            try {
                List<AVObject> list = query.find();
                LocalDateTime dateTime;
                if (list == null || list.size() == 0) {
                    dateTime = LocalDateTime.MIN;
                } else {
                    AVObject o = list.get(0);
                    Date d = o.getUpdatedAt();
                    dateTime = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
                }
                logger.info("checkLatestUpdateTime: " + pathInfo.name + " " + dateTime);
                if (dateTime.plusHours(1).isBefore(LocalDateTime.now())) {
                    Map<String, String> params = Collections.singletonMap("pathName", pathInfo.name);
                    try {
                        AVCloud.callFunction("getLatestNews", params);
                    } catch (AVException avE) {
                        avE.printStackTrace();
                    }
                }
            } catch (AVException e) {
                e.printStackTrace();
            }
        }
    }

    @EngineFunction("getLatestNews")
    public static void getLatestNews(@EngineFunctionParam("pathName") String pathName) {
        List<PathInfo> pathInfoList = Utils.loadPathList();
        if (pathInfoList == null) {
            logger.error("checkLatestUpdateTime: pathInfoList null");
            return;
        }
        PathInfo pathInfo = null;
        for (PathInfo info : pathInfoList) {
            if (info.name.equals(pathName)) {
                pathInfo = info;
                break;
            }
        }
        if (pathInfo == null) {
            logger.error("getLatestNews: pathName[%s] not found", pathName);
            return;
        }

        AVQuery<AVObject> query = new AVQuery<>("LatestUpdate");
        query.whereEqualTo(COL_ORGANIZE_NAME, pathInfo.name);
        try {
            List<AVObject> list = query.find();
            AVObject o;
            if (list == null || list.size() == 0) {
                o = new AVObject();
                o.put(COL_ORGANIZE_NAME, pathName);
            } else {
                o = list.get(0);
            }
            String id = o.getString(COL_NEWS_ID);
            NewsInfo newsInfo = getLatestNewsInfo(pathInfo, id);
            if (newsInfo == null) {
                return;
            }
            o.put(COL_NEWS_ID, newsInfo.id);
            o.saveInBackground();
            logger.info("newsInfo:" + newsInfo);
            AVPush push = new AVPush();
            String message = pathName + "有新的公告";
            push.setMessage(message);
            push.sendInBackground();
            logger.info("push message:" + message);

        } catch (AVException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
    }

    static class PathInfo {
        public PathInfo() {

        }

        @SuppressWarnings("WeakerAccess")
        public String name;
        @SuppressWarnings("WeakerAccess")
        public String url;
        @SuppressWarnings("WeakerAccess")
        public String regex;

        @Override
        public String toString() {
            return "PathInfo{" +
                    "name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    ", regex='" + regex + '\'' +
                    '}';
        }
    }

    static class NewsInfo implements Comparable<NewsInfo> {
        String id;
        String href;

        @Override
        public String toString() {
            return "NewsInfo{" +
                    "id='" + id + '\'' +
                    ", href='" + href + '\'' +
                    '}';
        }

        @Override
        public int compareTo(NewsInfo o) {
            return this.id.compareTo(o.id);
        }
    }
}
