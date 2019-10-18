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
        logger.info(pathInfo.regex);
        Pattern pattern = Pattern.compile(pathInfo.regex);
        // test
//        pattern = Pattern.compile("<div");
        assertNotNull(htmlBody);
        // test
        htmlBody = "<div class=\"col-lg-6 col-sm-6 grayBg p10\">\n" +
                "                    <div class=\"news-item-temp1 whiteBg p20\">\n" +
                "                        <a href=\"./201910/t20191016_18331001.htm\"><strong>\n" +
                "                        关于面向盐田区户籍在册轮候家庭配租公共租赁住房认租初审结果公示的通告</strong><b>发布时间： ［2019-10-16］</b>\n" +
                "                        </a>\n" +
                "                    </div>\n" +
                "                </div>";
        Matcher m = pattern.matcher(htmlBody);
        while (m.find()){
            logger.info("m.find");
            logger.info(pathInfo.name + " url:" + pathInfo.url);
            NewsInfo info = NewsInfo.getCreator(pathInfo.creator).create(m);
            logger.info("newsInfo:" + info);
        }
    }
}
