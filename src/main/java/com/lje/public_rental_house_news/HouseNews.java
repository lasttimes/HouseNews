package cn.leancloud.demo.todo;

import cn.leancloud.EngineFunction;
import com.avos.avoscloud.AVObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test
 */
public class HouseNews {

    // 深圳住建局
    public static final String URL_SHENZEN = "http://www.szjs.gov.cn/csml/bgs/xxgk/tzgg_1/bgszfbz/index_16952.htm";

    // 龙岗住建局
    public static final String URL_LONGGANG = "http://www.lg.gov.cn/bmzz/zjj/xxgk/qt/tzgg/";

    private static final OkHttpClient sClient = new OkHttpClient();

    // 查找 class=ftdt-list 的 ul，其下的第一个 ui 对应的 href
    // 示例：
    // <ul class="ftdt-list">
    //      <li><a href="./201809/t20180921_14108653.htm" title="深圳市住房保障署关于暂停受理公共租赁住房置换申请的通知"
    private static final String REGEX_SHENZHEN = "<ul\\s*class=\"ftdt-list\">\\s*<li>\\s*<a\\s*href=\"(.*/(.*?).htm)\"\\s*title=\"(.*?)\".*?<span>(.*)</span></li>";

    //  div class="news_list">
    //      <ul>
    //          <li><a class="_recurl" data-link="" href="./201809/t20180925_14114538.htm" target="_blank">龙岗区住房和建设局关于在监工程项目未落实劳务工实名制和分账制管理的处理通报</a><span>2018-09-25</span></li>
    private static final String REGEX_LONGGANG = "<div\\s+class=\"news_list\">\\s*<ul>\\s*<li>\\s*<a.*href=\"(.*/(.*?).htm)\".*>(.*?)</a>.*?<span>(.*?)</span>";


    public static String getIdFromFileName(String fileName) {
        Pattern pattern = Pattern.compile("t[0-9]+_[0-9]+\\.htm.*");
        Matcher m = pattern.matcher(fileName);
        System.out.println("m.matches() = " + m.matches());
        return null;
    }

    // 查找 class=ftdt-list 的 ul，其下的第一个 ui 对应的 href，其最后为 id ，递增（应该吧）
    public static List<NewsInfo> getInfo(String htmlBody, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(htmlBody);
        while (m.find()) {
            NewsInfo info = new NewsInfo();
            info.href = m.group(1);
            info.id = m.group(2);
            info.title = m.group(3);
            info.date = m.group(4);
            System.out.println("info = " + info);
        }
        return null;
    }

    public static String getHtmlBody(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = sClient.newCall(request).execute();
        String body = response.body() != null ? response.body().string() : null;
        System.out.println("body:\n" + body + "\n");
        return body;
    }

    @EngineFunction("houseInfo")
    public static void getHouseInfo() {
        String htmlBody = null;
        try {
            htmlBody = getHtmlBody(URL_SHENZEN);
            getInfo(htmlBody, REGEX_SHENZHEN);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
        getHouseInfo();
    }

    interface FileSys {
        void saveProp(String fileName, String key, String value);

        String loadProp(String fileName, String key);
    }


    static class NewsInfo {
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
    }
}
