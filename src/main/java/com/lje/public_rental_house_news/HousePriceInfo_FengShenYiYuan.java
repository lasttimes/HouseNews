package com.lje.public_rental_house_news;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 丰盛懿园房屋信息
 */
public class HousePriceInfo_FengShenYiYuan {

    private static final File FILE = new File("1.csv");

    private static final String BASE_URL = "https://amr.sz.gov.cn/0501W/Iframe/LicItemIframe.aspx?licId=fcda36b3-a5c9-412f-9921-b2e7bad15198&page=";

//    private static final String REGEX = "<tr class=\"tab_body\">\\s*<td>(.*)</td>\\s<td>(.*)</td>\\s<td>(.*)</td>\\s<td>(.*)</td>\\s<td>(.*)</td>\\s<td>(.*)</td>\\s<td>(.*)</td>\\s";


    private static final String REGEX = "<tr class=\"tab_body\">\\s*<td>(.*)</td>\\s*<td>(.*)</td>\\s*<td>(.*)</td>\\s*<td>(.*)</td>\\s*<td>(.*)</td>\\s*<td>(.*)</td>\\s*<td>(.*)</td>";

    private static void readHtml() {
        List<String> lines = new ArrayList<>();
        Pattern pattern = Pattern.compile(REGEX);
        for (int i = 1; i <= 342; i++) {
            String url = BASE_URL + i;
            String htmlBody = Utils.getHtmlBodyText(null, url,null);
            if (htmlBody != null) {
                Matcher m = pattern.matcher(htmlBody);
                while (m.find()){
                    StringBuilder line = new StringBuilder();
                    for (int j = 1 ;j <= 7 ;j ++){
                        line.append(m.group(j));
                        line.append(",");
                    }
                    lines.add(line.toString());
                }
            }
        }
        try {
            FileUtils.writeLines(FILE,lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
        readHtml();
    }

}
