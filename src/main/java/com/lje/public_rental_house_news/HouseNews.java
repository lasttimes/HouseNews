package com.lje.public_rental_house_news;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineFunctionParam;
import com.avos.avoscloud.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 在不同机构，在 path.json 中配置有地址及正则。
 * 根据正则查询最新的公告的 id
 * 如果 id 有更新，认为有新的公告，发送通知
 */
public class HouseNews {

    private static Logger logger = LogManager.getLogger();


    // 刷新网站间隔时间, in ms
    private static final int REFRESH_INTERVAL_IN_MS = 20;


    private static final String CLASS_NAME_LATEST_NEWS = "LatestNewsInfo";
    // 机构名，据此在 path.json 查询对应机构的地址及正则
    private static final String COL_ORGANIZE_NAME = "organizeName";
    // 页面id，据此判断是否有新的公告
    private static final String COL_NEWS_ID = "newsId";
    // 上次查询页面的时间
    private static final String COL_LAST_UPDATE_TIME = "lastUpdateHtmlTime";
    // 上次发送 push 时间
    private static final String COL_LAST_PUSH_TIME = "lastPushTime";

    // 查找对应 html 中的 id ,对比保存的上一次最新id，
    // 如果没有保存记录，或者新id 大于保存id，返回对应 NewsInfo
    private static NewsInfo getLatestNewsInfo(PathInfo pathInfo, String lastId) {
        String htmlBody = Utils.getHtmlBodyText(logger, pathInfo.url,pathInfo.charset);
        if (htmlBody == null) {
            return null;
        }
        Pattern pattern = Pattern.compile(pathInfo.regex);
        Matcher m = pattern.matcher(htmlBody);

        if (m.find()) {
            NewsInfo info = NewsInfo.getCreator(pathInfo.creator).create(m);
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
        for (PathInfo pathInfo : pathInfoList) {
            // 查询最后更新时间
            AVObject o = findAVObjectEquals(pathInfo.name);
            LocalDateTime dateTime;
            if (o == null) {
                dateTime = LocalDateTime.MIN;
            } else {
                Date d = o.getDate(COL_LAST_UPDATE_TIME);
                dateTime = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
            }

            boolean needUpdate = dateTime.plusMinutes(REFRESH_INTERVAL_IN_MS).isBefore(LocalDateTime.now());
            if (needUpdate) {
                logger.printf(Level.INFO, "checkLatestUpdateTime: [%s] at %s ,  Need Update", pathInfo.name, dateTime);
            } else {
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
            o = new AVObject(CLASS_NAME_LATEST_NEWS);
            o.put(COL_ORGANIZE_NAME, pathName);
            o.put(COL_LAST_UPDATE_TIME, new Date());
        }
        String id = o.getString(COL_NEWS_ID);
        NewsInfo newsInfo = getLatestNewsInfo(pathInfo, id);
        logger.info("newsInfo:" + newsInfo);
        Date now = new Date();
        o.put(COL_LAST_UPDATE_TIME, now);
        if (newsInfo == null) {
            o.saveInBackground();
            return;
        }

        // 发送推送
        o.put(COL_NEWS_ID, newsInfo.id);
        o.put(COL_LAST_PUSH_TIME, now);
        o.saveInBackground();

        sendPush(pathInfo, newsInfo);
        sendMail(pathInfo, newsInfo);
    }

    // 发送推送，如果失败，保存到重试表
    private static void sendPush(PathInfo pathInfo, NewsInfo newsInfo) {
        AVPush push = new AVPush();
        String message = pathInfo.name + ":" + newsInfo.title;
        push.setMessage(message);
        push.sendInBackground(new SendCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    return;
                }
                Utils.addErrorLog("push", "msg:" + message + " e:" + e.getMessage());
            }
        });
    }

    // 发送邮件，如果失败，保存到重试表
    private static void sendMail(PathInfo pathInfo, NewsInfo newsInfo) {
        Properties props = new Properties();
        try {
            Utils.loadProperties(props, "mail.properties");
        } catch (IOException e) {
            e.printStackTrace();
            Utils.addErrorLog("failed", "loadProperties error:" + e.getMessage());
            return;
        }
        String toAddress = props.getProperty("toAddress");
        String subject = pathInfo.name + "有新公告";
        String content = String.format("%s<br/>%s<br><a href=\"%s\">%s</a>", pathInfo.name, newsInfo.title, pathInfo.url, pathInfo.url);
        try {
            Utils.senHTMLdMail(toAddress, subject, content);
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
            Utils.addErrorLog("mail", "subject:" + subject + " e:" + e.getMessage());
        }
    }

    private static AVObject findAVObjectEquals(Object equalsValue) {
        AVQuery<AVObject> query = new AVQuery<>(HouseNews.CLASS_NAME_LATEST_NEWS);
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
}
