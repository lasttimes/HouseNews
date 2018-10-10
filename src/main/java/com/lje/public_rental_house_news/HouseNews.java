package com.lje.public_rental_house_news;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineFunctionParam;
import com.avos.avoscloud.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test
 */
public class HouseNews {

    private static Logger logger = LogManager.getLogger();

    private static final String TABLE_NAME = "LatestUpdate";
    private static final String COL_ORGANIZE_NAME = "organizeName";
    private static final String COL_NEWS_ID = "newsId";
    private static final String COL_TIME = "time";

    // 查找对应 html 中的 id ,对比保存的上一次最新id，
    // 如果没有保存记录，或者新id 大于保存id，返回对应 NewsInfo
    private static NewsInfo getLatestNewsInfo(PathInfo pathInfo, String lastId) {
        String htmlBody = Utils.getHtmlBodyText(logger,pathInfo.url);
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
        } else {
            logger.fatal("Pattern find failed! " + pathInfo.url);
        }
        return null;
    }

    // 从配置文件读取上次成功更新时间，如是超出1小时，则再次请求更新
    @EngineFunction("checkLatestUpdateTime")
    public static void checkLatestUpdateTime() {
        List<PathInfo> pathInfoList = Utils.loadPathList();
        logger.info(">>> checkLatestUpdateTime");
        if (pathInfoList == null) {
            logger.error("checkLatestUpdateTime: pathInfoList null");
            return;
        }
        for (PathInfo pathInfo : pathInfoList) {
            // 查询最后更新时间
            AVObject o = findAVObjectEquals(pathInfo.name);
            LocalDateTime dateTime;
            if (o == null) {
                dateTime = LocalDateTime.MIN;
            } else {
                Date d = o.getDate(COL_TIME);
                if (d == null) {
                    dateTime = LocalDateTime.MIN;
                }else{
                    dateTime = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
                }
            }

            boolean needUpdate = dateTime.plusHours(1).isBefore(LocalDateTime.now());
            logger.printf(Level.INFO, "checkLatestUpdateTime: [%s] at %s , %s", pathInfo.name, dateTime, needUpdate ? " Need Update" : " need no Update");
            if (!needUpdate) {
                continue;
            }
            Map<String, String> params = Collections.singletonMap("pathName", pathInfo.name);
            try {
                AVCloud.callFunction("getLatestNews", params);
            } catch (AVException e) {
                e.printStackTrace();
            }
        }
    }

    @EngineFunction("getLatestNews")
    public static void getLatestNews(@EngineFunctionParam("pathName") String pathName) {
        logger.info(">>> getLatestNews pathName:" + pathName);
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
            logger.error("getLatestNews: pathName[" + pathName + "] not found");
            return;
        }

        AVObject o = findAVObjectEquals(pathInfo.name);
        if (o == null) {
            o = new AVObject(TABLE_NAME);
            o.put(COL_ORGANIZE_NAME, pathName);
            o.put(COL_TIME,new Date());
        }
        String id = o.getString(COL_NEWS_ID);
        NewsInfo newsInfo = getLatestNewsInfo(pathInfo, id);
        logger.info("newsInfo:" + newsInfo);
        Date now = new Date();
        o.put(COL_TIME, now);
        if (newsInfo == null) {
            o.saveInBackground();
            return;
        }
        o.put(COL_TIME, newsInfo.id);
        o.saveInBackground();
        AVPush push = new AVPush();
        String message = pathName + "有新的公告";
        push.setMessage(message);
        push.sendInBackground();
        logger.info("push message:" + message);
    }

    private static AVObject findAVObjectEquals(Object equalsValue) {
        AVQuery<AVObject> query = new AVQuery<>(HouseNews.TABLE_NAME);
        query.whereEqualTo(HouseNews.COL_ORGANIZE_NAME, equalsValue);
        try {
            List<AVObject> list = query.find();
            if (list != null && list.size() > 0) {
                return list.get(0);
            }
        } catch (AVException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String... args) {
        checkLatestUpdateTime();
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
