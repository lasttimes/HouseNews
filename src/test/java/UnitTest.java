import com.lje.public_rental_house_news.NewsInfo;
import com.lje.public_rental_house_news.PathInfo;
import com.lje.public_rental_house_news.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitTest {

    private static Logger logger = LogManager.getLogger();

    @Test
    public void checkPattern() {
        List<PathInfo> pathInfoList = Utils.loadPathList();

        for (PathInfo pathInfo : pathInfoList) {
            String htmlBody = Utils.getHtmlBodyText(logger, pathInfo.url);
            Pattern pattern = Pattern.compile(pathInfo.regex);
            if (htmlBody == null) {
                logger.warn("htmlBody is null: " + pathInfo.url);
                continue;
            }
            Matcher m = pattern.matcher(htmlBody);
            logger.info(pathInfo.name + " url:" + pathInfo.url);
            Assert.assertTrue(m.find());
            NewsInfo info = NewsInfo.getCreator(pathInfo.creator).create(m);
            logger.info("newsInfo:" + info);
        }
    }

    @Test
    public void checkMailOK() throws IOException, MessagingException {
        String url = "http://www.yantian.gov.cn/cn/zwgk/tzgg/";
        String content = String.format("%s：%s<br/><a href=\"%s\">%s</a>", "盐田区", "关于征集2019年盐田区改革思路的公告 ", url, url);
        Utils.senHTMLdMail("lasttimes@163.com","测试",content);
    }

}
