import com.lje.public_rental_house_news.NewsInfo;
import com.lje.public_rental_house_news.PathInfo;
import com.lje.public_rental_house_news.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.junit.Assert.*;

import org.junit.Test;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HouseUnitTest {

    private static Logger logger = LogManager.getLogger();

    @Test
    public void checkPattern() {
        List<PathInfo> pathInfoList = Utils.loadPathList();

        for (PathInfo pathInfo : pathInfoList) {
            logger.info(pathInfo.toString());
            String htmlBody = Utils.getHtmlBodyText(logger, pathInfo.url, pathInfo.charset);
            Pattern pattern = Pattern.compile(pathInfo.regex);
            if (htmlBody == null) {
                logger.warn("htmlBody is null: " + pathInfo.url);
                continue;
            }
            logger.info(pathInfo.toString());
            Matcher m = pattern.matcher(htmlBody);
            assertTrue(m.find());
            NewsInfo info = NewsInfo.getCreator(pathInfo.creator).create(m);
            logger.info("newsInfo:" + info);
        }
    }

    @Test
    public void checkMailOK() throws IOException, MessagingException {
        String url = "http://www.yantian.gov.cn/cn/zwgk/tzgg/";
        String content = String.format("%s：%s<br/><a href=\"%s\">%s</a>", "盐田区", "关于征集2019年盐田区改革思路的公告 ", url, url);
        Utils.senHTMLdMail("lasttimes@163.com", "测试", content);
    }

    @Test
    public void temp() {
        List<PathInfo> list = Utils.loadPathList();
        PathInfo pathInfo = list.get(list.size() - 1);
        logger.info(pathInfo.toString());

        String htmlBody = Utils.getHtmlBodyText(logger, pathInfo.url, pathInfo.charset);
        Pattern pattern = Pattern.compile(pathInfo.regex);
        assertNotNull(htmlBody);
        // test
        htmlBody = "<a href=\"./201910/t20191008_18242711.htm\" title=\"深圳市大鹏新区住房和建设局关于面向大鹏新区先进制造业企业定向配租公共租赁住房的通告\">深圳市大鹏新区住房和建设局关于面向大鹏新区先进制造业企业定向配租公共租赁住房的通告</a></li>";
        Matcher m = pattern.matcher(htmlBody);
        while (m.find()){
            logger.info(pathInfo.name + " url:" + pathInfo.url);
            NewsInfo info = NewsInfo.getCreator(pathInfo.creator).create(m);
            logger.info("newsInfo:" + info);
        }
    }
}
