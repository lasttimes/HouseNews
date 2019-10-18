package com.lje.public_rental_house_news;

import java.util.regex.Matcher;

public class NewsInfo implements Comparable<NewsInfo> {
    public String id;
    private String href;
    public String title;

    private NewsInfo(String href, String id, String title) {
        this.href = href;
        this.id = id;
        this.title = title;
    }

    @Override
    public String toString() {
        return "NewsInfo{" +
                "id='" + id + '\'' +
                ", href='" + href + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public int compareTo(NewsInfo o) {
        return this.id.compareTo(o.id);
    }

    public static Creator getCreator(String name) {
        if ("baoan".equalsIgnoreCase(name)) {
            return CREATOR_BAOAN;
        }else if ("SZJS".equalsIgnoreCase(name)){
            return CREATE_SZJS;
        }
        return CREATOR_DEFAULT;
    }

    public interface Creator {
        NewsInfo create(Matcher matcher);
    }

    private static Creator CREATOR_DEFAULT = matcher -> new NewsInfo(matcher.group(1), matcher.group(2), matcher.group(3));


    // 保安区
    private static Creator CREATOR_BAOAN = matcher -> new NewsInfo(matcher.group(2), matcher.group(3), matcher.group(1));

    // 市住住健局
    private static Creator CREATE_SZJS = matcher -> new NewsInfo(matcher.group(2), matcher.group(3), matcher.group(4));
}