package com.lje.public_rental_house_news;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineFunctionParam;
import com.avos.avoscloud.AVCloud;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVPush;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
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

    // 最后更新id
    private static final String LATEST_ID_PROPS_NAME = "houseNews.props";

    // 上次成功更新时间
    private static final String LATEST_SUCCESS_UPDATE_TIME_PROPS_NAME = "latestSuccessUpdateTime.props";


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

    @EngineFunction("pushLatestHouseNews")
    public static void pushLatestHouseNews() {
        List<PathInfo> pathInfoList = Utils.loadPathList();
        if (pathInfoList == null) {
            return;
        }
        Properties latestIdProps = Utils.loadProp(LATEST_ID_PROPS_NAME);
        List<PathInfo> needPushNewsInfoList = new ArrayList<>();
        for (PathInfo pathInfo : pathInfoList) {
            NewsInfo newsInfo = getLatestNewsInfo(pathInfo, latestIdProps);
            if (newsInfo != null) {
                latestIdProps.setProperty(pathInfo.name, newsInfo.id);
                needPushNewsInfoList.add(pathInfo);
            }
        }
        if (needPushNewsInfoList.size() > 0) {
            Utils.saveProp(latestIdProps, LATEST_ID_PROPS_NAME);
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

    // 从配置文件读取上次成功更新时间，如是超出1小时，则再次请求更新
    @EngineFunction("checkLatestUpdateTime")
    public static void checkLatestUpdateTime() {
        List<PathInfo> pathInfoList = Utils.loadPathList();
        if (pathInfoList == null) {
            logger.error("checkLatestUpdateTime: pathInfoList null");
            return;
        }
        Properties latestSucUpdateTimeProps = Utils.loadProp(LATEST_SUCCESS_UPDATE_TIME_PROPS_NAME);
        List<String> logRefreshPathList = new ArrayList<>();
        for (PathInfo pathInfo : pathInfoList) {
            String fmtTimeStr = latestSucUpdateTimeProps.getProperty(pathInfo.name);
            LocalDateTime dateTime = fmtTimeStr != null ?
                    LocalDateTime.parse(fmtTimeStr, DATE_TIME_FORMATTER) :
                    LocalDateTime.MIN;
            if (dateTime.plusHours(1).isBefore(LocalDateTime.now())) {
                logRefreshPathList.add(pathInfo.name);
                Map<String, String> params = Collections.singletonMap("pathName", pathInfo.name);
                try {
                    AVCloud.callFunction("getLatestNews", params);
                } catch (AVException e) {
                    e.printStackTrace();
                }
            }
        }

        // log
        if (logRefreshPathList.size() == 0) {
            logger.info("checkLatestUpdateTime: No need for update");
        } else {
            logger.info("checkLatestUpdateTime: " + logRefreshPathList);
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
        for (PathInfo info:pathInfoList){
            if (info.name.equals(pathName)) {
                pathInfo = info;
                break;
            }
        }
        if (pathInfo == null) {
            logger.error("getLatestNews: pathName[%s] not found",pathName);
            return;
        }
        Properties latestIdProps = Utils.loadProp(LATEST_ID_PROPS_NAME);
        NewsInfo newsInfo = getLatestNewsInfo(pathInfo, latestIdProps);
        if (newsInfo != null){
            logger.info("newsInfo:" + newsInfo);

            AVPush push = new AVPush();
            String message = pathName+"有新的公告";
            push.setMessage(message);
            push.sendInBackground();
            logger.info("push message:" + message);

            latestIdProps = Utils.loadProp(LATEST_ID_PROPS_NAME);
            latestIdProps.setProperty(pathName,newsInfo.id);
            Utils.saveProp(latestIdProps,LATEST_ID_PROPS_NAME);

            Properties latestSucUpdateTimeProps = Utils.loadProp(LATEST_SUCCESS_UPDATE_TIME_PROPS_NAME);
            latestSucUpdateTimeProps.setProperty(pathName,LocalDateTime.now().format(DATE_TIME_FORMATTER));
            Utils.saveProp(latestSucUpdateTimeProps,LATEST_SUCCESS_UPDATE_TIME_PROPS_NAME);
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
