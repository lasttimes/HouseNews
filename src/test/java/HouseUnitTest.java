import com.lje.public_rental_house_news.NewsInfo;
import com.lje.public_rental_house_news.PathInfo;
import com.lje.public_rental_house_news.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


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
        String uuid = UUID.randomUUID().toString();
        String url = "https://jandan.net/";
        String content = String.format("%s：%s<br/><a href=\"%s\">%s</a>", uuid, uuid, url, url);
        Utils.senHTMLdMail("61244036@qq.com", "测试", content);
    }

    @Test
    public void temp() {
        List<PathInfo> list = Utils.loadPathList();
        PathInfo pathInfo = list.get(list.size() -1);
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
