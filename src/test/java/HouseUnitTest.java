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

            String toAddress = "lasttimes@163.com";
            String subject = pathInfo.name + "，有新公告";
            String content = String.format("%s<br/>%s<br><a href=\"%s\">%s</a>", pathInfo.name, info.title, pathInfo.url, pathInfo.url);
            try {
                Utils.senHTMLdMail(toAddress, subject, content);
            } catch (IOException | MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void checkMailOK() throws IOException, MessagingException {
        String url = "http://www.baidu.com/";
        String content = String.format("%s：%s<br/><a href=\"%s\">%s</a>", "测试", "测试下 ", url, url);
        Utils.senHTMLdMail("lasttimes@163.com,61244036@qq.com", "测试", content);
    }

    @Test
    public void temp() {
        List<PathInfo> list = Utils.loadPathList();
        PathInfo pathInfo = list.get(list.size() - 1);
        logger.info(pathInfo.toString());

        String htmlBody = Utils.getHtmlBodyText(logger, pathInfo.url, pathInfo.charset);
        logger.info(pathInfo.regex);
        Pattern pattern = Pattern.compile(pathInfo.regex);
        assertNotNull(htmlBody);
        Matcher m = pattern.matcher(htmlBody);
        while (m.find()){
            logger.info("m.find");
            logger.info(pathInfo.name + " url:" + pathInfo.url);
            NewsInfo info = NewsInfo.getCreator(pathInfo.creator).create(m);
            logger.info("newsInfo:" + info);
        }
    }
}
