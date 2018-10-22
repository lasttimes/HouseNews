package com.lje.public_rental_house_news;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineFunctionParam;
import com.avos.avoscloud.*;
import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.Timezone;
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
public class Jandan {

    @EngineFunction("hello")
    public static void hello(){
        JBrowserDriver driver = new JBrowserDriver(Settings.builder().javaOptions("-Dquantum.verbose=true", "-Dprism.verbose=true", "-verbose:class").

                        timezone(Timezone.ASIA_TOKYO).build());
        driver.get("https://jandan.net/ooxx");
        String loadedPage = driver.getPageSource();
        System.out.println("loadedPage = " + loadedPage);
    }

    public static void main(String... args) {
        hello();
    }
}
