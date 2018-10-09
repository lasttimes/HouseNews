package com.lje.public_rental_house_news;

import cn.leancloud.EngineFunction;
import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVPush;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test
 */
public class HouseNews {

    private static Logger logger = LogManager.getLogger(HouseNews.class);

    private static final OkHttpClient sClient = new OkHttpClient();


    // 查找对应 html 中的 id ,对比保存的上一次最新id，
    // 如果没有保存记录，或者新id 大于保存id，返回对应 NewsInfo
    private static NewsInfo getLatestNewsInfo(PathInfo pathInfo, Properties props) {
        String htmlBody = getHtmlBodyText(pathInfo.url);
        if (htmlBody == null) {
            return null;
        }
        String lastId = props.getProperty(pathInfo.name);
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
                logger.info("info:" + info);
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
            logger.error(" request " + url + " " + e.getMessage());
            return null;
        }
    }

    @EngineFunction("pushLatestHouseNews")
    public static void pushLatestHouseNews() {
        List<PathInfo> pathInfoList = Utils.loadPathList();
        if (pathInfoList == null) {
            return;
        }
        Properties props = Utils.loadProp();
        List<PathInfo> needPushNewsInfoList = new ArrayList<>();
        for (PathInfo pathInfo : pathInfoList) {
            NewsInfo newsInfo = getLatestNewsInfo(pathInfo, props);
            if (newsInfo != null) {
                props.setProperty(pathInfo.name,newsInfo.id);
                needPushNewsInfoList.add(pathInfo);
            }
        }
        if (needPushNewsInfoList.size() > 0) {
            Utils.saveProp(props);
        }
        logger.info("needPushNewsInfoList:" + needPushNewsInfoList);
        if (needPushNewsInfoList.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("有新公告：");
            for (int i = 0; i < needPushNewsInfoList.size(); i++) {
                if (i != 0) {
                    sb.append(",");
                }
                sb.append(needPushNewsInfoList.get(i).name);
            }
            AVPush push = new AVPush();
            push.setMessage(sb.toString());
            logger.info("push message:" + sb.toString());
            push.sendInBackground();
        }
    }

    @EngineFunction("getLatestHouseNews")
    public static List<String> getLatestHouseNews() {
        List<PathInfo> pathInfoList = Utils.loadPathList();
        if (pathInfoList == null) {
            return null;
        }
        Properties props = Utils.loadProp();
        List<String> latestNames = new ArrayList<>();
        for (PathInfo pathInfo : pathInfoList) {
            NewsInfo newsInfo = getLatestNewsInfo(pathInfo, props);
            if (newsInfo != null) {
                latestNames.add(pathInfo.name);
            }
        }
        logger.info("getLatestHouseNews return :" + latestNames);
        return latestNames;
    }

    public static void main(String... args) {
        pushLatestHouseNews();
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
        String title;
        String date;
        String href;

        @Override
        public String toString() {
            return "NewsInfo{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", date='" + date + '\'' +
                    ", href='" + href + '\'' +
                    '}';
        }

        @Override
        public int compareTo(NewsInfo o) {
            return this.id.compareTo(o.id);
        }
    }
}
